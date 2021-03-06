package net.fortytwo.ripple.model;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.keyval.KeyValueValue;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.util.Arrays;
import java.util.Comparator;

/*
 * TODO: this comparator currently ignores the equivalence of RDF lists with native lists, and of numeric values with numeric-typed literals
 * <p/>
 * literal
 * plain literal
 * plain literal without language tag
 * plain literal with language tag
 * typed literal
 * numeric typed literal
 * other typed literal
 * resource
 * list
 * operator
 * other resource
 */

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleValueComparator implements Comparator<RippleValue> {
    private final ModelConnection modelConnection;

    public RippleValueComparator(final ModelConnection mc) {
        this.modelConnection = mc;
    }

    public int compare(final RippleValue first,
                       final RippleValue second) {
        try {
            int c = findType(first).compareTo(findType(second));

            if (0 == c) {
                switch (first.getType()) {
                    case PLAIN_LITERAL_WITHOUT_LANGUAGE_TAG:
                        return comparePlainLiteralWithoutLanguageTag(first, second);
                    case PLAIN_LITERAL_WITH_LANGUAGE_TAG:
                        return comparePlainLiteralWithLanguageTag(first, second);
                    case NUMERIC_TYPED_LITERAL:
                        return compareNumericTypedLiteral(first, second);
                    case OTHER_TYPED_LITERAL:
                        return compareOtherTypedLiteral(first, second);
                    case LIST:
                        return compareList(first, second);
                    case KEYVALUE_VALUE:
                        return compareKeyValueValue(first, second);
                    case OPERATOR:
                        return compareOperator(first, second);
                    case OTHER_RESOURCE:
                        return compareOtherResource(first, second);
                    default:
                        // Shouldn't happen.
                        return 0;
                }
            } else {
                return c;
            }
        } catch (RippleException e) {
            e.logError();
            return 0;
        }
    }

    private RippleValue.Type findType(final RippleValue value) throws RippleException {
        if (value instanceof RDFValue) {
            Value sesameValue = ((RDFValue) value).sesameValue();

            if (sesameValue instanceof Literal) {
                URI datatype = ((Literal) sesameValue).getDatatype();

                if (null == datatype) {
                    String language = ((Literal) sesameValue).getLanguage();

                    if (null == language) {
                        return RippleValue.Type.PLAIN_LITERAL_WITHOUT_LANGUAGE_TAG;
                    } else {
                        return RippleValue.Type.PLAIN_LITERAL_WITH_LANGUAGE_TAG;
                    }
                } else {
                    if (NumericValue.isNumericLiteral((Literal) sesameValue)) {
                        return RippleValue.Type.NUMERIC_TYPED_LITERAL;
                    } else {
                        return RippleValue.Type.OTHER_TYPED_LITERAL;
                    }
                }
            } else if (sesameValue instanceof Resource) {
                if (Operator.isRDFList((RDFValue) value, modelConnection)) {
                    return RippleValue.Type.LIST;
                } else {
                    return RippleValue.Type.OTHER_RESOURCE;
                }
            } else {
                throw new RippleException("Sesame value has unrecognized class: " + sesameValue);
            }
        } else {
            return value.getType();
        }
    }

    private int comparePlainLiteralWithoutLanguageTag(final RippleValue first,
                                                      final RippleValue second) throws RippleException {
        return first.toRDF(modelConnection).sesameValue().stringValue().compareTo(
                second.toRDF(modelConnection).sesameValue().stringValue());
    }

    private int comparePlainLiteralWithLanguageTag(final RippleValue first,
                                                   final RippleValue second) throws RippleException {
        Literal firstLiteral = (Literal) first.toRDF(modelConnection).sesameValue();
        Literal secondLiteral = (Literal) second.toRDF(modelConnection).sesameValue();

        int c = firstLiteral.getLanguage().compareTo(
                secondLiteral.getLanguage());
        if (0 == c) {
            return firstLiteral.getLabel().compareTo(
                    secondLiteral.getLabel());
        } else {
            return c;
        }
    }

    private int compareNumericTypedLiteral(final RippleValue first,
                                           final RippleValue second) throws RippleException {
        if (first instanceof NumericValue) {
            if (second instanceof NumericValue) {
                return ((NumericValue) first).compareTo((NumericValue) second);
            } else {
                return -NumericValue.compare((Literal) ((RDFValue) second).sesameValue(), (NumericValue) first);
            }
        } else {
            if (second instanceof NumericValue) {
                return NumericValue.compare((Literal) ((RDFValue) first).sesameValue(), (NumericValue) second);
            } else {
                return NumericValue.compareNumericLiterals(
                        (Literal) ((RDFValue) first).sesameValue(),
                        (Literal) ((RDFValue) second).sesameValue());
            }
        }
    }

    private int compareOtherTypedLiteral(final RippleValue first,
                                         final RippleValue second) throws RippleException {
        Literal firstLiteral = (Literal) first.toRDF(modelConnection).sesameValue();
        Literal secondLiteral = (Literal) second.toRDF(modelConnection).sesameValue();

        int c = firstLiteral.getDatatype().stringValue().compareTo(
                secondLiteral.getDatatype().stringValue());
        if (0 == c) {
            return firstLiteral.getLabel().compareTo(
                    secondLiteral.getLabel());
        } else {
            return c;
        }
    }

    // TODO: list comparison is vulnerable to infinite loops due to circular lists
    private int compareList(final RippleValue first,
                            final RippleValue second) throws RippleException {
        if (first instanceof RippleList) {
            if (second instanceof RippleList) {
                return compareNativeLists((RippleList) first, (RippleList) second);
            } else {
                Collector<RippleList> firstLists
                        = new Collector<RippleList>();
                Collector<RippleList> secondLists
                        = new Collector<RippleList>();
                firstLists.put((RippleList) first);
                modelConnection.toList(second, secondLists);
                return compareListCollectors(firstLists, secondLists);
            }
        } else {
            if (second instanceof RippleList) {
                Collector<RippleList> firstLists
                        = new Collector<RippleList>();
                Collector<RippleList> secondLists
                        = new Collector<RippleList>();
                modelConnection.toList(first, firstLists);
                secondLists.put((RippleList) second);
                return compareListCollectors(firstLists, secondLists);
            } else {
                Collector<RippleList> firstLists
                        = new Collector<RippleList>();
                Collector<RippleList> secondLists
                        = new Collector<RippleList>();
                modelConnection.toList(first, firstLists);
                modelConnection.toList(second, secondLists);
                return compareListCollectors(firstLists, secondLists);
            }
        }
    }

    private int compareKeyValueValue(final RippleValue first,
                                     final RippleValue second) {
        return ((KeyValueValue) first).compareTo((KeyValueValue) second);
    }

    private int compareOperator(final RippleValue first,
                                final RippleValue second) throws RippleException {
        // TODO: let Operator implement Comparable<Operator>
        return 0;
    }

    private int compareOtherResource(final RippleValue first,
                                     final RippleValue second) throws RippleException {
//System.out.println("first = " + first + ", second = " + second);
        RDFValue firstRdf = first.toRDF(modelConnection);
        RDFValue secondRdf = second.toRDF(modelConnection);
        return null == firstRdf
                ? null == secondRdf ? 0
                : -1
                : null == secondRdf ? 1
                : firstRdf.sesameValue().stringValue().compareTo(
                secondRdf.sesameValue().stringValue());
    }

    private int compareNativeLists(final RippleList first,
                                   final RippleList second) throws RippleException {
        RippleList firstCur = first;
        RippleList secondCur = second;

        while (!firstCur.isNil()) {
            if (secondCur.isNil()) {
                return 1;
            }

            int cmp = compare(firstCur.getFirst(), secondCur.getFirst());

            if (0 != cmp) {
                return cmp;
            }

            firstCur = firstCur.getRest();
            secondCur = secondCur.getRest();
        }

        if (secondCur.isNil()) {
            return 0;
        } else {
            return -1;
        }
    }

    private int compareListCollectors(final Collector<RippleList> firstLists,
                                      final Collector<RippleList> secondLists) throws RippleException {
        int firstSize = firstLists.size();
        int secondSize = secondLists.size();

        if (firstSize < secondSize) {
            return -1;
        } else if (secondSize < firstSize) {
            return 1;
        }

        // This is common enough to probably be worth a special case.
        else if (1 == firstSize) {
            return compareNativeLists(firstLists.iterator().next(),
                    secondLists.iterator().next());
        } else {
            RippleList firstArray[] = new RippleList[firstSize];
            RippleList secondArray[] = new RippleList[secondSize];

            int i = 0;
            for (RippleList firstList : firstLists) {
                firstArray[i] = firstList;
            }
            i = 0;
            for (RippleList secondList : secondLists) {
                secondArray[i] = secondList;
            }

            Comparator<RippleList> comparator = new RippleListComparator();
            Arrays.sort(firstArray, comparator);
            Arrays.sort(secondArray, comparator);

            for (int j = 0; j < firstSize; j++) {
                int c = compareNativeLists(firstArray[j], secondArray[j]);
                if (0 != c) {
                    return c;
                }
            }

            return 0;
        }
    }

    private class RippleListComparator implements Comparator<RippleList> {
        public int compare(final RippleList first,
                           final RippleList second) {
            try {
                return compareNativeLists(first, second);
            } catch (RippleException e) {
                e.logError();
                return 0;
            }
        }
    }
}

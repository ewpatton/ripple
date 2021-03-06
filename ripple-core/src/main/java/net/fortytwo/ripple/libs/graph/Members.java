package net.fortytwo.ripple.libs.graph;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.Model;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RDFValue;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.impl.sesame.SesameModel;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;

/**
 * A primitive which consumes an RDF container and produces all items in the
 * container.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Members extends PrimitiveStackMapping {
    private static final String[] IDENTIFIERS = {
            GraphLibrary.NS_2013_03 + "members",
            GraphLibrary.NS_2008_08 + "members",
            GraphLibrary.NS_2007_08 + "contains",
            GraphLibrary.NS_2007_05 + "contains"};

    private static final Logger LOGGER = Logger.getLogger(Members.class);

    public String[] getIdentifiers() {
        return IDENTIFIERS;
    }

    public Members() throws RippleException {
        super();
    }

    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("c", "an RDF Container; for instance, a Bag", true)};
    }

    public String getComment() {
        return "c  =>  x  -- for each member x of Container c";
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {

        Model model = mc.getModel();
        if (model instanceof SesameModel) {
            RippleList stack = arg;

            RippleValue head = stack.getFirst();
            final RippleList rest = stack.getRest();

            final Sink<RippleValue> pushSink = new Sink<RippleValue>() {
                public void put(final RippleValue v) throws RippleException {
                    solutions.put(rest.push(v));
                }
            };

            Sink<Statement> stSink = new Sink<Statement>() {
                public void put(final Statement st) throws RippleException {
                    if ('_' == st.getPredicate().getLocalName().charAt(0)) {
                        pushSink.put(new RDFValue(st.getObject()));
                    }
                }
            };

            mc.getStatements(head.toRDF(mc), null, null, stSink);

            /*
            int i = 1;
            while (true) { // Break out when there are no more members to produce
                Collector<RippleValue, RippleException> results = new Collector<RippleValue, RippleException>();
                RDFValue pred = new RDFValue(mc.createURI(RDF.NAMESPACE + "_" + i));
                StatementPatternQuery query = new StatementPatternQuery(head, pred, null, false);
                mc.query(query, results);
                if (0 == results.size()) {
                    break;
                }
                results.writeTo(pushSink);
                i++;
            }*/
        } else {
            LOGGER.warn("primitive is compatible only with the Sesame model: " + this);
        }
    }
}


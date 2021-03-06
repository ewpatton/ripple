package net.fortytwo.ripple.model;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.io.RipplePrintStream;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface RippleValue {
    // TODO: rpl:op may not be handled consistently.
    public enum Type {
        KEYVALUE_VALUE,
        LIST,
        NUMERIC_TYPED_LITERAL,
        OPERATOR,
        OTHER_RESOURCE,  // Note: includes PrimitiveStackMapping
        OTHER_TYPED_LITERAL,
        PLAIN_LITERAL_WITHOUT_LANGUAGE_TAG,
        PLAIN_LITERAL_WITH_LANGUAGE_TAG,
    }

    RDFValue toRDF(ModelConnection mc) throws RippleException;

    StackMapping getMapping();

    void printTo(RipplePrintStream p) throws RippleException;

    Type getType();
}


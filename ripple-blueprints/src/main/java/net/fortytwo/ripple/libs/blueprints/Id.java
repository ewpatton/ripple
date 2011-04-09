package net.fortytwo.ripple.libs.blueprints;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.StackContext;

/**
 * User: josh
 * Date: 4/5/11
 * Time: 8:11 PM
 */
public class Id extends PrimitiveStackMapping {
    @Override
    public String[] getIdentifiers() {
        return new String[]{
                BlueprintsLibrary.NS_2011_04 + "id"
        };
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("el", "a Blueprints element (vertex or edge)", true)};
    }

    @Override
    public String getComment() {
        return "finds the id of a Blueprints element (vertex or edge)";
    }

    @Override
    public void apply(final StackContext arg,
                      final Sink<StackContext, RippleException> solutions) throws RippleException {
        ModelConnection mc = arg.getModelConnection();
        RippleList stack = arg.getStack();
        RippleValue first = stack.getFirst();
        stack = stack.getRest();

        if (first instanceof ElementValue) {
            Element el = ((ElementValue) first).getElement();
            solutions.put(arg.with(stack.push(BlueprintsLibrary.createRippleValue(el.getId(), mc))));
        }
    }
}
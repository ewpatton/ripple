package net.fortytwo.ripple.libs.math;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.NumericValue;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.StackMapping;

/**
 * A primitive which consumes a number and produces the base-10 logarithm of the
 * number.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Log10 extends PrimitiveStackMapping
{
    private static final String[] IDENTIFIERS = {
            MathLibrary.NS_2013_03 + "log10",
            MathLibrary.NS_2008_08 + "log10",
            MathLibrary.NS_2007_08 + "log10"};

    private final StackMapping self;

    public String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

	public Log10()
		throws RippleException
	{
		super();

        this.self = this;
	}

    public Parameter[] getParameters()
    {
        return new Parameter[] {
                new Parameter( "x", null, true )};
    }

    public String getComment()
    {
        return "x  =>  base-10 logarithm of x";
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {
		RippleList stack = arg;

		double a;
		NumericValue result;

		a = mc.toNumericValue( stack.getFirst() ).doubleValue();
		stack = stack.getRest();

		// Apply the function only if it is defined for the given argument.
		if ( a > 0 )
		{
			result = mc.numericValue(Math.log10(a));

			solutions.put(
					stack.push( result ) );
		}
	}

    @Override
    public StackMapping getInverse()
    {
        return new Pow10();
    }

    public class Pow10 implements StackMapping
    {
        public int arity()
        {
            return 1;
        }

        public StackMapping getInverse() throws RippleException
        {
            return self;
        }

        public boolean isTransparent()
        {
            return true;
        }

        public void apply(final RippleList arg,
                          final Sink<RippleList> solutions,
                          final ModelConnection mc) throws RippleException {
            RippleList stack = arg;
            NumericValue x, result;

            x = mc.toNumericValue( stack.getFirst() );
            stack = stack.getRest();

            result = mc.numericValue(10).pow( x );

            solutions.put(
                    stack.push( result ) );
        }
    }
}


/*
 * $URL: https://ripple.googlecode.com/svn/trunk/ripple-core/src/main/java/net/fortytwo/ripple/libs/stack/Cons.java $
 * $Revision: 71 $
 * $Author: parcour $
 *
 * Copyright (C) 2007-2008 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.stack;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.flow.Sink;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.StackContext;

/**
 * A primitive which consumes an item and a list, prepends the item to the list,
 * then produces the resulting list.
 */
public class Take extends PrimitiveStackMapping
{
	private static final int ARITY = 2;

    private static final String[] IDENTIFIERS = {
            StackLibrary.NS_2008_08 + "take"};

    public String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

	public Take() throws RippleException
	{
		super();
	}

	public int arity()
	{
		return ARITY;
	}

	public void apply( final StackContext arg,
                       final Sink<StackContext, RippleException> solutions )
		throws RippleException
	{
		final ModelConnection mc = arg.getModelConnection();
		RippleList stack = arg.getStack();

        // Note: a bad numeric value will cause an error.  However, an
        // out-of-range numeric value (e.g. -1 or a value which exceeds the
        // length of the list) will simply fail to produce a result.
        final int n = mc.toNumericValue(stack.getFirst()).intValue();
        if ( 0 > n )
        {
            return;
        }

        stack = stack.getRest();
		final RippleValue l = stack.getFirst();
		final RippleList rest = stack.getRest();

		Sink<RippleList, RippleException> listSink = new Sink<RippleList, RippleException>()
		{
			public void put( final RippleList list ) throws RippleException
			{
                RippleList result = take( list, n, mc.list() );
                if ( null != result )
                {
                    solutions.put( arg.with(
                            rest.push( result ) ) );
                }
            }
		};

		mc.toList( l, listSink );
	}

    private RippleList take( final RippleList list,
                             final int n,
                             final RippleList nil ) throws RippleException
    {
        RippleList cur = list;
        RippleList inverted = nil;

        for ( int i = 0; i < n; i++ )
        {
            if ( cur.isNil() )
            {
                return null;
            }

            inverted = inverted.push( cur.getFirst() );
            cur = cur.getRest();
        }

        return inverted.invert();
    }
}
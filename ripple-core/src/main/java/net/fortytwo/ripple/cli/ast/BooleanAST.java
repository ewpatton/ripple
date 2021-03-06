package net.fortytwo.ripple.cli.ast;

import net.fortytwo.ripple.query.QueryEngine;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.flow.Sink;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class BooleanAST implements AST<RippleList>
{
	private final boolean value;

	public BooleanAST( final boolean value )
	{
		this.value = value;
	}

	public void evaluate( final Sink<RippleList> sink,
						final QueryEngine qe,
						final ModelConnection mc )
		throws RippleException
	{
		sink.put( mc.list().push( mc.booleanValue(value) ) );
	}

	public String toString()
	{
		return "" + value;
	}
}


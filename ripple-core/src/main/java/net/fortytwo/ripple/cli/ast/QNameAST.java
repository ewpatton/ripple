package net.fortytwo.ripple.cli.ast;

import net.fortytwo.ripple.query.QueryEngine;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.RippleList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class QNameAST implements AST<RippleList>
{
	private final String nsPrefix, localName;

	public QNameAST( final String nsPrefix, final String localName )
	{
		this.nsPrefix = nsPrefix;
		this.localName = localName;
	}

	public String toString()
	{
		return ( ( null == nsPrefix ) ? "" : nsPrefix )
			+ ":"
			+ ( ( null == localName ) ? "" : localName );
	}

	public void evaluate( final Sink<RippleList> sink,
						final QueryEngine qe,
						final ModelConnection mc )
		throws RippleException
	{
		Sink<RippleValue> uriSink = new Sink<RippleValue>()
		{
			public void put(final RippleValue v) throws RippleException
			{
				sink.put( mc.list().push( v ) );
			}
		};

		qe.getLexicon().uriForQName( nsPrefix, localName, uriSink, mc, qe.getErrorPrintStream() );
	}
}


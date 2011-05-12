/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2011 Joshua Shinavier
 */


package net.fortytwo.ripple.model;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.io.RipplePrintStream;
import net.fortytwo.ripple.model.keyval.KeyValueMapping;
import net.fortytwo.ripple.util.ModelConnectionHelper;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

public class Operator implements RippleValue
{
	public static final Operator OP = new Operator( new Op() );

	private static final RDFValue RDF_FIRST = new RDFValue( RDF.FIRST );

	private final StackMapping mapping;

    private RDFValue rdfEquivalent = null;

    private Operator( final String key )
    {
        mapping = new KeyValueMapping( key );
    }

	private Operator( final RDFValue pred ) throws RippleException
	{
		mapping = new RDFPredicateMapping( StatementPatternQuery.Pattern.SP_O, pred, null );
	}

	public Operator( final StackMapping mapping )
	{
//System.out.println( "Operator[" + this + "](" + function + ")" );
		this.mapping = mapping;
	}

	public Operator( final RippleList list )
	{
//System.out.println( "Operator[" + this + "](" + list + ")" );
		mapping = new ListDequotation( list );
	}

	public StackMapping getMapping()
	{
		return mapping;
	}

    // TODO: implement equals() and hashCode() for all StackMappings
    public boolean equals( final Object other )
    {
        return ( other instanceof Operator )
                ? ( (Operator) other ).mapping.equals( mapping )
                : false;
    }

    public int hashCode()
    {
        return 630572943 + mapping.hashCode();
    }

    public String toString()
	{
		return "Operator(" + mapping + ")";
	}

	public void printTo( final RipplePrintStream p )
		throws RippleException
	{
		p.print( rdfEquivalent );
	}

	public boolean isActive()
	{
		return true;
	}

public RDFValue toRDF( final ModelConnection mc )
	throws RippleException
{
// Note: only correct for OP, but I expect this method only to be used with OP anyway
if ( null == rdfEquivalent )
{
	rdfEquivalent = mc.uriValue("http://fortytwo.net/2007/03/ripple/schema#op");
}
return rdfEquivalent;
}

	/**
	 *  Finds the type of a value and creates an appropriate "active" wrapper.
	 */
	public static void createOperator( final RippleValue v,
                                       final Sink<Operator, RippleException> opSink,
                                       final ModelConnection mc )
		throws RippleException
	{
        // A function becomes active.
		if ( v instanceof StackMapping)
		{
			opSink.put( new Operator( (StackMapping) v ) );
			return;
		}

		// A list is dequoted.
		else if ( v instanceof RippleList )
		{
			opSink.put( new Operator( (RippleList) v ) );
			return;
		}

		// This is the messy part.  Attempt to guess the type of the object from
		// the available RDF statements, and create the appropriate object.
		if ( v instanceof RDFValue)
		{
            if ( isRDFList( (RDFValue) v, mc ) )
			{
                Sink<RippleList, RippleException> listSink = new Sink<RippleList, RippleException>()
				{
					public void put( final RippleList list )
						throws RippleException
					{
						opSink.put( new Operator( list ) );
					}
				};

				mc.toList( v, listSink );
                return;
			}

            else
            {
                Value sv = ( (RDFValue) v ).sesameValue();

                if ( sv instanceof Literal )
                {
                    opSink.put( new Operator( ( (Literal) sv ).getLabel() ) );
                    return;
                }

                // An RDF resource not otherwise recognizable becomes a predicate filter.
                else if ( sv instanceof Resource)
                {
                    opSink.put( new Operator( (RDFValue) v ) );
                    return;
                }
            }
        }

		// Anything else becomes an active nullary filter with no output.
	    opSink.put( new Operator( new NullStackMapping() ) );
	}

// TODO: replace this with something a little more clever
	public static boolean isRDFList( final RDFValue v, final ModelConnection mc )
		throws RippleException
	{
        ModelConnectionHelper h = new ModelConnectionHelper(mc);
		return ( v.sesameValue().equals( RDF.NIL )
			|| null != h.findSingleObject( v, RDF_FIRST ) );
	}

    public Type getType() {
        return Type.OPERATOR;
    }
}


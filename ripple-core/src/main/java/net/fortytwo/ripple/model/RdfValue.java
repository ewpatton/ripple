/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2008 Joshua Shinavier
 */


package net.fortytwo.ripple.model;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.io.RipplePrintStream;
import net.fortytwo.ripple.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.BNode;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *  Note: this class has a natural ordering that is inconsistent with equals.
 */
public class RdfValue implements RippleValue
{
	private final Value value;
	
	public RdfValue( final Value value )
	{
		this.value = value;
	}

	public Value sesameValue()
	{
		return value;
	}

	public RdfValue toRDF( final ModelConnection mc )
	{
		return this;
	}

	public void printTo( final RipplePrintStream p )
		throws RippleException
	{
		p.print( value );
	}

	public boolean isActive()
	{
		return false;
	}

// FIXME: not consistent with hashCode()
	public boolean equals( final Object other )
	{
		return ( other instanceof RippleValue )
		        ? ( 0 == compareTo( (RippleValue) other ) )
		        : false;
	}

	public int hashCode()
	{
		return value.hashCode();
	}

	public String toString()
	{
		if ( value instanceof URI )
		{
			URI uri = (URI) value;
			return "<" + uri.getNamespace() + uri.getLocalName() + ">";
		}

		else if ( value instanceof Literal )
		{
			Literal lit = (Literal) value;
			return "\"" + StringUtils.escapeString( lit.getLabel() ) + "\""
					+ ( (null != lit.getDatatype() ) ? "^^<" + lit.getDatatype() + ">" : "" );
		}

		else
		{
			// Use Sesame's toString() methods.
			return value.toString();
		}
	}

    // Global comparison stuff -- should be moved //////////////////////////////


    public static int defaultCompare( final Object first, final Object second )
    {
        int n = first.getClass().getName().compareTo(
                second.getClass().getName() );
        return ( 0 == n )
                ? first.toString().compareTo( second.toString() )
                : n;
    }

    public static int compare( final Literal first, final Literal second )
    {
//        System.out.println( "comparing Literals: " + first + ", " + second );
        URI firstDatatype = first.getDatatype(), secondDatatype = second.getDatatype();
        String firstLanguage = first.getLanguage(), secondLanguage = second.getLanguage();

        return ( NumericValue.isNumericLiteral( first ) && NumericValue.isNumericLiteral( second ) )
                // Numeric literals are a special case
                // FIXME: this logic should really be in NumericValue
                ? NumericValue.compareNumericLiterals( first, second )
                // All other literals are compared firstly by comparing their
                // data types, then their languages, then their labels.
                : ( null == firstDatatype )
                        ? ( null == secondDatatype )
                                ? ( null == firstLanguage )
                                        ? ( null == secondLanguage )
                                                ? first.getLabel().compareTo( second.getLabel() )
                                                : -1
                                        : ( null == secondLanguage )
                                                ? 1
                                                : ( firstLanguage.equals( secondLanguage ) )
                                                    ? first.getLabel().compareTo( second.getLabel() )
                                                    : firstLanguage.compareTo( secondLanguage )
                                : -1
                        : ( null == secondDatatype )
                                ? 1
                                : ( firstDatatype.equals( secondDatatype ) )
                                        ? first.getLabel().compareTo( second.getLabel() )
                                        : firstDatatype.toString().compareTo( secondDatatype.toString() );
    }

    // Note: first is assumed to be a numeric value
    public static int compare( final Literal first, final NumericValue second )
    {
        return NumericValue.compare( first, second );
    }

    public static int compare( final RdfValue first, final NumericValue second )
    {
        return ( first.value instanceof Literal && NumericValue.isNumericLiteral( (Literal) first.value ) )
                ? NumericValue.compare( (Literal) first.value, second )
                : defaultCompare( first, second );
    }

    public int compareTo( final RippleValue other )
	{
//System.out.println("this = " + this + ", other = " + other);
        return ( value instanceof Literal )
                ? ( other instanceof RdfValue )
                        ? ( ( (RdfValue) other ).value instanceof Literal )
                                ? compare( (Literal) value, (Literal) ( (RdfValue) other ).value )
                                : defaultCompare( this, other )
                        : ( other instanceof NumericValue )
                                ? NumericValue.isNumericLiteral( (Literal) value )
                                        ? NumericValue.compare( (Literal) value, (NumericValue) other )
                                        : defaultCompare( this, other )
                                : defaultCompare( this, other )
                : ( value instanceof BNode )
                        ? ( other instanceof RdfValue )
                                ? ( ( (RdfValue) other ).value instanceof BNode )
                                        ? ( (BNode) value ).getID().compareTo( ( (BNode) ( (RdfValue) other ).value ).getID() )
                                        : defaultCompare( value, ( (RdfValue) other ).value )
                                : defaultCompare( this, other )
                        : ( value instanceof URI )
                                ? ( other instanceof RdfValue )
                                        ? ( ( (RdfValue) other ).value instanceof URI )
                                                ? value.toString().compareTo( ( (RdfValue) other ).value.toString() )
                                                : defaultCompare( value, ( (RdfValue) other ).value )
                                        : defaultCompare( this, other )
                                : defaultCompare( this, other );
	}
}

package net.fortytwo.ripple.model.impl.sesame;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.NumericValue;
import net.fortytwo.ripple.model.RDFValue;

import java.math.BigDecimal;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SesameNumericValue extends NumericValue {

	private RDFValue rdfEquivalent = null;

	public SesameNumericValue( final int i )
	{
		type = Type.INTEGER;
		number = i;
	}
    
    public SesameNumericValue( final long l )
	{
		type = Type.LONG;
		number = l;
	}
	
	public SesameNumericValue( final double d )
	{
		type = Type.DOUBLE;
		number = d;
	}

    public SesameNumericValue( final float f )
	{
		type = Type.FLOAT;
		number = f;
	}

    public SesameNumericValue( final BigDecimal b )
	{
		type = Type.DECIMAL;
		number = b;
	}

	public SesameNumericValue( final RDFValue rdf )
		throws RippleException
	{
		rdfEquivalent = rdf;
		Value v = rdf.sesameValue();

		if ( !( v instanceof Literal ) )
		{
			throw new RippleException( "value " + v.toString() + " is not numeric" );
		}

		URI dataType = ( (Literal) v ).getDatatype();

		if ( null == dataType )
		{
			throw new RippleException( "value is untyped" );
		}

		else if ( dataType.equals( XMLSchema.INTEGER )
			|| dataType.equals( XMLSchema.INT ) )
		{
			try
			{
				type = Type.INTEGER;
				number = ((Literal) v).intValue();
			}

			catch ( Throwable t )
			{
				throw new RippleException( t );
			}
		}

		else if ( dataType.equals( XMLSchema.LONG ) )
		{
			try
			{
				type = Type.LONG;
				number = (long) ((Literal) v).intValue();
			}

			catch ( Throwable t )
			{
				throw new RippleException( t );
			}
		}
		
		else if ( dataType.equals( XMLSchema.DOUBLE ) )
		{
			try
			{
				type = Type.DOUBLE;
                number = doubleValue( (Literal) v );
            }

			catch ( Throwable t )
			{
				throw new RippleException( t );
			}
		}

		else if ( dataType.equals( XMLSchema.FLOAT ) )
		{
			try
			{
				type = Type.FLOAT;
				number = ((Literal) v).floatValue();
			}

			catch ( Throwable t )
			{
				throw new RippleException( t );
			}
		}

        else if ( dataType.equals( XMLSchema.DECIMAL ) )
		{
			try
			{
				type = Type.DECIMAL;
				number = ( (Literal) v ).decimalValue();
			}

			catch ( Throwable t )
			{
				throw new RippleException( t );
			}
		}

        else
		{
			throw new RippleException( "not a recognized numeric data type: " + dataType );
		}
	}
	
	public RDFValue toRDF( final ModelConnection mc )
		throws RippleException
	{
		SesameModelConnection smc = (SesameModelConnection) mc;
		if ( null == rdfEquivalent )
		{
			switch ( type )
			{
				case INTEGER:
                    // Don't use ValueFactory.creatLiteral(int), which (at
                    // least in this case) produces xsd:int instead of xsd:integer
                    rdfEquivalent = new RDFValue( smc.getValueFactory().createLiteral( "" + number.intValue(), XMLSchema.INTEGER ) );
					break;
				case LONG:
					rdfEquivalent = new RDFValue( smc.getValueFactory().createLiteral( number.longValue() ) );
					break;
				case DOUBLE:
					rdfEquivalent = new RDFValue( smc.getValueFactory().createLiteral( number.doubleValue() ) );
					break;
				case FLOAT:
					rdfEquivalent = new RDFValue( smc.getValueFactory().createLiteral( number.floatValue() ) );
					break;
                case DECIMAL:
                    rdfEquivalent = new RDFValue( smc.getValueFactory().createLiteral( number.toString(), XMLSchema.DECIMAL ) );
                    break;
            }
		}
	
		return rdfEquivalent;
	}
	
	public NumericValue abs()
	{
		NumericValue a = this;

        switch ( a.getDatatype() )
        {
            case INTEGER:
                return new SesameNumericValue( Math.abs( a.intValue() ) );
            case LONG:
                return new SesameNumericValue( Math.abs( a.longValue() ) );
            case FLOAT:
                return new SesameNumericValue( Math.abs( a.floatValue() ) );
            case DOUBLE:
                return new SesameNumericValue( Math.abs( a.doubleValue() ) );
            case DECIMAL:
                return new SesameNumericValue( a.decimalValue().abs() );
            default:
                // Shouldn't happen.
                return null;
        }
	}

	public NumericValue neg()
	{
		NumericValue a = this;

        switch ( a.getDatatype() )
        {
            case INTEGER:
                return new SesameNumericValue( -a.intValue() );
            case LONG:
                return new SesameNumericValue( -a.longValue() );
            case FLOAT:
                return new SesameNumericValue( -a.floatValue() );
            case DOUBLE:
                // Note: avoids negative zero.
                return new SesameNumericValue( 0.0 - a.doubleValue() );
            case DECIMAL:
                return new SesameNumericValue( a.decimalValue().negate() );
            default:
                // Shouldn't happen.
                return null;
        }
	}

    public NumericValue add( final NumericValue b )
	{
		NumericValue a = this;

        Type precision = maxPrecision( a, b );
        switch ( precision )
        {
            case INTEGER:
                return new SesameNumericValue( a.intValue() + b.intValue() );
            case LONG:
                return new SesameNumericValue( a.longValue() + b.longValue() );
            case FLOAT:
                return new SesameNumericValue( a.floatValue() + b.floatValue() );
            case DOUBLE:
                return new SesameNumericValue( a.doubleValue() + b.doubleValue() );
            case DECIMAL:
                return new SesameNumericValue( a.decimalValue().add( b.decimalValue() ) );
            default:
                // Shouldn't happen.
                return null;
        }
	}

	public NumericValue sub( final NumericValue b )
	{
		NumericValue a = this;

        Type precision = maxPrecision( a, b );
        switch ( precision )
        {
            case INTEGER:
                return new SesameNumericValue( a.intValue() - b.intValue() );
            case LONG:
                return new SesameNumericValue( a.longValue() - b.longValue() );
            case FLOAT:
                return new SesameNumericValue( a.floatValue() - b.floatValue() );
            case DOUBLE:
                return new SesameNumericValue( a.doubleValue() - b.doubleValue() );
            case DECIMAL:
                return new SesameNumericValue( a.decimalValue().subtract( b.decimalValue() ) );
            default:
                // Shouldn't happen.
                return null;
        }
	}

	public NumericValue mul( final NumericValue b )
	{
		NumericValue a = this;

        Type precision = maxPrecision( a, b );
        switch ( precision )
        {
            case INTEGER:
                return new SesameNumericValue( a.intValue() * b.intValue() );
            case LONG:
                return new SesameNumericValue( a.longValue() * b.longValue() );
            case FLOAT:
                return new SesameNumericValue( a.floatValue() * b.floatValue() );
            case DOUBLE:
                return new SesameNumericValue( a.doubleValue() * b.doubleValue() );
            case DECIMAL:
                return new SesameNumericValue( a.decimalValue().multiply( b.decimalValue() ) );
            default:
                // Shouldn't happen.
                return null;
        }
	}

	// Note: does not check for divide-by-zero.
	public NumericValue div( final NumericValue b )
	{
		NumericValue a = this;

        Type precision = maxPrecision( a, b );
        switch ( precision )
        {
            case INTEGER:
                return new SesameNumericValue( a.intValue() / b.intValue() );
            case LONG:
                return new SesameNumericValue( a.longValue() / b.longValue() );
            case FLOAT:
                return new SesameNumericValue( a.floatValue() / b.floatValue() );
            case DOUBLE:
                return new SesameNumericValue( a.doubleValue() / b.doubleValue() );
            case DECIMAL:
                return new SesameNumericValue( a.decimalValue().divide( b.decimalValue() ) );
            default:
                // Shouldn't happen.
                return null;
        }
	}

	// Note: does not check for divide-by-zero.
	public NumericValue mod( final NumericValue b )
	{
		NumericValue a = this;

        Type precision = maxPrecision( a, b );
        switch ( precision )
        {
            case INTEGER:
                return new SesameNumericValue( a.intValue() % b.intValue() );
            case LONG:
                return new SesameNumericValue( a.longValue() % b.longValue() );
            case FLOAT:
                return new SesameNumericValue( a.floatValue() % b.floatValue() );
            case DOUBLE:
                return new SesameNumericValue( a.doubleValue() % b.doubleValue() );
            case DECIMAL:
                return new SesameNumericValue( a.decimalValue().remainder( b.decimalValue() ).abs() );
            default:
                // Shouldn't happen.
                return null;
        }
	}

	public NumericValue pow( final NumericValue pow )
	{
//System.out.println("this = " + this + " (type = " + this.getType() + "), pow = " + pow + " (type = " + pow.getType() + ")");
        NumericValue a = this;

        if ( Type.DECIMAL == a.getDatatype() && Type.INTEGER == pow.getDatatype() )
        {
            return new SesameNumericValue( a.decimalValue().pow( pow.intValue() ) );
        }

        else
        {
            double r = Math.pow( a.doubleValue(), pow.doubleValue() );
//System.out.println("    r = " + r);
            Type precision = maxPrecision( a, pow );
//System.out.println("    precision = " + precision);
            switch ( precision )
            {
                case INTEGER:
                    return new SesameNumericValue( (int) r );
                case LONG:
                    return new SesameNumericValue( (long) r );
                case FLOAT:
                    return new SesameNumericValue( (float) r );
                case DOUBLE:
                    return new SesameNumericValue( r );
                case DECIMAL:
                    return new SesameNumericValue( r );
                default:
                    // Shouldn't happen.
                    return null;
            }
        }
	}
}

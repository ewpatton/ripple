package net.fortytwo.ripple.libs.math;

import net.fortytwo.ripple.test.RippleTestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class NegTest extends RippleTestCase
{
    public void testSingleSolution() throws Exception
    {
        assertReducesTo( "1 neg.", "-1" );
        assertReducesTo( "0.01 neg.", "-0.01" );
        assertReducesTo( "0 neg.", "0" );
        assertReducesTo( "42.2 neg.", "-42.2" );
    }

    public void testSpecialValues() throws Exception
    {
        assertReducesTo( "\"NaN\"^^xsd:double neg.", "\"NaN\"^^xsd:double" );
        assertReducesTo( "\"INF\"^^xsd:double neg.", "\"-INF\"^^xsd:double" );
        assertReducesTo( "\"-INF\"^^xsd:double neg.", "\"INF\"^^xsd:double" );
    }

    public void testInverse() throws Exception
    {
        assertReducesTo( "1 neg~.", "-1" );
        assertReducesTo( "0.01 neg~.", "-0.01" );
        assertReducesTo( "0 neg~.", "0" );
        assertReducesTo( "42.2 neg~.", "-42.2" );
    }
}

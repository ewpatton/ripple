package net.fortytwo.ripple.libs.math;

import net.fortytwo.ripple.test.RippleTestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class CoshTest extends RippleTestCase
{
    public void testSingleSolution() throws Exception
    {
        assertReducesTo( "0 cosh.", "1" );
        assertReducesTo( "-1 cosh.", "1.543080634815244" );
        assertReducesTo( "1 cosh.", "1.543080634815244" );
    }

    public void testSpecialValues() throws Exception
    {
        assertReducesTo( "\"NaN\"^^xsd:double cosh.", "\"NaN\"^^xsd:double" );
        assertReducesTo( "\"INF\"^^xsd:double cosh.", "\"INF\"^^xsd:double" );
        assertReducesTo( "\"-INF\"^^xsd:double cosh.", "\"INF\"^^xsd:double" );
    }

    public void testInverse() throws Exception
    {
        // TODO: implement inverse mapping
    }
}

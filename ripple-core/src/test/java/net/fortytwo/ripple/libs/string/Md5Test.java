package net.fortytwo.ripple.libs.string;

import net.fortytwo.ripple.test.RippleTestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Md5Test extends RippleTestCase
{
    public void testAll() throws Exception
    {
        assertReducesTo( "\"mailto:josh@fortytwo.net\" md5.", "\"64af9cc78fee2cc44f6109df1c2442d5\"" );
        assertReducesTo( "\"foo\" md5.", "\"acbd18db4cc2f85cedef654fccc4a4d8\"" );
        assertReducesTo( "\"acbd18db4cc2f85cedef654fccc4a4d8\" md5.", "\"beb61ddcd9a912dbea834dfebdd84411\"" );

        // Empty string
        assertReducesTo( "\"\" md5.", "\"d41d8cd98f00b204e9800998ecf8427e\"" );
    }
}

package net.fortytwo.ripple.libs.math;

import net.fortytwo.ripple.test.RippleTestCase;

/**
 * Author: josh
 * Date: May 3, 2008
 * Time: 1:42:20 PM
 */
public class CbrtTest extends RippleTestCase
{
    public void testSingleSolution() throws Exception
    {
        assertReducesTo( "1 cbrt >>", "1" );
        assertReducesTo( "-1 cbrt >>", "-1" );
        assertReducesTo( "0.125 cbrt >>", "0.5" );
    }

    public void testSpecialValues() throws Exception
    {
        assertReducesTo( "\"NaN\"^^xsd:double cbrt >>", "\"NaN\"^^xsd:double" );
        assertReducesTo( "\"INF\"^^xsd:double cbrt >>", "\"INF\"^^xsd:double" );
        assertReducesTo( "\"-INF\"^^xsd:double cbrt >>", "\"-INF\"^^xsd:double" );
    }

    public void testInverse() throws Exception
    {
        assertReducesTo( "0.5 cbrt <<", "0.125" );
        assertReducesTo( "-1 cbrt <<", "-1" );
    }
}
package net.fortytwo.ripple.model.impl.sesame;

import net.fortytwo.ripple.test.NewRippleTestCase;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.RippleList;

import java.util.Comparator;

/**
 * Author: josh
 * Date: May 3, 2008
 * Time: 1:42:20 PM
 */
public class RippleValueComparatorTest extends NewRippleTestCase
{
    public void testLists() throws Exception
    {
        ModelConnection mc = getTestModel().getConnection("");
        Comparator<RippleValue> comparator = mc.getComparator();

        RippleValue
				minusone = mc.value( -1.0 ),
				one = mc.value( 1 ),
				two = mc.value( 2 );

        assertEquals( 0, comparator.compare(
                createStack( mc, one, two ),
                createStack( mc, one, two ) ) );
        assertEquals( -1, comparator.compare(
                createStack( mc, one ),
                createStack( mc, one, two ) ) );

        // ...

        mc.close();
    }

    // ...
}
/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2008 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.etc;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.Library;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.LibraryLoader;
import net.fortytwo.ripple.URIMap;

/**
 * A collection of miscellaneous primitives.
 */
public class EtcLibrary extends Library
{
    public static final String
            NS_2008_08 = "http://fortytwo.net/2008/08/ripple/etc#",
            NS_2007_08 = "http://fortytwo.net/2007/08/ripple/etc#",
            NS_2007_05 = "http://fortytwo.net/2007/05/ripple/etc#";

    private static PrimitiveStackMapping invertVal;

    public void load( final URIMap uf,
                      final LibraryLoader.Context context )
		throws RippleException
	{
		uf.put( NS_2008_08, getClass().getResource( "etc.ttl" ) + "#" );

        registerPrimitives( context,
                DateTimeToMillis.class,
                Get.class,
                Time.class );
        invertVal = registerPrimitive( Invert.class, context );
	}

    public static PrimitiveStackMapping getInvertValue()
    {
        return invertVal;
    }
}


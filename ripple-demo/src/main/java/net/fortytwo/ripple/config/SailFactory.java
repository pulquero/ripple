package net.fortytwo.ripple.config;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.URIMap;
import org.eclipse.rdf4j.sail.Sail;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface SailFactory {
    Class getSailClass();

    Sail createSail(URIMap uriMap,
                    SailConfiguration config) throws RippleException;
}

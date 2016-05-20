package net.fortytwo.ripple.sail;

import net.fortytwo.ripple.model.RippleList;
import org.eclipse.rdf4j.model.Value;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface RippleSesameValue {
    RippleList getStack();

    void setStack(RippleList list);

    Value getNativeValue();
}

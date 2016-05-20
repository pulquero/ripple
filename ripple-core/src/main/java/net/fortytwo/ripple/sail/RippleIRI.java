package net.fortytwo.ripple.sail;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

import net.fortytwo.ripple.model.RippleList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleIRI extends SimpleIRI implements RippleSesameValue {
    private RippleList list = null;

    public RippleIRI(String uriString) {
        super(uriString);
    }

    @Override
    public RippleList getStack() {
        return list;
    }

    @Override
    public void setStack(final RippleList list) {
        this.list = list;
    }

    @Override
    public Value getNativeValue() {
        return this;
    }
}

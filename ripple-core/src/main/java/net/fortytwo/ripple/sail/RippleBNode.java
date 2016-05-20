package net.fortytwo.ripple.sail;

import net.fortytwo.ripple.model.RippleList;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.BNodeImpl;

import java.util.UUID;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleBNode extends BNodeImpl implements RippleSesameValue {
    private RippleList list = null;

    public RippleBNode() {
        super(UUID.randomUUID().toString());
    }

    public RippleBNode(String id) {
        super(id);
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

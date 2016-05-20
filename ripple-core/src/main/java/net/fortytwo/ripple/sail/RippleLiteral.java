package net.fortytwo.ripple.sail;

import java.util.Random;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;

import net.fortytwo.ripple.model.RippleList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleLiteral extends SimpleLiteral implements RippleSesameValue, BNode {
    private RippleList list = null;

    public RippleLiteral(String label) {
        super(label);
    }

    public RippleLiteral(String label, String language) {
        super(label, language);
    }

    public RippleLiteral(String label, IRI datatype) {
        super(label, datatype);
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
        return null != this.getLanguage()
                ? new LiteralImpl(this.getLabel(), this.getLanguage())
                : null != this.getDatatype()
                ? new LiteralImpl(this.getLabel(), this.getDatatype())
                : new LiteralImpl(this.getLabel());
    }

    private static final Random random = new Random();

    @Override
    public String getID() {
        return "rl" + random.nextInt(Integer.MAX_VALUE);
    }
}

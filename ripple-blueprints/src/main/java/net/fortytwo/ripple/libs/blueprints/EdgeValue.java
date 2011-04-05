package net.fortytwo.ripple.libs.blueprints;

import com.tinkerpop.blueprints.pgm.Edge;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.io.RipplePrintStream;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.RDFValue;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.keyval.KeyValueValue;

import java.util.Collection;

/**
 * User: josh
 * Date: 4/5/11
 * Time: 9:05 PM
 */
public class EdgeValue implements KeyValueValue {
    private final Edge edge;

    public EdgeValue(final Edge edge) {
        this.edge = edge;
    }

    public Edge getEdge() {
        return edge;
    }

    @Override
    public RDFValue toRDF(final ModelConnection mc) throws RippleException {
        // FIXME: add a data type
        return mc.value("[edge " + edge.getId() + "]");
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void printTo(final RipplePrintStream p) throws RippleException {
        p.print("[edge " + edge.getId() + "]");
    }

    @Override
    public RippleValue getValue(final String key,
                                final ModelConnection mc) throws RippleException {
        Object result = edge.getProperty(key);
        return null == result ? null : BlueprintsLibrary.createRippleValue(result, mc);
    }

    @Override
    public Collection<String> getKeys() {
        return edge.getPropertyKeys();
    }
}
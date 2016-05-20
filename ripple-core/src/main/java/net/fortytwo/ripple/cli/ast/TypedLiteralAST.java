package net.fortytwo.ripple.cli.ast;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryEngine;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TypedLiteralAST implements AST<RippleList> {
    private final String value;
    private final AST<RippleList> type;

    public TypedLiteralAST(final String value, final AST<RippleList> type) {
        this.value = value;
        this.type = type;
    }

    public void evaluate(final Sink<RippleList> sink,
                         final QueryEngine qe,
                         final ModelConnection mc)
            throws RippleException {
        Sink<RippleList> typeSink = new Sink<RippleList>() {
            public void put(final RippleList l) throws RippleException {
                Object type = l.getFirst();
                Value t = mc.toRDF(type);
                if (t instanceof IRI) {
                    sink.put(mc.list().push(mc.valueOf(value, (IRI) t)));
                } else {
                    qe.getErrorPrintStream().println("datatype does not map to a URI reference: " + type);
                }
            }
        };

        type.evaluate(typeSink, qe, mc);
    }

    public String toString() {
        return "\"" + value + "\"^^" + type;
    }
}


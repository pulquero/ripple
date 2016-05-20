package net.fortytwo.flow.rdf;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

/**
 * An RdfSink which passes its input into an RDFHandler.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SesameOutputAdapter implements RDFSink {
    private RDFHandler handler;

    private final Sink<Statement> stSink;
    private final Sink<Namespace> nsSink;
    private final Sink<String> cmtSink;

    public SesameOutputAdapter(final RDFHandler handler) {
        this.handler = handler;

        stSink = new Sink<Statement>() {
            public void put(final Statement st) throws RippleException {
                try {
                    handler.handleStatement(st);
                } catch (RDFHandlerException e) {
                    throw new RippleException(e);
                }
            }
        };

        nsSink = new Sink<Namespace>() {
            public void put(final Namespace ns) throws RippleException {
                try {
                    handler.handleNamespace(ns.getPrefix(), ns.getName());
                } catch (RDFHandlerException e) {
                    throw new RippleException(e);
                }
            }
        };

        cmtSink = new Sink<String>() {
            public void put(final String comment) throws RippleException {
                try {
                    handler.handleComment(comment);
                } catch (RDFHandlerException e) {
                    throw new RippleException(e);
                }
            }
        };
    }

    public void startRDF() throws RDFHandlerException {
        handler.startRDF();
    }

    public void endRDF() throws RDFHandlerException {
        handler.endRDF();
    }

    public Sink<Statement> statementSink() {
        return stSink;
    }

    public Sink<Namespace> namespaceSink() {
        return nsSink;
    }

    public Sink<String> commentSink() {
        return cmtSink;
    }
}


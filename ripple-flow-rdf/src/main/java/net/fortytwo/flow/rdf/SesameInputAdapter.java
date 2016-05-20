package net.fortytwo.flow.rdf;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.InputStream;

/**
 * An RDFHandler which passes its input into an RdfSink.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SesameInputAdapter implements RDFHandler {
    private final Sink<Statement> stSink;
    private final Sink<Namespace> nsSink;
    private final Sink<String> cmtSink;

    public SesameInputAdapter(final RDFSink sink) {
        stSink = sink.statementSink();
        nsSink = sink.namespaceSink();
        cmtSink = sink.commentSink();
    }

    /**
     * Handles a comment.
     */
    public void handleComment(final String comment) {
//System.out.println( "handleComment(\"" + comment + "\")" );
        try {
            cmtSink.put(comment);
        } catch (Exception e) {
            // Log the error, but continue.
            logError(e);
        }
    }

    /**
     * Handles a namespace declaration/definition.
     */
    public void handleNamespace(final String prefix, final String uri) {
//System.out.println( "handleNamespace(" + prefix + ", " + uri + ")" );
        try {
            nsSink.put(new NamespaceImpl(prefix, uri));
        } catch (Exception e) {
            // Log the error, but continue.
            logError(e);
        }
    }

    /**
     * Handles a statement.
     */
    public void handleStatement(final Statement st) {
//System.out.println( "handleStatement(" + st + ")" );
        try {
            stSink.put(st);
        } catch (Exception e) {
            // Log the error, but continue.
            logError(e);
        }
    }

    /**
     * Signals the start of the RDF data.
     */
    public void startRDF() {
    }

    /**
     * Signals the end of the RDF data.
     */
    public void endRDF() {
    }

    private void logError(Exception e) {
        // FIXME
        System.err.println("Error: " + e);
    }

    public static RDFFormat parse(final InputStream is,
                                  final RDFSink sink,
                                  final String baseUri,
                                  final RDFFormat format) throws RippleException {
        RDFParser parser = Rio.createParser(format);
        parser.setRDFHandler(new SesameInputAdapter(sink));

        try {
            parser.parse(is, baseUri);
        } catch (IOException e) {
            throw new RippleException(e);
        } catch (RDFParseException e) {
            throw new RippleException(e);
        } catch (RDFHandlerException e) {
            throw new RippleException(e);
        }

        return format;
    }
}


package net.fortytwo.flow.rdf;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SailInserter implements RDFHandler {
    private final SailConnection sailConnection;

    public SailInserter(final SailConnection sailConnection) {
        this.sailConnection = sailConnection;
    }

    public void startRDF() throws RDFHandlerException {
    }

    public void endRDF() throws RDFHandlerException {
    }

    public void handleNamespace(final String prefix, final String uri) throws RDFHandlerException {
        try {
            sailConnection.setNamespace(prefix, uri);
        } catch (SailException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleStatement(final Statement st) throws RDFHandlerException {
        try {
            sailConnection.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
        } catch (SailException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleComment(final String comment) throws RDFHandlerException {
        // Do nothing.
    }
}

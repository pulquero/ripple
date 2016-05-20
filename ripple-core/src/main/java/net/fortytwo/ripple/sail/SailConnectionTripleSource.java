package net.fortytwo.ripple.sail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import net.fortytwo.ripple.RippleException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SailConnectionTripleSource implements TripleSource {
    private static final Logger logger = Logger.getLogger(SailConnectionTripleSource.class.getName());

    private final SailConnection sailConnection;
    private final ValueFactory valueFactory;
    private final boolean includeInferred;

    public SailConnectionTripleSource(final SailConnection conn,
                                      final ValueFactory valueFactory,
                                      final boolean includeInferred) {
        sailConnection = conn;
        this.valueFactory = valueFactory;
        this.includeInferred = includeInferred;
    }

    public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(
            final Resource subj, final IRI pred, final Value obj, final Resource... contexts) {
        try {
            return new QueryEvaluationIteration(
                    sailConnection.getStatements(subj, pred, obj, includeInferred, contexts));
        } catch (SailException e) {
            logger.log(Level.WARNING, "query evaluation failed", e);
            return new EmptyCloseableIteration<Statement, QueryEvaluationException>();
        }
    }

    public ValueFactory getValueFactory() {
        return valueFactory;
    }
}

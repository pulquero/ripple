package net.fortytwo.ripple.sail;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.ripple.RippleException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

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
            final Resource subj, final URI pred, final Value obj, final Resource... contexts) {
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

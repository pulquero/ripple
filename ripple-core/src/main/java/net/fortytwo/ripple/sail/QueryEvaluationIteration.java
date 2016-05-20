package net.fortytwo.ripple.sail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class QueryEvaluationIteration implements CloseableIteration<Statement, QueryEvaluationException> {
    private final CloseableIteration<? extends Statement, SailException> baseIteration;

    public QueryEvaluationIteration(final CloseableIteration<? extends Statement, SailException> baseIteration) {
        this.baseIteration = baseIteration;
    }

    public void close() throws QueryEvaluationException {
        try {
            baseIteration.close();
        } catch (SailException e) {
            throw new QueryEvaluationException(e);
        }
    }

    public boolean hasNext() throws QueryEvaluationException {
        try {
            return baseIteration.hasNext();
        } catch (SailException e) {
            throw new QueryEvaluationException(e);
        }
    }

    public Statement next() throws QueryEvaluationException {
        try {
            return baseIteration.next();
        } catch (SailException e) {
            throw new QueryEvaluationException(e);
        }
    }

    public void remove() throws QueryEvaluationException {
        try {
            baseIteration.remove();
        } catch (SailException e) {
            throw new QueryEvaluationException(e);
        }
    }
}

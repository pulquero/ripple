package net.fortytwo.linkeddata.sail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SailConnectionTripleSource implements TripleSource {
    private static final Logger logger = LoggerFactory.getLogger(SailConnectionTripleSource.class.getName());
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
            logger.warn("query evaluation failed", e);
            return new EmptyCloseableIteration<Statement, QueryEvaluationException>();
        }
    }

    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    private static class QueryEvaluationIteration implements CloseableIteration<Statement, QueryEvaluationException> {
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

    private static class EmptyCloseableIteration<T, E extends Exception> implements CloseableIteration<T, E> {
        public void close() throws E {
        }

        public boolean hasNext() throws E {
            return false;
        }

        public T next() throws E {
            return null;
        }

        public void remove() throws E {
        }
    }
}

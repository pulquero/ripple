package net.fortytwo.linkeddata.sail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.QueryRoot;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.BindingAssigner;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.CompareOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.ConstantOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.FilterOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailConnectionListener;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;
import org.eclipse.rdf4j.sail.helpers.NotifyingSailConnectionBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortytwo.linkeddata.LinkedDataCache;
import net.fortytwo.ripple.RippleException;

/**
 * A connection to a LinkedDataSail
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LinkedDataSailConnection extends NotifyingSailConnectionBase {

    private static final Logger logger = LoggerFactory.getLogger(LinkedDataSailConnection.class.getName());

    private final ValueFactory valueFactory;
    private final LinkedDataCache linkedDataCache;
    private final FederatedServiceResolver federatedServiceResolver;

    private SailConnection baseConnection;

    public synchronized void addConnectionListener(final SailConnectionListener listener) {
        if (baseConnection instanceof NotifyingSailConnection) {
            ((NotifyingSailConnection) baseConnection).addConnectionListener(listener);
        }
    }

    protected void addStatementInternal(final Resource subj,
                                        final IRI pred,
                                        final Value obj,
                                        final Resource... contexts) throws SailException {
        baseConnection.addStatement(subj, pred, obj, contexts);
    }

    protected void clearInternal(final Resource... contexts) throws SailException {
        baseConnection.clear(contexts);
    }

    protected void clearNamespacesInternal() throws SailException {
        baseConnection.clearNamespaces();
    }

    protected synchronized void closeInternal() throws SailException {
        baseConnection.rollback();
        baseConnection.close();
    }

    protected void commitInternal() throws SailException {
        baseConnection.commit();
    }

    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
            TupleExpr tupleExpr,
            final Dataset dataset,
            final BindingSet bindings,
            final boolean includeInferred)
            throws SailException {
		// Clone the tuple expression to allow for more aggresive optimizations
		tupleExpr = tupleExpr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimizers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		// Decompose queries into getStatements operations so we can dereference IRIs.
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this, valueFactory, includeInferred);
            EvaluationStrategy strategy = new SimpleEvaluationStrategy(tripleSource, dataset, federatedServiceResolver);

			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
			new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
			new QueryJoinOptimizer(new EvaluationStatistics()).optimize(tupleExpr, dataset, bindings);
			// new SubSelectJoinOptimizer().optimize(tupleExpr, dataset,
			// bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
            throws SailException {
        return baseConnection.getContextIDs();
    }

    protected String getNamespaceInternal(final String prefix)
            throws SailException {
        return baseConnection.getNamespace(prefix);
    }

    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
            throws SailException {
        return baseConnection.getNamespaces();
    }

    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            final Resource subj,
            final IRI pred,
            final Value obj,
            final boolean includeInferred,
            final Resource... contexts) throws SailException {

        // Pull in any data needed for this query.
        extendClosureToStatement(subj, pred, obj, contexts);

        // Now match the triple pattern.
        return baseConnection.getStatements(subj, pred, obj, includeInferred, contexts);
    }

    public synchronized void removeConnectionListener(final SailConnectionListener listener) {
        if (baseConnection instanceof NotifyingSailConnection) {
            ((NotifyingSailConnection) baseConnection).removeConnectionListener(listener);
        }
    }

    protected void removeNamespaceInternal(final String prefix)
            throws SailException {
        baseConnection.removeNamespace(prefix);
    }

    protected void removeStatementsInternal(final Resource subj,
                                            final IRI pred,
                                            final Value obj,
                                            final Resource... context) throws SailException {
        baseConnection.removeStatements(subj, pred, obj, context);
    }

    protected void rollbackInternal() throws SailException {
        baseConnection.rollback();
        //baseConnection.begin();
    }

    protected void setNamespaceInternal(final String prefix, final String name)
            throws SailException {
        baseConnection.setNamespace(prefix, name);
    }

    protected long sizeInternal(final Resource... contexts) throws SailException {
        return baseConnection.size(contexts);
    }

    protected void startTransactionInternal() throws SailException {
        baseConnection.begin();
    }

    LinkedDataSailConnection(final AbstractSail sail,
                             final Sail baseSail,
                             final LinkedDataCache linkedDataCache,
                             FederatedServiceResolver federateServiceResolver) throws SailException {
        super(sail);
        this.linkedDataCache = linkedDataCache;

        // Inherit the local store's ValueFactory
        valueFactory = baseSail.getValueFactory();

        baseConnection = baseSail.getConnection();
        this.federatedServiceResolver = federateServiceResolver;
    }

    private void retrieveIRI(final IRI IRI) {
        try {
            linkedDataCache.retrieve(IRI, baseConnection);
        } catch (RippleException e) {
            logger.error("failed to retrieve IRI", e);
        }
    }

    private void extendClosureToStatement(final Resource subj,
                                          final IRI pred,
                                          final Value obj,
                                          final Resource... contexts) throws SailException {
        if (linkedDataCache.getDereferenceSubjects() && null != subj && subj instanceof IRI) {
            retrieveIRI((IRI) subj);
        }

        if (linkedDataCache.getDereferencePredicates() && null != pred) {
            retrieveIRI(pred);
        }

        if (linkedDataCache.getDereferenceObjects() && null != obj && obj instanceof IRI) {
            retrieveIRI((IRI) obj);
        }

        if (linkedDataCache.getDereferenceContexts()) {
            for (Resource ctx : contexts) {
                if (null != ctx && ctx instanceof IRI) {
                    retrieveIRI((IRI) ctx);
                }
            }
        }
    }
}


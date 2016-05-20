package net.fortytwo.linkeddata.sail;

import java.io.File;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.eclipse.rdf4j.sail.NotifyingSail;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailChangedListener;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.StackableSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;

import net.fortytwo.linkeddata.LinkedDataCache;
import net.fortytwo.ripple.RippleException;

/**
 * A dynamic storage layer which treats the Semantic Web as a single global graph of linked data.
 * LinkedDataSail is layered on top of another Sail which serves as a database for cached Semantic Web documents.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LinkedDataSail extends AbstractSail implements StackableSail, NotifyingSail, FederatedServiceResolverClient {
    public static final String
            CACHE_LIFETIME = "net.fortytwo.linkeddata.cacheLifetime",
            DATATYPE_HANDLING_POLICY = "net.fortytwo.linkeddata.datatypeHandlingPolicy",
            MEMORY_CACHE_CAPACITY = "net.fortytwo.linkeddata.memoryCacheCapacity";

    private final LinkedDataCache cache;

    private Sail baseSail;

    private FederatedServiceResolver serviceResolver = new FederatedServiceResolverImpl();

    public LinkedDataSail(LinkedDataCache cache)
    {
    	this.cache = cache;
    }

    /**
     * @param baseSail base Sail which provides a storage layer for aggregated RDF data.
     *                 Note: the base Sail should be initialized before this Sail is used.
     * @param cache    a custom WebClosure providing an RDF-document-level view of the Web
     * @throws RippleException if there is a configuration error
     */
    public LinkedDataSail(final Sail baseSail,
                          final LinkedDataCache cache)
            throws RippleException {
        this.baseSail = baseSail;

        this.cache = cache;
    }

    /**
     * @param baseSail base Sail which provides a storage layer for aggregated RDF data.
     *                 Note: the base Sail should be initialized before this Sail is used.
     * @throws RippleException if there is a configuration error
     */
    public LinkedDataSail(final Sail baseSail)
            throws RippleException {
        this(baseSail, LinkedDataCache.createDefault(baseSail));
    }

    public void addSailChangedListener(final SailChangedListener listener) {
        if (baseSail instanceof NotifyingSail) {
            ((NotifyingSail) baseSail).addSailChangedListener(listener);
        }
    }

    @Override
    public NotifyingSailConnection getConnection() throws SailException {
        SailConnection sc = super.getConnection();
        return (NotifyingSailConnection) sc;
    }

    protected SailConnection getConnectionInternal() throws SailException {
        return new LinkedDataSailConnection(this, baseSail, cache, serviceResolver);
    }

    public File getDataDir() {
		return baseSail.getDataDir();
    }

    public ValueFactory getValueFactory() {
        // Inherit the base Sail's ValueFactory
        return baseSail.getValueFactory();
    }

    protected void initializeInternal() throws SailException {
    	baseSail.initialize();
        try {
			cache.initialize(baseSail);
			LinkedDataCache.configure(cache);
		} catch (RippleException e) {
			throw new SailException(e);
		}
    }

    public boolean isWritable() throws SailException {
        // You may write to LinkedDataSail, but be sure not to interfere with caching metadata.
        return true;
    }

    public void removeSailChangedListener(final SailChangedListener listener) {
        if (baseSail instanceof NotifyingSail) {
            ((NotifyingSail) baseSail).removeSailChangedListener(listener);
        }
    }

    public void setDataDir(final File dataDir) {
		baseSail.setDataDir(dataDir);
    }

    protected void shutDownInternal() throws SailException {
        try {
            cache.close();
        } catch (RippleException e) {
            // note: SailException --> RippleException --> SailException
            throw new SailException(e);
        }

        // Do not shut down the base Sail.
    }

    public Sail getBaseSail() {
        return baseSail;
    }

    public void setBaseSail(final Sail baseSail) {
        this.baseSail = baseSail;
    }

	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		serviceResolver = resolver;
		if (baseSail instanceof FederatedServiceResolverClient) {
			((FederatedServiceResolverClient)baseSail).setFederatedServiceResolver(resolver);
		}
	}

    /**
     * @return this LinkedDataSail's cache manager
     */
    public LinkedDataCache getCache() {
        return cache;
    }
}

package net.fortytwo.ripple.sail;

import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.Model;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.impl.sesame.SesameModel;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleSail implements Sail, FederatedServiceResolverClient {

    private final RippleValueFactory valueFactory = new RippleValueFactory(new ValueFactoryImpl());
    private final Model model;

    private FederatedServiceResolver serviceResolver = new FederatedServiceResolverImpl();

    public RippleSail(final Model model) {
        this.model = model;
    }

    public RippleSail(final Sail base) throws RippleException {
        model = new SesameModel(base);
    }

    private boolean asynch;

    @Override
    public void initialize() throws SailException {
        // FIXME: cheat to temporarily disable asynchronous query answering
        asynch = Ripple.asynchronousQueries();
        Ripple.enableAsynchronousQueries(false);
    }

    @Override
    public void shutDown() throws SailException {
        Ripple.enableAsynchronousQueries(asynch);

        try {
            model.shutDown();
        } catch (RippleException e) {
            throw new SailException(e);
        }
        //base.shutDown();
    }

    @Override
    public SailConnection getConnection() throws SailException {
        ModelConnection mc;
        try {
            mc = model.createConnection();
        } catch (RippleException e) {
            throw new SailException(e);
        }
        return new RippleSailConnection(mc, valueFactory, serviceResolver);
    }

    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    @Override
    public void setDataDir(final File file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDataDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWritable() throws SailException {
        return true;
    }

	@Override
	public List<IsolationLevel> getSupportedIsolationLevels() {
		return Collections.<IsolationLevel>singletonList(IsolationLevels.NONE);
	}

	@Override
	public IsolationLevel getDefaultIsolationLevel() {
		return IsolationLevels.NONE;
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		this.serviceResolver = resolver;
	}
}

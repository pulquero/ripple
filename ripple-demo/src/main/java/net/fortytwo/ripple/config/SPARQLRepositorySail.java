package net.fortytwo.ripple.config;

import net.fortytwo.sesametools.reposail.RepositorySail;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SPARQLRepositorySail extends RepositorySail {
    public SPARQLRepositorySail(final String endpoint) throws SailException {
        super(createRepo(endpoint));
    }

    private static Repository createRepo(final String endpoint) throws SailException {
        Repository repo = new SPARQLRepository(endpoint);
        try {
            repo.initialize();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
        return repo;
    }
}

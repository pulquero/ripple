package net.fortytwo.linkeddata;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import junit.framework.TestCase;
import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.URIMap;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LinkedDataSailTest extends TestCase {
    private Sail baseSail;
    private LinkedDataSail sail;

    public void setUp() throws Exception {
        Ripple.initialize();

        baseSail = new MemoryStore();
        baseSail.initialize();

        URIMap map = new URIMap();

        // This is an example where HttpDereferencer fails by requesting the
        // full URI of a resource (rather than stripping off the local part).
        // Here, we define a mapping to a local file, so dereferencing
        // succeeds.
        map.put("http://www.holygoat.co.uk/owl/redwood/0.1/tags/Tagging",
                LinkedDataSailTest.class.getResource("tags.owl").toString());

        LinkedDataCache wc = LinkedDataCache.createDefault(baseSail);
        wc.setURIMap(map);
        sail = new LinkedDataSail(baseSail, wc);
        sail.initialize();
    }

    public void tearDown() throws Exception {
        sail.shutDown();
        baseSail.shutDown();
    }

    public void testDereferencer() throws Exception {
        long count;
        boolean includeInferred = false;
        ValueFactory vf = sail.getValueFactory();
        IRI tagging = vf.createIRI("http://www.holygoat.co.uk/owl/redwood/0.1/tags/Tagging");

        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            count = countStatements(sc.getStatements(tagging, RDF.TYPE, null, includeInferred));
            assertEquals(1, count);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    public void testCountStatements() throws Exception {
        ValueFactory vf = sail.getValueFactory();
        IRI ctxA = vf.createIRI("urn:org.example.test.countStatementsTest#");
        IRI uri1 = vf.createIRI("urn:org.example.test#uri1");
        IRI uri2 = vf.createIRI("urn:org.example.test#uri2");
        IRI uri3 = vf.createIRI("urn:org.example.test#uri3");
        IRI[] uris = {uri1, uri2, uri3};

        SailConnection sc = baseSail.getConnection();
        try {
            sc.begin();

            assertEquals(0, countStatements(sc, ctxA));
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        sc.addStatement(
                                uris[i], uris[j], uris[k], ctxA);
                    }
                }
            }
            sc.commit();
        } finally {
            sc.close();
        }

        sc = sail.getConnection();
        try {
            sc.begin();
            assertEquals(27, countStatements(sc, ctxA));
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    private static long countStatements(final CloseableIteration<? extends Statement, SailException> iter)
            throws SailException {
        long count;

        try {
            count = 0;
            while (iter.hasNext()) {
                iter.next();
                count++;
            }
        } finally {
            iter.close();
        }

        return count;
    }

    private static long countStatements(final SailConnection sc, final Resource... contexts)
            throws SailException {
        return countStatements(sc.getStatements(null, null, null, false, contexts));
    }

    // For debugging/experimentation
    public static void main(final String[] args) throws Exception {
        Ripple.initialize();

        Sail baseSail = new MemoryStore();
        baseSail.initialize();

        try {
            Repository repo = new SailRepository(baseSail);

            LinkedDataSail sail = new LinkedDataSail(baseSail);
            sail.initialize();
            try {
                SailConnection sc = sail.getConnection();
                try {
                    sc.begin();
                    sc.getStatements(sail.getValueFactory().createIRI("http://rdf.freebase.com/rdf/en.stephen_fry"), null, null, false);
                    sc.commit();
                } finally {
                    sc.close();
                }
            } finally {
                sail.shutDown();
            }

            RepositoryConnection rc = repo.getConnection();
            try {
                rc.export(new RDFHandler() {
                    public void startRDF() throws RDFHandlerException {
                    }

                    public void endRDF() throws RDFHandlerException {
                    }

                    public void handleNamespace(String s, String s1) throws RDFHandlerException {
                    }

                    public void handleStatement(Statement statement) throws RDFHandlerException {
                        System.out.println("" + statement);
                    }

                    public void handleComment(String s) throws RDFHandlerException {
                    }
                });
            } finally {
                rc.close();
            }
        } finally {
            baseSail.shutDown();
        }
    }
}

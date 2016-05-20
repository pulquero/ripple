package net.fortytwo.ripple.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.URIMap;
import net.fortytwo.ripple.model.Model;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.impl.sesame.SesameModel;
import net.fortytwo.ripple.query.LazyEvaluatingIterator;
import net.fortytwo.ripple.query.QueryEngine;
import net.fortytwo.ripple.query.QueryPipe;
import net.fortytwo.ripple.query.StackEvaluator;
import org.junit.After;
import org.junit.Before;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class RippleTestCase extends TestCase {
    // TODO: add a shutdown hook to clean up these objects
    private static Sail sail = null;
    private static URIMap uriMap = null;
    private static Model model = null;
    private static QueryEngine queryEngine = null;

    protected ModelConnection modelConnection = null;
    protected Comparator<Object> comparator = null;

    @Before
    public void setUp() throws Exception {
        modelConnection = getTestModel().createConnection();
        comparator = modelConnection.getComparator();

        SailConnection sc = getTestSail().getConnection();

        try {
            sc.begin();
            sc.clear();
            sc.commit();
        } finally {
            sc.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (null != modelConnection) {
            modelConnection.close();
            modelConnection = null;
        }
    }

    protected Sail getTestSail() throws RippleException {
        if (null == sail) {
            sail = new MemoryStore();

            try {
                sail.initialize();

                SailConnection sc = sail.getConnection();
                try {
                    sc.begin();
                    // Define some common namespaces
                    sc.setNamespace("rdf", RDF.NAMESPACE);
                    sc.setNamespace("rdfs", RDFS.NAMESPACE);
                    sc.setNamespace("xsd", XMLSchema.NAMESPACE);
                    sc.commit();
                } finally {
                    sc.close();
                }
            } catch (SailException e) {
                throw new RippleException(e);
            }
        }

        return sail;
    }

    protected URIMap getTestURIMap() {
        if (null == uriMap) {
            uriMap = new URIMap();
        }

        return uriMap;
    }

    protected Model getTestModel() throws RippleException {
        if (null == model) {
            Ripple.initialize();

            // Asynchronous queries can cause certain tests cases to fail, as
            // they are not set up to wait on other threads.
            Ripple.enableAsynchronousQueries(false);

            model = new SesameModel(getTestSail());
        }

        return model;
    }

    protected QueryEngine getTestQueryEngine() throws RippleException {
        if (null == queryEngine) {
            StackEvaluator eval = new LazyEvaluatingIterator.WrappingEvaluator();
            //StackEvaluator eval = new LazyStackEvaluator();
            queryEngine = new QueryEngine(getTestModel(), eval, System.out, System.err);
        }

        return queryEngine;
    }

    protected RippleList createStack(final ModelConnection mc,
                                     final Object... values) throws RippleException {
        if (0 == values.length) {
            return mc.list();
        }

        RippleList l = mc.list().push(values[0]);
        for (int i = 1; i < values.length; i++) {
            l = l.push(values[i]);
        }

        return l;
    }

    protected RippleList createQueue(final ModelConnection mc,
                                     final Object... values) throws RippleException {
        return createStack(mc, values).invert();
    }

    protected void assertCollectorsEqual(final Collector<RippleList> expected,
                                         final Collector<RippleList> actual) throws Exception {
        int size = expected.size();

        assertEquals("wrong number of results.", size, actual.size());
        if (0 == size) {
            return;
        }
//for (RippleList l : expected) {System.out.println("expected: " + l);}
//for (RippleList l : actual) {System.out.println("actual: " + l);}

        // Sort the results.
        RippleList[] expArray = new RippleList[size];
        RippleList[] actArray = new RippleList[size];
        Iterator<RippleList> expIter = expected.iterator();
        Iterator<RippleList> actIter = actual.iterator();
        for (int i = 0; i < size; i++) {
            expArray[i] = expIter.next();
            actArray[i] = actIter.next();
        }
        Arrays.sort(expArray, comparator);
        Arrays.sort(actArray, comparator);

        // Compare the results by pairs.
        for (int i = 0; i < size; i++) {
            assertRippleEquals(expArray[i], actArray[i]);
        }
    }

    protected void assertRippleEquals(final Object first, final Object second) throws Exception {
        int cmp = comparator.compare(first, second);
        if (0 != cmp) {
            throw new AssertionFailedError("expected <" + first + "> but was <" + second + ">");
        }
    }

    protected Collection<RippleList> reduce(final InputStream from) throws RippleException {
        Collector<RippleList>
                results = new Collector<RippleList>();

        QueryEngine qe = getTestQueryEngine();

        QueryPipe actualPipe = new QueryPipe(qe, results);
        actualPipe.put(from);
        actualPipe.close();

        Collection<RippleList> c = new LinkedList<RippleList>();
        for (RippleList result : results) {
            c.add(result);
        }

        return c;
    }

    protected void assertLegal(final String from) throws Exception {
        Collection<RippleList> result = reduce("(" + from + ")");
        assertTrue("expression is illegal: " + from, 1 == result.size());
    }

    protected void assertIllegal(final String from) throws Exception {
        Collection<RippleList> result = reduce("(" + from + ")");
        assertTrue("expression is legal: " + from, 0 == result.size());
    }

    protected Collection<RippleList> reduce(final String from) throws RippleException {
        Collector<RippleList>
                results = new Collector<RippleList>();

        QueryEngine qe = getTestQueryEngine();

        QueryPipe actualPipe = new QueryPipe(qe, results);
        actualPipe.put(from + "\n");
        actualPipe.close();

        Collection<RippleList> c = new LinkedList<RippleList>();
        for (RippleList result : results) {
            c.add(result);
        }

        return c;
    }

    protected void assertReducesTo(final String from, final String... to) throws Exception {
        Collector<RippleList>
                expected = new Collector<RippleList>(),
                actual = new Collector<RippleList>();

        QueryEngine qe = getTestQueryEngine();

        QueryPipe actualPipe = new QueryPipe(qe, actual);
        actualPipe.put(from + "\n");
        actualPipe.close();

        QueryPipe expectedPipe = new QueryPipe(qe, expected);
        for (String t : to) {
            expectedPipe.put(t + "\n");
        }
        expectedPipe.close();

        assertCollectorsEqual(expected, actual);
    }

    protected IRI createIRI(final String s,
                            final ModelConnection mc) throws RippleException {
        return mc.valueOf(java.net.URI.create(s));
    }
}

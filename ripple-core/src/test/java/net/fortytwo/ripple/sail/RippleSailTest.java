package net.fortytwo.ripple.sail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import junit.framework.TestCase;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleSailTest extends TestCase {
    private Sail baseSail;
    private Sail sail;
    private SailConnection sc;
    private SPARQLParser parser = new SPARQLParser();

    public final void setUp() throws Exception {
        baseSail = new MemoryStore();
        baseSail.initialize();
        sail = new RippleSail(baseSail);
        sail.initialize();

        addDummyData(baseSail);

        sc = sail.getConnection();
        sc.begin();
    }

    public final void tearDown() throws Exception {
        sc.rollback();
        sc.close();
        sail.shutDown();
        baseSail.shutDown();
    }

    private Collection<BindingSet> evaluate(final String queryStr) throws Exception {
        BindingSet bindings = new EmptyBindingSet();
        String baseURI = "http://example.org/baseUri#";
        ParsedQuery query;
        CloseableIteration<? extends BindingSet, QueryEvaluationException> results;
        query = parser.parseQuery(queryStr, baseURI);
        Collection<BindingSet> coll = new LinkedList<BindingSet>();
        results = sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, false);
        try {
            while (results.hasNext()) {
                coll.add(results.next());
            }
        } finally {
            results.close();
        }
        return coll;
    }

    public void testSparqlForward() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?f WHERE {\n" +
                "    :foo rdf:first ?f.\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("1", ((Literal) results.iterator().next().getValue("f")).getLabel());

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?f WHERE {\n" +
                "    :foo rdf:rest ?l.\n" +
                "    ?l rdf:first ?f.\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("2", ((Literal) results.iterator().next().getValue("f")).getLabel());
    }

    public void testLiteralIntermediates() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX control: <http://fortytwo.net/2013/03/ripple/control#>\n" +
                "PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "SELECT ?sum WHERE {\n" +
                "    :foo control:apply ?stack.\n" +
                "    ?stack math:add ?sum.\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("5", ((Literal) results.iterator().next().getValue("sum")).getLabel());
    }

    public void testLiteralSubjects() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "PREFIX stack: <http://fortytwo.net/2013/03/ripple/stack#>\n" +
                "SELECT DISTINCT ?result WHERE {\n" +
                "   ?x stack:self 100 .\n" +
                "   ?x math:sqrt/math:abs ?result .\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("10.0", ((Literal) results.iterator().next().getValue("result")).getLabel());
    }

    public void testLiteralPredicates() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX graph: <http://fortytwo.net/2013/03/ripple/graph#>\n" +
                "PREFIX stack: <http://fortytwo.net/2013/03/ripple/stack#>\n" +
                "PREFIX s: <urn:string:>\n" +
                "SELECT ?result WHERE {\n" +
                "   ?x stack:self \"{\\\"foo\\\": true, \\\"bar\\\": [6, 9, 42]}\" .\n" +
                "   ?x graph:to-json/s:foo ?result .\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("true", ((Literal) results.iterator().next().getValue("result")).getLabel());
    }

    public void testSparqlPropertyPaths() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?f WHERE {\n" +
                "    :foo rdf:rest/rdf:first ?f.\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("2", ((Literal) results.iterator().next().getValue("f")).getLabel());

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?f WHERE {\n" +
                "    :foo rdf:rest*/rdf:first ?f.\n" +
                "}");
        assertEquals(3, results.size());

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "SELECT ?n WHERE {\n" +
                "    :foo rdf:rest/rdf:first/math:neg ?n.\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("-2", ((Literal) results.iterator().next().getValue("n")).getLabel());
    }

    public void testListDequotation() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "SELECT ?n WHERE {\n" +
                "    rdf:nil :foo/math:add ?n.\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("5", ((Literal) results.iterator().next().getValue("n")).getLabel());

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX control: <http://fortytwo.net/2013/03/ripple/control#>\n" +
                "PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "SELECT ?n WHERE {\n" +
                "    :foo control:apply/math:add ?n.\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("5", ((Literal) results.iterator().next().getValue("n")).getLabel());
    }

    public void testSparqlInverseProperties() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "SELECT ?n WHERE {\n" +
                "    :foo rdf:rest/rdf:rest/rdf:first/^math:sqrt ?n.\n" +
                "}");
        assertEquals(1, results.size());
        assertEquals("9", ((Literal) results.iterator().next().getValue("n")).getLabel());
    }

    public void testSparqlFilterEquals() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "SELECT ?n2 WHERE {\n" +
                "    :foo rdf:first ?n1 ." +
                "    ?n1 math:sqrt ?n2.\n" +
                "}");
        assertEquals(2, results.size());

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "SELECT ?n2 WHERE {\n" +
                "    :foo rdf:first ?n1 ." +
                "    ?n1 math:sqrt ?n2.\n" +
                "    FILTER(?n1 != ?n2)\n" +
                "}");
        assertEquals(1, results.size());
    }

    public void testEquality() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?l WHERE {\n" +
                "    ?l rdf:first 2 ." +
                "}");
        assertEquals(2, results.size());

        // There are still two results, because resources are distinct by reference, not value.
        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT DISTINCT ?l WHERE {\n" +
                "    ?l rdf:first 2 ." +
                "}");
        assertEquals(2, results.size());
    }

    // Comparison follows SPARQL semantics.
    public void testComparison() throws Exception {
        Collection<BindingSet> results;

        results = evaluate("PREFIX : <http://example.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX math: <http://fortytwo.net/2013/03/ripple/math#>\n" +
                "SELECT ?n WHERE {\n" +
                "    :foo rdf:first/math:sqrt ?n.\n" +
                "    FILTER(?n > 0)\n" +
                "}");
        assertEquals(1, results.size());
    }

    /*
    public void testNothing() throws Exception {
        // This is necessary in order to avoid race conditions.
        Ripple.enableAsynchronousQueries(false);

        Model model = new SesameModel(baseSail);
        StackEvaluator eval = new LazyStackEvaluator();

        ModelConnection mc = model.getConnection();
        try {
            Collector<StackContext, RippleException> solutions = new Collector<StackContext, RippleException>();
            RippleList stack = mc.list().push(mc.canonicalValue(new RDFValue(new URIImpl("http://example.org/foo"))))
                    .push(mc.canonicalValue(new RDFValue(RDF.FIRST))).push(Operator.OP);

            eval.apply(new StackContext(stack, mc), solutions);
            for (StackContext c : solutions) {
                System.out.println("solution: " + c.getStack());
            }
        } finally {
            mc.close();
        }

        model.shutDown();
        sail.shutDown();
    }*/

    private void addDummyData(final Sail sail) throws Exception {
        Repository repo = new SailRepository(sail);
        RepositoryConnection rc = repo.getConnection();
        try {
            rc.begin();
            rc.add(RippleSail.class.getResource("rippleSailTest.trig"), "", RDFFormat.TRIG);
            rc.commit();
        } finally {
            rc.rollback();
            rc.close();
        }
    }

    private Set<Statement> toSet(final CloseableIteration<? extends Statement, SailException> i) throws SailException {
        try {
            Set<Statement> set = new HashSet<Statement>();
            while (i.hasNext()) {
                set.add(i.next());
            }
            return set;
        } finally {
            i.close();
        }
    }
}

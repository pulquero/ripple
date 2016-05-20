package net.fortytwo.ripple.model.impl.sesame;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import net.fortytwo.flow.rdf.RDFSink;
import net.fortytwo.flow.rdf.SailInserter;
import net.fortytwo.flow.rdf.SesameInputAdapter;
import net.fortytwo.flow.rdf.SesameOutputAdapter;
import net.fortytwo.flow.rdf.SingleContextPipe;
import net.fortytwo.ripple.test.RippleTestCase;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SesameTest extends RippleTestCase {

    static int countStatements(final SailConnection sc, final IRI context)
            throws Exception {
        int count = 0;

        CloseableIteration<? extends Statement, SailException> stmtIter
                = (null == context)
                ? sc.getStatements(null, null, null, false)
                : sc.getStatements(null, null, null, false, context);

        while (stmtIter.hasNext()) {
            stmtIter.next();
            count++;
        }

        stmtIter.close();

        return count;
    }

    public void testRecoverFromParseError() throws Exception {
        Sail sail = new MemoryStore();
        sail.initialize();

        String bad = "bad";

        String good = "@prefix foo:  <http://example.org/foo#>.\n"
                + "foo:a foo:b foo:c.";

        InputStream is = null;

        try {
            is = new ByteArrayInputStream(bad.getBytes());
            add(sail, is, "", RDFFormat.TURTLE);
        } catch (Exception e) {
        } finally {
            is.close();
        }

        try {
            is = new ByteArrayInputStream(good.getBytes());
            add(sail, is, "", RDFFormat.TURTLE);
        } catch (Exception e) {
        }
        is.close();

        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            int count = countStatements(sc, null);
            assertEquals(1, count);
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    public void testAddFromInputStream() throws Exception {
        Sail sail = new MemoryStore();
        sail.initialize();
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();

            IRI ctxA = sail.getValueFactory().createIRI("urn:test.AddFromInputStreamTest.ctxA#");

            String s = "@prefix foo:  <http://example.org/foo#>.\n"
                    + "foo:a foo:b foo:c.";
            InputStream is = new ByteArrayInputStream(s.getBytes());
            try {
                add(sail, is, ctxA.toString(), RDFFormat.TURTLE, ctxA);
            } finally {
                is.close();
            }

            assertEquals(1, countStatements(sc, null));
/* 60 */
            assertEquals(1, countStatements(sc, ctxA));
        } finally {
            sc.close();
        }
        sail.shutDown();
    }

    // Verifies that Sesame does not unescape literal labels (not that one would
    // reasonably suspect it of doing so).
    public void testEscapeCharactersInLiterals() throws Exception {
        Sail sail = new MemoryStore();
        sail.initialize();
        ValueFactory vf = sail.getValueFactory();
        Literal l;

        l = vf.createLiteral("\"");
        assertEquals(1, l.getLabel().length());
        l = vf.createLiteral("\\\"");
        assertEquals(2, l.getLabel().length());

        l = vf.createLiteral("\"", XMLSchema.STRING);
        assertEquals(1, l.getLabel().length());
        l = vf.createLiteral("\\\"", XMLSchema.STRING);
        assertEquals(2, l.getLabel().length());

        sail.shutDown();
    }

    private void add(final Sail sail, final InputStream is, final String baseUri, final RDFFormat format)
            throws Exception {

        RDFParser parser = Rio.createParser(format);
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();

            SailInserter inserter = new SailInserter(sc);
            parser.setRDFHandler(inserter);

            inserter.startRDF();

            try {
                parser.parse(is, baseUri);
            } catch (Exception e) {
                inserter.endRDF();
                sc.close();
                throw e;
            }

            inserter.endRDF();
            sc.commit();
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    private void add(final Sail sail,
                     final InputStream is,
                     final String baseUri,
                     final RDFFormat format,
                     final IRI context) throws Exception {

        RDFParser parser = Rio.createParser(format);
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();
            SailInserter inserter = new SailInserter(sc);
            SesameOutputAdapter outAdapter = new SesameOutputAdapter(inserter);
            RDFSink scp = new SingleContextPipe(outAdapter, context, sail.getValueFactory());
            SesameInputAdapter inAdapter = new SesameInputAdapter(scp);
            parser.setRDFHandler(inAdapter);

            inserter.startRDF();

            try {
                parser.parse(is, baseUri);
            } catch (Exception e) {
                inserter.endRDF();
                sc.close();
                throw e;
            }

            inserter.endRDF();
            sc.commit();
        } finally {
            sc.rollback();
            sc.close();
        }
    }
}


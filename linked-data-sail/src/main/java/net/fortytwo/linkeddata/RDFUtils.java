package net.fortytwo.linkeddata;

import net.fortytwo.flow.rdf.SesameOutputAdapter;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import org.apache.jena.iri.IRIFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public final class RDFUtils {
    private static final Logger logger = LoggerFactory.getLogger(RDFUtils.class.getName());

    private RDFUtils() {
    }

    public static SesameOutputAdapter createOutputAdapter(
            final OutputStream out,
            final RDFFormat format)
            throws RippleException {
        RDFWriter writer;

        try {
            // Note: a comment by Jeen suggests that a new writer should be created
            //       for each use:
            //       http://www.openrdf.org/forum/mvnforum/viewthread?thread=785#3159
            writer = Rio.createWriter(format, out);
        } catch (Throwable t) {
            throw new RippleException(t);
        }

        return new SesameOutputAdapter(writer);
    }

    public static boolean isHttpUri(final IRI uri) {
        return uri.toString().startsWith("http://");
    }

    public static IRI inferContextURI(final Resource subject,
                                      final ValueFactory valueFactory)
            throws RippleException {
        if (!(subject instanceof IRI)) {
            return null;
        } else {
            String s = removeFragmentIdentifier((subject).toString());

            try {
                return valueFactory.createIRI(s);
            } catch (Throwable t) {
                throw new RippleException(t);
            }
        }
    }

    /**
     * Strips the fragment identifier of a (usually) HTTP URI.
     *
     * @param uri a URI possibly containing a fragment identifier, e.g. http://example.org/foo#bar
     * @return the URI without a fragment identifier, e.g. http://example.org/foo
     */
    public static String removeFragmentIdentifier(final String uri) {
        int i = uri.lastIndexOf('#');
        return 0 <= i ? uri.substring(0, i) : uri;
    }

    /**
     * Creates a hashed version of a URI.
     * This is useful for storing metadata about a URI in a graph without also linking the metadata
     * to the original URI; it is accessible only through hashing.
     */
    public static String hashedUri(final String originalUri) {
        return Ripple.RANDOM_URN_PREFIX
                + UUID.nameUUIDFromBytes(originalUri.getBytes());
    }

    public static String findGraphUri(final String uri) {
        String docUri = removeFragmentIdentifier(uri);
        return hashedUri(docUri);
    }

    public static URL iriToUrl(final String iriStr) throws MalformedURLException {
        IRIFactory f = new IRIFactory();
        org.apache.jena.iri.IRI iri = f.create(iriStr);
        boolean includeWarnings = false;
        if (iri.hasViolation(includeWarnings)) {
            logger.warn("IRI has syntax violation: " + iriStr);
        }

        return iri.toURL();
    }
}

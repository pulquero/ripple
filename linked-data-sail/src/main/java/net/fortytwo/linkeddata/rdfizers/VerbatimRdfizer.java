package net.fortytwo.linkeddata.rdfizers;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortytwo.linkeddata.CacheEntry;
import net.fortytwo.linkeddata.Rdfizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class VerbatimRdfizer implements Rdfizer {
    private static final Logger logger = LoggerFactory.getLogger(VerbatimRdfizer.class.getName());

    private final RDFFormat format;
    private final RDFParser parser;

    public VerbatimRdfizer(final RDFFormat format,
                           final ParserConfig config) {
        this.format = format;
        parser = Rio.createParser(format);
        parser.setParserConfig(config);
    }

    public CacheEntry.Status rdfize(final InputStream is,
                                    final RDFHandler handler,
                                    final String baseUri) {
        try {
            parser.setRDFHandler(handler);
            parser.parse(is, baseUri);
        } catch (IOException e) {
            logger.warn("I/O error in " + format.getName() + " rdfizer", e);
            return CacheEntry.Status.Failure;
        } catch (RDFParseException e) {
            logger.warn("RDF parsing error in " + format.getName() + " rdfizer", e);
            return CacheEntry.Status.ParseError;
        } catch (RDFHandlerException e) {
            logger.warn("RDF handler error in " + format.getName() + " rdfizer", e);
            return CacheEntry.Status.Failure;
        } catch (Throwable t) {
            logger.error("ungrokked error in " + format.getName() + " rdfizer", t);
        }

        return CacheEntry.Status.Success;
    }

    public String toString() {
        return "'" + this.format.getName() + "' verbatim rdfizer";
    }
}

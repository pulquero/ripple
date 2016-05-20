package org.semarglproject.rdf4j.rdf.rdfa;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserFactory;

public class Rdf4jRDFaParserFactory implements RDFParserFactory {

    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.RDFA;
    }

    @Override
    public RDFParser getParser() {
        return new Rdf4jRDFaParser();
    }

}

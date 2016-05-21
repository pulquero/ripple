package net.fortytwo.linkeddata.sail.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.config.AbstractDelegatingSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.restlet.data.MediaType;

import net.fortytwo.linkeddata.Rdfizer;
import net.fortytwo.linkeddata.rdfizers.VerbatimRdfizer;

public class LinkedDataSailConfig extends AbstractDelegatingSailImplConfig {

	private final Map<MediaType, Rdfizer> rdfizers = new HashMap<>();

	public LinkedDataSailConfig() {
		super(LinkedDataSailFactory.SAIL_TYPE);
	}

	public LinkedDataSailConfig(SailImplConfig delegate) {
		super(LinkedDataSailFactory.SAIL_TYPE, delegate);
	}

	public Map<MediaType, Rdfizer> getRdfizers() {
		return rdfizers;
	}

	public void addRdfizer(MediaType mediaType, Rdfizer rdfizer) {
		rdfizers.put(mediaType, rdfizer);
	}

	@Override
	public Resource export(Model m) {
		Resource implNode = super.export(m);
		return implNode;
	}

	@Override
	public void parse(Model graph, Resource implNode) throws SailConfigException {
		super.parse(graph, implNode);

		// hard-code config for now
		ParserConfig parserConfig = new ParserConfig();
        // www.ipni.org
        addRdfizer(MediaType.TEXT_XML, new VerbatimRdfizer(RDFFormat.RDFXML, parserConfig));
	}
}

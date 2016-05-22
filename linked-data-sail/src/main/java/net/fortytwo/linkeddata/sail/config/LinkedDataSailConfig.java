package net.fortytwo.linkeddata.sail.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.config.AbstractDelegatingSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.restlet.data.MediaType;

import net.fortytwo.linkeddata.Rdfizer;
import net.fortytwo.linkeddata.rdfizers.VerbatimRdfizer;

public class LinkedDataSailConfig extends AbstractDelegatingSailImplConfig {

	private static final String NAMESPACE = "http://fortytwo.net/2012/02/linkeddata/config#";
	private static final IRI MBEAN_NAME_PROPERTY;

	static {
		ValueFactory vf = SimpleValueFactory.getInstance();
		MBEAN_NAME_PROPERTY = vf.createIRI(NAMESPACE+"mbeanName");

	}

	private final Map<MediaType, Rdfizer> rdfizers = new HashMap<>();
	private String mbeanName;

	public LinkedDataSailConfig() {
		super(LinkedDataSailFactory.SAIL_TYPE);
	}

	public LinkedDataSailConfig(SailImplConfig delegate) {
		super(LinkedDataSailFactory.SAIL_TYPE, delegate);
	}

	public Map<MediaType, Rdfizer> getRdfizers() {
		return rdfizers;
	}

	public String getMBeanName() {
		return mbeanName;
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

		mbeanName = Models.objectString(graph.filter(implNode, MBEAN_NAME_PROPERTY, null));

		// hard-code config for now
		ParserConfig parserConfig = new ParserConfig();
        // www.ipni.org
        addRdfizer(MediaType.TEXT_XML, new VerbatimRdfizer(RDFFormat.RDFXML, parserConfig));
	}
}

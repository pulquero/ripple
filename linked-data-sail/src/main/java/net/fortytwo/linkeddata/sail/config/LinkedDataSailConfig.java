package net.fortytwo.linkeddata.sail.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.sail.config.AbstractDelegatingSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.restlet.data.MediaType;

import net.fortytwo.linkeddata.Rdfizer;
import net.fortytwo.linkeddata.rdfizers.VerbatimRdfizer;

public class LinkedDataSailConfig extends AbstractDelegatingSailImplConfig {

	private static final String NAMESPACE = "http://fortytwo.net/2012/02/linkeddata/config#";
	private static final String MEDIA_TYPE_NS = "http://www.iana.org/assignments/media-types/";
	private static final IRI MBEAN_NAME_PROPERTY;
	private static final IRI RDFIZER_PROPERTY;
	private static final IRI CONTENT_TYPE_PROPERTY;
	private static final IRI RDF_FORMAT_PROPERTY;

	static {
		ValueFactory vf = SimpleValueFactory.getInstance();
		MBEAN_NAME_PROPERTY = vf.createIRI(NAMESPACE+"mbeanName");
		RDFIZER_PROPERTY = vf.createIRI(NAMESPACE+"rdfizer");
		CONTENT_TYPE_PROPERTY = vf.createIRI(NAMESPACE+"contentType");
		RDF_FORMAT_PROPERTY = vf.createIRI(NAMESPACE+"rdfFormat");
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

		RDFParserRegistry rdfRegistry = RDFParserRegistry.getInstance();
		ParserConfig parserConfig = new ParserConfig();
		for(Value rdfizer : graph.filter(implNode, RDFIZER_PROPERTY, null).objects()) {
			IRI contentTypeIri = Models.objectIRI(graph.filter((Resource) rdfizer, CONTENT_TYPE_PROPERTY, null));
			IRI rdfFormatIri = Models.objectIRI(graph.filter((Resource) rdfizer, RDF_FORMAT_PROPERTY, null));
			RDFFormat rdfFormat = rdfRegistry.getFileFormatForMIMEType(getMediaType(rdfFormatIri));
	        addRdfizer(MediaType.valueOf(getMediaType(contentTypeIri)), new VerbatimRdfizer(rdfFormat, parserConfig));
		}

		mbeanName = Models.objectString(graph.filter(implNode, MBEAN_NAME_PROPERTY, null));
	}

	private String getMediaType(IRI iri) {
		return iri.stringValue().substring(MEDIA_TYPE_NS.length());
	}
}

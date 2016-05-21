package net.fortytwo.linkeddata.sail.config;

import java.util.Map;

import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailFactory;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.restlet.data.MediaType;

import net.fortytwo.linkeddata.LinkedDataCache;
import net.fortytwo.linkeddata.Rdfizer;
import net.fortytwo.linkeddata.sail.LinkedDataSail;

public class LinkedDataSailFactory implements SailFactory {
	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "ripple:LinkedDataSail";

	/**
	 * Returns the Sail's type: <tt>ripple:LinkedDataSail</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new LinkedDataSailConfig();
	}

	public Sail getSail(SailImplConfig config)
		throws SailConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: " + config.getType());
		}

		LinkedDataCache cache = new LinkedDataCache();

		if (config instanceof LinkedDataSailConfig) {
			LinkedDataSailConfig ldConfig = (LinkedDataSailConfig)config;

			for(Map.Entry<MediaType, Rdfizer> entry : ldConfig.getRdfizers().entrySet())
			{
				cache.addRdfizer(entry.getKey(), entry.getValue(), 0.4);
			}
		}

		LinkedDataSail linkedDataSail = new LinkedDataSail(cache);

		return linkedDataSail;
	}

}

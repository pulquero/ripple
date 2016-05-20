package net.fortytwo.linkeddata.sail.config;

import org.eclipse.rdf4j.sail.config.AbstractDelegatingSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailImplConfig;

public class LinkedDataSailConfig extends AbstractDelegatingSailImplConfig {

	public LinkedDataSailConfig() {
		super(LinkedDataSailFactory.SAIL_TYPE);
	}

	public LinkedDataSailConfig(SailImplConfig delegate) {
		super(LinkedDataSailFactory.SAIL_TYPE, delegate);
	}

}

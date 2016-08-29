/**
 * Copyright (c) 2015 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package net.fortytwo.linkeddata.dereferencers.redirects;

import java.net.URL;

import net.fortytwo.linkeddata.dereferencers.RedirectMapping;

/**
 * @author markh
 *
 */
public class Bio2RdfRedirectMapping implements RedirectMapping {

	@Override
	public String translate(String redirectUrl, URL originalUrl) {
		String host = originalUrl.getHost();
		int port = originalUrl.getPort();
		String addr = (port != -1) ? host + ":" + port : host;
		return redirectUrl.replace("localhost:8890", addr);
	}
}

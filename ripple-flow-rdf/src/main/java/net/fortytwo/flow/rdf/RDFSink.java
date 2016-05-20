package net.fortytwo.flow.rdf;

import net.fortytwo.flow.Sink;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface RDFSink {
    Sink<Statement> statementSink();

    Sink<Namespace> namespaceSink();

    Sink<String> commentSink();
}


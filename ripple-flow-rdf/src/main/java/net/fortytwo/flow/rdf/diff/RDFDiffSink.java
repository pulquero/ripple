package net.fortytwo.flow.rdf.diff;

import net.fortytwo.flow.diff.DiffSink;
import net.fortytwo.flow.rdf.RDFSink;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface RDFDiffSink {
    RDFSink adderSink();

    RDFSink subtractorSink();

    DiffSink<Statement> statementSink();

    DiffSink<Namespace> namespaceSink();

    DiffSink<String> commentSink();
}


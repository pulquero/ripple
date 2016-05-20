package net.fortytwo.flow.rdf;

import net.fortytwo.flow.Collector;
import net.fortytwo.flow.Sink;
import net.fortytwo.flow.Source;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFCollector extends RDFSource implements RDFSink {
    private final Collector<Statement> statements;
    private final Collector<Namespace> namespaces;
    private final Collector<String> comments;

    public RDFCollector() {
        statements = new Collector<Statement>();
        namespaces = new Collector<Namespace>();
        comments = new Collector<String>();
    }

    public Sink<Statement> statementSink() {
        return statements;
    }

    public Sink<Namespace> namespaceSink() {
        return namespaces;
    }

    public Sink<String> commentSink() {
        return comments;
    }

    public Source<Statement> statementSource() {
        return statements;
    }

    public Source<Namespace> namespaceSource() {
        return namespaces;
    }

    public Source<String> commentSource() {
        return comments;
    }

    public void clear() {
        statements.clear();
        namespaces.clear();
        comments.clear();
    }

    public int countStatements() {
        return statements.size();
    }

    public int countNamespaces() {
        return namespaces.size();
    }

    public int countComments() {
        return comments.size();
    }
}


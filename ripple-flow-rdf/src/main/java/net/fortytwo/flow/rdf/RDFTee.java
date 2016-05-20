package net.fortytwo.flow.rdf;

import net.fortytwo.flow.Sink;
import net.fortytwo.flow.Tee;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFTee implements RDFSink {
    private final Sink<Statement> statementTee;
    private final Sink<Namespace> namespaceTee;
    private final Sink<String> commentTee;

    public RDFTee(final RDFSink sinkA, final RDFSink sinkB) {
        statementTee = new Tee<Statement>(sinkA.statementSink(), sinkB.statementSink());
        namespaceTee = new Tee<Namespace>(sinkA.namespaceSink(), sinkB.namespaceSink());
        commentTee = new Tee<String>(sinkA.commentSink(), sinkB.commentSink());
    }

    public Sink<Statement> statementSink() {
        return statementTee;
    }

    public Sink<Namespace> namespaceSink() {
        return namespaceTee;
    }

    public Sink<String> commentSink() {
        return commentTee;
    }
}


package net.fortytwo.flow.rdf;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SingleContextPipe implements RDFSink {
    private final Sink<Statement> stSink;
    private final Sink<Namespace> nsSink;
    private final Sink<String> cmtSink;

    public SingleContextPipe(final RDFSink sink,
                             final Resource context,
                             final ValueFactory valueFactory) {
        final Sink<Statement> otherStSink = sink.statementSink();

        stSink = new Sink<Statement>() {
            public void put(final Statement st) throws RippleException {
                Statement newSt;

                newSt = valueFactory.createStatement(
                        st.getSubject(), st.getPredicate(), st.getObject(), context);

                otherStSink.put(newSt);
            }
        };

        nsSink = sink.namespaceSink();
        cmtSink = sink.commentSink();
    }

    public Sink<Statement> statementSink() {
        return stSink;
    }

    public Sink<Namespace> namespaceSink() {
        return nsSink;
    }

    public Sink<String> commentSink() {
        return cmtSink;
    }
}


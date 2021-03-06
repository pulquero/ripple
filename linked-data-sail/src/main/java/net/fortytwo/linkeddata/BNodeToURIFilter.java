package net.fortytwo.linkeddata;

import net.fortytwo.flow.Sink;
import net.fortytwo.flow.rdf.RDFSink;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import org.openrdf.model.BNode;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class BNodeToURIFilter implements RDFSink {
    private final Sink<Statement> stSink;
    private final Sink<Namespace> nsSink;
    private final Sink<String> cmtSink;

    private ValueFactory valueFactory;

    public BNodeToURIFilter(final RDFSink sink, final ValueFactory vf) {
        valueFactory = vf;

        final Sink<Statement> destStSink = sink.statementSink();

        stSink = new Sink<Statement>() {
            public void put(final Statement st) throws RippleException {
                boolean s = st.getSubject() instanceof BNode;
                boolean o = st.getObject() instanceof BNode;
                boolean c = (null != st.getContext())
                        && st.getContext() instanceof BNode;

                if (s || o || c) {
                    Resource subj = s ? bnodeToUri((BNode) st.getSubject()) : st.getSubject();
                    URI pred = st.getPredicate();
                    Value obj = o ? bnodeToUri((BNode) st.getObject()) : st.getObject();
                    Resource con = c ? bnodeToUri((BNode) st.getContext()) : st.getContext();

                    Statement newSt = (null == con)
                            ? valueFactory.createStatement(subj, pred, obj)
                            : valueFactory.createStatement(subj, pred, obj, con);

                    destStSink.put(newSt);
                } else {
                    destStSink.put(st);
                }
            }
        };

        nsSink = sink.namespaceSink();
        cmtSink = sink.commentSink();
    }

    private URI bnodeToUri(final BNode bnode) {
        return valueFactory.createURI(Ripple.RANDOM_URN_PREFIX + bnode.getID());
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

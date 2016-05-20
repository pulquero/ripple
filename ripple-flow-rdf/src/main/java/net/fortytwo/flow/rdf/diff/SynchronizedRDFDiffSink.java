package net.fortytwo.flow.rdf.diff;

import net.fortytwo.flow.Sink;
import net.fortytwo.flow.diff.DiffSink;
import net.fortytwo.flow.rdf.RDFSink;
import net.fortytwo.flow.rdf.SynchronizedRDFSink;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SynchronizedRDFDiffSink implements RDFDiffSink {
    private final SynchronizedRDFSink addSink, subSink;
    private final DiffSink<Statement> stSink;
    private final DiffSink<Namespace> nsSink;
    private final DiffSink<String> cmtSink;

    public SynchronizedRDFDiffSink(final RDFDiffSink sink, final Object mutex) {
        addSink = new SynchronizedRDFSink(sink.adderSink(), mutex);
        subSink = new SynchronizedRDFSink(sink.subtractorSink(), mutex);

        stSink = new DiffSink<Statement>() {
            public Sink<Statement> getPlus() {
                return addSink.statementSink();
            }

            public Sink<Statement> getMinus() {
                return subSink.statementSink();
            }
        };

        nsSink = new DiffSink<Namespace>() {
            public Sink<Namespace> getPlus() {
                return addSink.namespaceSink();
            }

            public Sink<Namespace> getMinus() {
                return subSink.namespaceSink();
            }
        };

        cmtSink = new DiffSink<String>() {
            public Sink<String> getPlus() {
                return addSink.commentSink();
            }

            public Sink<String> getMinus() {
                return subSink.commentSink();
            }
        };
    }

    public RDFSink adderSink() {
        return addSink;
    }

    public RDFSink subtractorSink() {
        return subSink;
    }

    public DiffSink<Statement> statementSink() {
        return stSink;
    }

    public DiffSink<Namespace> namespaceSink() {
        return nsSink;
    }

    public DiffSink<String> commentSink() {
        return cmtSink;
    }

    public Object getMutex() {
        return addSink.getMutex();
    }
}


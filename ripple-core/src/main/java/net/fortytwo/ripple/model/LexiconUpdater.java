package net.fortytwo.ripple.model;

import net.fortytwo.flow.Sink;
import net.fortytwo.flow.diff.DiffSink;
import net.fortytwo.flow.rdf.RDFSink;
import net.fortytwo.flow.rdf.diff.RDFDiffSink;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import java.util.regex.Pattern;

/**
 * Note: several LexiconUpdaters may safely be attached to a single Lexicon.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LexiconUpdater implements RDFDiffSink {
    // TODO: Unicode characters supported by the lexer / Turtle grammar
    private static final Pattern PREFIX_PATTERN
            = Pattern.compile("[A-Za-z][-0-9A-Z_a-z]*");

    private final RDFSink addSink, subSink;
    private final DiffSink<Statement> stSink;
    private final DiffSink<Namespace> nsSink;
    private final DiffSink<String> cmtSink;

    public LexiconUpdater(final Lexicon lexicon) throws RippleException {
        final boolean override = Ripple.getConfiguration().getBoolean(
                Ripple.PREFER_NEWEST_NAMESPACE_DEFINITIONS);
        final boolean allowDuplicateNamespaces = Ripple.getConfiguration().getBoolean(
                Ripple.ALLOW_DUPLICATE_NAMESPACES);

        addSink = new RDFSink() {
            private Sink<Statement> stSink = new Sink<Statement>() {
                public void put(final Statement st) throws RippleException {
//System.out.println( "st = " + st );
                    Resource subj = st.getSubject();
                    IRI pred = st.getPredicate();
                    Value obj = st.getObject();

                    synchronized (lexicon) {
                        if (subj instanceof IRI) {
                            lexicon.addURI((IRI) subj);
                        }

                        lexicon.addURI(pred);

                        if (obj instanceof IRI) {
                            lexicon.addURI((IRI) obj);
                        }
                    }
                }
            };

            private Sink<Namespace> nsSink = new Sink<Namespace>() {
                public void put(final Namespace ns) throws RippleException {
// TODO: delete me
                }
            };

            private Sink<String> cmtSink = new Sink<String>() {
                public void put(final String comment) throws RippleException {
                }
            };

            public Sink<Statement> statementSink() {
                return stSink;
            }

            public Sink<Namespace> namespaceSink() {
                return nsSink;
            }

            public Sink<String> commentSink() {
                return cmtSink;
            }
        };

// TODO
        subSink = new RDFSink() {
            private Sink<Statement> stSink = new Sink<Statement>() {
                public void put(final Statement st) throws RippleException {
                }
            };

            private Sink<Namespace> nsSink = new Sink<Namespace>() {
                public void put(final Namespace ns) throws RippleException {
                }
            };

            private Sink<String> cmtSink = new Sink<String>() {
                public void put(final String comment) throws RippleException {
                }
            };

            public Sink<Statement> statementSink() {
                return stSink;
            }

            public Sink<Namespace> namespaceSink() {
                return nsSink;
            }

            public Sink<String> commentSink() {
                return cmtSink;
            }
        };

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

    private boolean allowedNsPrefix(final String nsPrefix) {
        return PREFIX_PATTERN.matcher(nsPrefix).matches();
    }
}

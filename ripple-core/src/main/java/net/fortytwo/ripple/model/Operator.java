package net.fortytwo.ripple.model;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.util.ModelConnectionHelper;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Operator {
    public static final Op OP = new Op();

    private final StackMapping mapping;

    private Operator(final String key) {
        mapping = new KeyValueMapping(key);
    }

    // Note: only for URI values (Literals are handled in createOperator)
    private Operator(final Value pred) throws RippleException {
        mapping = new RDFPredicateMapping(StatementPatternQuery.Pattern.SP_O, pred, null);
    }

    public Operator(final StackMapping mapping) {
        this.mapping = mapping;
    }

    public Operator(final RippleList list) {
        mapping = new ListDequotation(list);
    }

    // TODO: implement equals() and hashCode() for all StackMappings
    public boolean equals(final Object other) {
        return ((Operator) other).mapping.equals(mapping) && (other instanceof Operator);
    }

    public int hashCode() {
        return 630572943 + mapping.hashCode();
    }

    public String toString() {
        return "Operator(" + mapping + ")";
    }

    public StackMapping getMapping() {
        return mapping;
    }

    /**
     * Finds the type of a value and creates an appropriate "active" wrapper.
     */
    public static void createOperator(final Object v,
                                      final Sink<Operator> opSink,
                                      final ModelConnection mc) throws RippleException {
        // A function becomes active.
        if (v instanceof StackMapping) {
            opSink.put(new Operator((StackMapping) v));
            return;
        }

        // A list is dequoted.
        else if (v instanceof RippleList) {
            opSink.put(new Operator((RippleList) v));
            return;
        }

        // A string becomes a key
        else if (v instanceof String) {
            opSink.put(new Operator((String) v));
        }

        // This is the messy part.  Attempt to guess the type of the object from
        // the available RDF statements, and create the appropriate object.
        if (v instanceof Value) {
            if (ModelConnectionHelper.isRDFList(v, mc)) {
                Sink<RippleList> listSink = new Sink<RippleList>() {
                    public void put(final RippleList list) throws RippleException {
                        opSink.put(new Operator(list));
                    }
                };

                mc.toList(v, listSink);
                return;
            } else {
                Value sv = ((Value) v);

                if (sv instanceof Literal) {
                    opSink.put(new Operator(((Literal) sv).getLabel()));
                    return;
                }

                // An RDF resource not otherwise recognizable becomes a predicate filter.
                else if (sv instanceof Resource) {
                    opSink.put(new Operator((Value) v));
                    return;
                }
            }
        }

        // Anything else becomes an active nullary filter with no output.
        opSink.put(new Operator(new NullStackMapping()));
    }
}


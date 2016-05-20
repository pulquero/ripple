package net.fortytwo.ripple.libs.graph;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A primitive which consumes a SPARQL query and pushes, for each result,
 * a variable number of values onto the stack, one for each binding.
 * FIXME: this is very much a hack.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Sparql extends PrimitiveStackMapping {
    private static final Logger logger
            = Logger.getLogger(Sparql.class.getName());

    private static final String[] IDENTIFIERS = {
            GraphLibrary.NS_2013_03 + "sparql",
            GraphLibrary.NS_2008_08 + "sparql"};

    public String[] getIdentifiers() {
        return IDENTIFIERS;
    }

    public Sparql()
            throws RippleException {
        super();
    }

    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("query", null, true)};
    }

    public String getComment() {
        return "evaluates a SPARQL query";
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {

        RippleList stack = arg;

        String query = mc.toString(stack.getFirst());
        stack = stack.getRest();

        CloseableIteration<? extends BindingSet, QueryEvaluationException> results
                = mc.evaluate(query);

        try {
            try {
                while (results.hasNext()) {
                    SPARQLValue kv = new SPARQLValue(results.next());

                    try {
                        solutions.put(stack.push(kv));
                    } catch (RippleException e) {
                        // Soft fail
                        logger.log(Level.WARNING, "failed to put SPARQL result", e);
                    }
                }
            } finally {
                results.close();
            }
        } catch (QueryEvaluationException e) {
            throw new RippleException(e);
        }
    }
}

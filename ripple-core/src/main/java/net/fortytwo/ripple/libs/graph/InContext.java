package net.fortytwo.ripple.libs.graph;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.Operator;
import net.fortytwo.ripple.model.RDFPredicateMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.StackMapping;
import org.eclipse.rdf4j.model.Value;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class InContext extends RDFPredicateStackMapping {
    private static final String[] IDENTIFIERS = {
            GraphLibrary.NS_2013_03 + "in-context",
            GraphLibrary.NS_2007_08 + "inContext"};

    public String[] getIdentifiers() {
        return IDENTIFIERS;
    }

    public InContext() throws RippleException {
        super(false);

        this.inverse = new InContext(this);
    }

    private InContext(final StackMapping original) throws RippleException {
        super(true);

        this.inverse = original;
    }

    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("s", null, true),
                new Parameter("p", null, true),
                new Parameter("g", null, true)};
    }

    public String getComment() {
        return "s p g  =>  o";
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {

        RippleList stack = arg;

        Value context = mc.toRDF(stack.getFirst());
        stack = stack.getRest();
        Value pred = mc.toRDF(stack.getFirst());
        stack = stack.getRest();

        RDFPredicateMapping mapping = getMapping(pred, context);

        solutions.put(
                stack.push(new Operator(mapping)));
    }
}

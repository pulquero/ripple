package net.fortytwo.ripple.libs.data;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 * A primitive which consumes a literal value and produces its data type (if
 * any).
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Type extends PrimitiveStackMapping {
    public String[] getIdentifiers() {
        return new String[]{
                DataLibrary.NS_XSD + "type"};
    }

    public Type()
            throws RippleException {
        super();
    }

    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("l", null, true)};
    }

    public String getComment() {
        return "l  =>  data type of literal l";
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {

        RippleList stack = arg;

        Value v;

        v = mc.toRDF(stack.getFirst());
        stack = stack.getRest();

        if (v instanceof Literal) {
            IRI type = ((Literal) v).getDatatype();

            if (null != type) {
                solutions.put(
                        stack.push(type));
            }
        }
    }
}


package net.fortytwo.ripple.model.types;

import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.StackMapping;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class StringLiteralType extends RDFValueType<Literal> {
    public StringLiteralType() {
        super(Literal.class);
    }

    @Override
    public boolean isInstance(Literal instance) {
        return null != instance.getDatatype() && XMLSchema.STRING == instance.getDatatype();
    }

    @Override
    public Category getCategory() {
        return Category.STRING_TYPED_LITERAL;
    }

    @Override
    public StackMapping getMapping(Literal instance) {
        return null;
    }

    @Override
    public int compare(Literal o1, Literal o2, ModelConnection mc) {
        return o1.getLabel().compareTo(o2.getLabel());
    }
}
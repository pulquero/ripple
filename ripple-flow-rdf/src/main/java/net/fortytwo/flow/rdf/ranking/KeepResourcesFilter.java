package net.fortytwo.flow.rdf.ranking;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class KeepResourcesFilter<E extends Exception> extends Pipe<Value, Resource, E> {
    public KeepResourcesFilter(final Handler<Resource, E> innerHandler) {
        super(innerHandler);
    }

    public boolean handle(final Value value) throws E {
        return !(value instanceof Resource) || innerHandler.handle((Resource) value);
    }
}

package net.fortytwo.ripple.libs.graph;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SPARQLValue {
    private final Map<String, Value> pairs;

    public SPARQLValue(BindingSet bindings) {
        pairs = new HashMap<String, Value>();
        for (String key : bindings.getBindingNames()) {
            Value value = bindings.getValue(key);
            if (null != value) {
                pairs.put(key, value);
            }
        }
    }

    public Map<String, Value> getPairs() {
        return pairs;
    }
}

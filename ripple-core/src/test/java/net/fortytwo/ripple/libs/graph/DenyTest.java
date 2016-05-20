package net.fortytwo.ripple.libs.graph;

import net.fortytwo.ripple.test.RippleTestCase;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DenyTest extends RippleTestCase {
    public void testSimple() throws Exception {
        reduce("@prefix ex: <http://example.org/denyTest/> .");

        assertReducesTo("ex:a rdf:type ex:ClassA ex:ClassB both. assert.", "ex:a", "ex:a");
        assertReducesTo("ex:a rdf:type.", "ex:ClassA", "ex:ClassB");
        assertReducesTo("ex:ClassA rdf:type~.", "ex:a");
        assertReducesTo("ex:ClassB rdf:type~.", "ex:a");
        assertReducesTo("ex:a rdf:type ex:ClassC deny.", "ex:a");
        assertReducesTo("ex:a rdf:type.", "ex:ClassA", "ex:ClassB");
        assertReducesTo("ex:ClassA rdf:type~.", "ex:a");
        assertReducesTo("ex:ClassB rdf:type~.", "ex:a");
        assertReducesTo("ex:a rdf:type ex:ClassB deny.", "ex:a");
        assertReducesTo("ex:a rdf:type.", "ex:ClassA");
        assertReducesTo("ex:ClassA rdf:type~.", "ex:a");
        assertReducesTo("ex:ClassB rdf:type~.");

        modelConnection.remove(null, null, modelConnection.list());
        modelConnection.commit();
        assertReducesTo("ex:b rdf:type () assert.", "ex:b");
        assertReducesTo("ex:b rdf:type.", "()");
        assertReducesTo("() rdf:type~.", "ex:b");
        assertReducesTo("ex:b rdf:type () deny.", "ex:b");
        assertReducesTo("ex:b rdf:type.");
        assertReducesTo("() rdf:type~.");
    }

    public void testLiteralObjects() throws Exception {
        reduce("@prefix ex: <http://example.org/denyTest/>.");

        modelConnection.remove(null, null, 42);
        modelConnection.commit();
        assertReducesTo("ex:a ex:specialNumer 42 assert.", "ex:a");
        assertReducesTo("ex:a ex:specialNumer.", "42");
        assertReducesTo("42 ex:specialNumer~.", "ex:a");
        assertReducesTo("ex:a ex:specialNumer 42 deny.", "ex:a");
        assertReducesTo("ex:a ex:specialNumer.");
        assertReducesTo("42 ex:specialNumer~.");
        // Equality does not imply identity in statement queries.
        assertReducesTo("ex:b ex:specialNumer 42 assert.", "ex:b");
        assertReducesTo("ex:b ex:specialNumer 42.0 deny.", "ex:b");
        assertReducesTo("ex:b ex:specialNumer.", "42");
        assertReducesTo("42 ex:specialNumer~.", "ex:b");

        modelConnection.remove(null, null, "something");
        modelConnection.commit();
        assertReducesTo("ex:a rdfs:comment \"something\" assert.", "ex:a");
        assertReducesTo("ex:a rdfs:comment.", "\"something\"");
        assertReducesTo("\"something\" rdfs:comment~.", "ex:a");
        assertReducesTo("ex:a rdfs:comment \"something\" deny.", "ex:a");
        assertReducesTo("ex:a rdfs:comment.");

        modelConnection.remove(null, null, modelConnection.valueOf("something", XMLSchema.STRING));
        modelConnection.commit();
        assertReducesTo("ex:a rdfs:label \"something\"^^xsd:string assert.", "ex:a");
        assertReducesTo("ex:a rdfs:label.", "\"something\"^^xsd:string");
        assertReducesTo("\"something\"^^xsd:string rdfs:label~.", "ex:a");
        assertReducesTo("ex:a rdfs:label \"something\" deny.", "ex:a");
        //assertReducesTo("ex:a rdfs:label.", "\"something\"^^xsd:string");
        //assertReducesTo("\"something\"^^xsd:string rdfs:label~.", "ex:a");
        assertReducesTo("ex:a rdfs:label \"something\"^^xsd:string deny.", "ex:a");
        assertReducesTo("ex:a rdfs:comment.");
        assertReducesTo("\"something\"^^xsd:string rdfs:label~.");
    }
}

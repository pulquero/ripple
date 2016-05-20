package net.fortytwo.ripple;

import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.ripple.sail.RippleSail;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleSailDemo {
    private RippleSailDemo() {
    }

    private static void runDemo() throws Exception {
        Sail storage = new MemoryStore();
        storage.initialize();
        Sail linkedData = new LinkedDataSail(storage);
        linkedData.initialize();

        RippleSail ripple = new RippleSail(linkedData);
        ripple.initialize();

        Repository repo = new SailRepository(ripple);
        RepositoryConnection rc = repo.getConnection();

        // Constructing literal subjects
        String query1 = "PREFIX math: <http://fortytwo.net/2011/04/ripple/math#>\n" +
                "PREFIX stack: <http://fortytwo.net/2011/04/ripple/stack#>\n" +
                "SELECT ?result WHERE {\n" +
                "   ?x stack:self 42 .\n" +
                "   ?x math:sqrt ?result .\n" +
                "}";

        // Constructing literal predicates
        String query2 = "PREFIX graph: <http://fortytwo.net/2011/04/ripple/graph#>\n" +
                "PREFIX stack: <http://fortytwo.net/2011/04/ripple/stack#>\n" +
                "PREFIX s: <urn:string:>\n" +
                "SELECT ?result WHERE {\n" +
                "   ?x stack:self \"{\\\"foo\\\": true, \\\"bar\\\": [6, 9, 42]}\" .\n" +
                "   ?x graph:to-json/s:foo ?result .\n" +
                "}";

        // Loading externally-defined programs
        String query3 = "PREFIX : <http://ripple.fortytwo.net/code/2011/06/rippleSailExamples#>\n" +
                "SELECT ?name WHERE {" +
                "    () :embarcadero/:parent-place*/s:name ?name ." +
                "}";

        TupleQueryResult r = rc.prepareTupleQuery(QueryLanguage.SPARQL, query2).evaluate();
        while (r.hasNext()) {
            System.out.println("result: " + r.next());
        }

        rc.close();
        repo.shutDown();
        ripple.shutDown();
        linkedData.shutDown();
        storage.shutDown();
    }

    public static void main(final String[] args) throws SailException {
        try {
            runDemo();
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // Exit despite any remaining active threads.
        System.exit(0);
    }

    /*
rm /tmp/ripple-tmp.trig
ripple config/publishing.props

@prefix : <http://ripple.fortytwo.net/code/2011/06/rippleSailExamples#>

# Fetches Twitter Places JSON from Twitter's REST API
# id: a place id (e.g. "90942366be65cd2c")
@list id twitter-place: \
    "http://api.twitter.com/1/geo/id/" id concat. \
    ".json" concat. \
    to-uri. get. to-json.

# Fetches the parent features of a place
# place: the JSON of an already-fetched place
@list place parent-place: \
    place "contained_within". each. \
    "id". :twitter-place.

@list embarcadero: "90942366be65cd2c" :twitter-place.
@quit

rapper -i trig -o rdfxml /tmp/ripple-tmp.trig > /tmp/ripple-tmp.rdf
cp /tmp/ripple-tmp.rdf ~/Dropbox/hosts/ripple.fortytwo.net/code/2011/06/rippleSailExamples.rdf
vim ~/Dropbox/hosts/ripple.fortytwo.net/code/2011/06/.htaccess
*/
}

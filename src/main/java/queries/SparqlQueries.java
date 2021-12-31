package queries;

import model.Vtuber;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import java.util.ArrayList;
import java.util.List;

public class SparqlQueries {

    public static List<String> getVtuberList() {
        final String queryString = """
                PREFIX wikibase: <http://wikiba.se/ontology#>
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                prefix owl: <http://www.w3.org/2002/07/owl#>

                SELECT distinct ?label
                WHERE {
                  ?item rdf:type wikibase:Item.
                  ?item wdt:P527 ?label
                }
                """;

        final Query query = QueryFactory.create(queryString);
        final Dataset dataset = TDBFactory.createDataset("src/main/resources/local_endpoint");
        final Model model = dataset.getDefaultModel();

        final List<String> vtuberList = new ArrayList<>();

        try (final QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            final ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                final String[] split = results.next().get("?label").toString().split("/");
                vtuberList.add(split[split.length - 1]);
            }
        }


        return vtuberList;

    }

    public static Vtuber buildVtuber(final String qid) {
        return new Vtuber().setName(getName(qid));
    }

    private static String getName(final String qid) {
        final String queryString = """
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX wd: <http://www.wikidata.org/entity/>
                SELECT  *
                WHERE {
                        wd:$id rdfs:label ?label .
                        FILTER (langMatches( lang(?label), "EN" ) )
                      }
                LIMIT 1
                """.replace("$id", qid);
        final Query query = QueryFactory.create(queryString);
        final QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
        final ResultSet results = qexec.execSelect();
        return getResultAsStringList(results).get(0).get(0);

    }

    private static List<List<String>> getResultAsStringList(final ResultSet results) {
        final List<List<String>> resultsString = new ArrayList<>();
        while (results.hasNext()) {
            final ArrayList<String> values = new ArrayList<>();
            final QuerySolution next = results.next();
            next.varNames().forEachRemaining(varName -> values.add(next.get(varName).toString()));
            resultsString.add(values);
        }
        results.close();
        return resultsString;
    }
}

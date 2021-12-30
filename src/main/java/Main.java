import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

public class Main {
    public static void main(String[] args) {
        String sparqlQueryString1 = """
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                 PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                 PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>
                 PREFIX dct: <http://purl.org/dc/terms/>
                 PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                 PREFIX owl: <http://www.w3.org/2002/07/owl#>
                 PREFIX wikibase: <http://wikiba.se/ontology#>
                 PREFIX wdata: <http://www.wikidata.org/wiki/Special:EntityData/>
                 PREFIX bd: <http://www.bigdata.com/rdf#>
                 PREFIX wd: <http://www.wikidata.org/entity/>
                 PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                 PREFIX p: <http://www.wikidata.org/prop/>
                 PREFIX ps: <http://www.wikidata.org/prop/statement/>
                SELECT DISTINCT ?item ?itemLabel WHERE {
                  SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE]". }
                  {
                    SELECT DISTINCT ?item WHERE {
                      ?item p:P361 ?statement0.
                      ?statement0 (ps:P361/(wdt:P279*)) wd:Q69583635.
                    }
                    LIMIT 100
                  }
                }""";
        Query query = QueryFactory.create(sparqlQueryString1);

        //QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
        //ResultSet results = qexec.execSelect();
        //ResultSetFormatter.out(System.out, results, query);
        //qexec.close();

        Dataset dataset = TDBFactory.createDataset("src/main/resources/local_endpoint");
        Model model = dataset.getDefaultModel();


        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(System.out, results, query);
        }
    }
}

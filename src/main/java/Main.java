import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.TDB2Factory;

public class Main {
    public static void main(String[] args) {
        requeteOnline();
        requeteLocal();
    }

    public static void requeteOnline(){
        String queryString = """
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
                SELECT DISTINCT ?item WHERE {
                  SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE]". }
                  {
                    SELECT DISTINCT ?item WHERE {
                      ?item p:P361 ?statement0.
                      ?statement0 (ps:P361/(wdt:P279*)) wd:Q69583635.
                    }
                    LIMIT 100
                  }
                }""";

        Query query = QueryFactory.create(queryString);

        QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
        ResultSet results = qexec.execSelect();
        ResultSetFormatter.out(System.out, results, query);
        qexec.close();
    }
    public static void requeteLocal(){
        String queryString= """
                PREFIX wikibase: <http://wikiba.se/ontology#>
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                prefix owl: <http://www.w3.org/2002/07/owl#>

                SELECT ?label
                WHERE {
                  ?item rdf:type wikibase:Item.
                  ?item wdt:P527 ?label
                }
                LIMIT 25""";

        Query query = QueryFactory.create(queryString);
        Dataset dataset = TDBFactory.createDataset("src/main/resources/local_endpoint");
        Model model = dataset.getDefaultModel();


        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect() ;
            ResultSetFormatter.out(System.out, results, query);
        }

    }
}

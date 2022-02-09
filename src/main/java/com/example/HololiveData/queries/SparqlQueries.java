package com.example.HololiveData.queries;

import com.example.HololiveData.model.Vtuber;
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
        /* Equivalent online
        PREFIX wikibase: <http://wikiba.se/ontology#>
        SELECT ?vtuber ?vtuberLabel
        WHERE
        {
             wd:Q69583635 p:P527 ?statement.
             ?statement ps:P527 ?vtuber.
             SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
        }
         */

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
        return new Vtuber()
                .setId(qid)
                .setName(getName(qid))
                ;
    }

    public static List<List<String>> getAllInfo(final String qid) {
        final String queryString = """
                PREFIX bd: <http://www.bigdata.com/rdf#>
                PREFIX wd: <http://www.wikidata.org/entity/>
                PREFIX wikibase: <http://wikiba.se/ontology#>
                SELECT ?wdLabel ?ps_Label ?wdpqLabel ?pq_Label {
                  VALUES (?company) {(wd:$qid)}
                  
                  ?company ?p ?statement .
                  ?statement ?ps ?ps_ .

                  ?wd wikibase:claim ?p.
                  ?wd wikibase:statementProperty ?ps.

                  OPTIONAL {
                  ?statement ?pq ?pq_ .
                  ?wdpq wikibase:qualifier ?pq .
                  }
                  
                  SERVICE wikibase:label { bd:serviceParam wikibase:language "en" }
                } ORDER BY ?wd ?statement ?ps_
                """.replace("$qid", qid);
        final Query query = QueryFactory.create(queryString);
        final QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
        final ResultSet results = qexec.execSelect();
        return getResultAsStringList(results);
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

    private static List<List<String>> getVtuberData(final String qid) {
        final String queryString = """
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX wd: <http://www.wikidata.org/entity/>
                SELECT  ?label ?date_debut ?native_name ?eye_color_label ?hair_color_label ?height
                WHERE {
                        wd:Q60649413 rdfs:label ?label .
                        wd:Q60649413 wdt:P2031 ?date_debut .
                        wd:Q60649413 wdt:P1559 ?native_name .
                        wd:Q60649413 wdt:P1340 ?eye_color .
                        ?eye_color rdfs:label ?eye_color_label .
                        wd:Q60649413 wdt:P1884 ?hair_color .
                        ?hair_color rdfs:label ?hair_color_label .
                        wd:Q60649413 wdt:P1884 ?hair_color .
                        wd:Q60649413 wdt:P2048 ?height .
                  Optional{   
                  wd:Q60649413 wdt:P2032 ?date_fin .
                  }
                        FILTER (langMatches( lang(?label), "EN" ) )
                        FILTER(LANGMATCHES(LANG(?eye_color_label), "en"))
                        FILTER(LANGMATCHES(LANG(?hair_color_label), "en"))
                      }
                LIMIT 1""".replace("$qid", qid);
        final Query query = QueryFactory.create(queryString);
        final QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
        final ResultSet results = qexec.execSelect();
        return getResultAsStringList(results);
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

package com.example.HololiveData.queries;

import com.example.HololiveData.model.SocialMedia;
import com.example.HololiveData.model.SocialMedias;
import com.example.HololiveData.model.Vtuber;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class SparqlQueries {

    public static Map<String, List<String>> genAndDateInfo = getVtuberGenerationAndDate();

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

    public static Set<String> getGenerations() {
        final Set<String> generations = new HashSet<>();
        genAndDateInfo.keySet().forEach(it ->
                generations.add(genAndDateInfo.get(it).get(2))
        );
        return generations;
    }

    public static Vtuber buildVtuber(final String qid) {
        System.out.println(qid);
        final List<String> vtuberData = getVtuberData(qid).get(0);
        final List<String> genInfo = genAndDateInfo.get(qid);
        final SocialMedias subsInfo = getSocialMedias(qid);
        final Vtuber vtuber = new Vtuber()
                .setId(qid)
                .setJapName(vtuberData.get(2))
                .setHeight(vtuberData.get(5))
                .setStartTime(getDate(genInfo.get(3)))
                .setGeneration(genInfo.get(2))
                .setHairColor(vtuberData.get(4))
                .setEyeColor(vtuberData.get(3))
                .setName(vtuberData.get(0));
        if (subsInfo.getTwitter().getUrl() != null) {
            vtuber.setTwitterUrl(subsInfo.getTwitter().getUrl());
            vtuber.setTwitterSubs(subsInfo.getTwitter().getNbSubs());
        }
        if (subsInfo.getYt().getUrl() != null) {
            vtuber.setYtUrl(subsInfo.getYt().getUrl());
            vtuber.setYtSubs(subsInfo.getYt().getNbSubs());
            vtuber.setImageUrl(getImageUrl(subsInfo.getYt().getUrl()));
        }
        return vtuber;

    }

    private static String getImageUrl(final String ytUrl) {
        final String sURL = "https://api.holotools.app/v1/channels/youtube/" + ytUrl;

        try {
            final URL url = new URL(sURL);
            final URLConnection request;
            request = url.openConnection();
            request.connect();
            final JsonParser jp = new JsonParser();
            final JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            final JsonObject rootobj = root.getAsJsonObject();
            return rootobj.get("photo").getAsString();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Date getDate(final String date) {
        return Date.from(LocalDateTime.parse(date.substring(0, date.length() - 1)).atZone(ZoneId.systemDefault()).toInstant());
    }

    private static String getName(final String qid) {
        final List<String> entries = Collections.singletonList("?label");
        final String queryString = """
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX wd: <http://www.wikidata.org/entity/>
                SELECT  ?label
                WHERE {
                        wd:$id rdfs:label ?label .
                        FILTER (langMatches( lang(?label), "EN" ) )
                      }
                LIMIT 1
                """.replace("$id", qid);
        final Query query = QueryFactory.create(queryString);
        final QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
        final ResultSet results = qexec.execSelect();
        return getResultAsStringList(entries, results).get(0).get(0);

    }

    public static Map<String, List<String>> getVtuberGenerationAndDate() {
        final List<String> entries = Arrays.asList("?vtuber", "?vtuberLabel", "?generationLabel", "?date_debutLabel");
        final String queryString = """
                PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                PREFIX bd: <http://www.bigdata.com/rdf#>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX wd: <http://www.wikidata.org/entity/>
                PREFIX pq: <http://www.wikidata.org/prop/qualifier/>
                PREFIX ps: <http://www.wikidata.org/prop/statement/>
                PREFIX wikibase: <http://wikiba.se/ontology#>
                PREFIX p: <http://www.wikidata.org/prop/>
                SELECT ?vtuber ?vtuberLabel ?generationLabel ?date_debutLabel
                WHERE
                {
                     wd:Q69583635 p:P527 ?statement.
                     ?statement ps:P527 ?vtuber.
                     ?statement pq:P580 ?date_debut .
                     ?statement pq:P361 ?generation .

                     SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
                }
                ORDER BY ?vtuber
                """;

        final Query query = QueryFactory.create(queryString);
        final QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
        final ResultSet results = qexec.execSelect();
        final List<List<String>> resultAsStringList = getResultAsStringList(entries, results);
        final Map<String, List<String>> vtuberMap = new HashMap<>();
        String s;
        for (final List<String> vtuber : resultAsStringList) {
            s = vtuber.get(0);
            s = s.substring(s.lastIndexOf("/") + 1);
            vtuberMap.put(s, vtuber);
        }
        return vtuberMap;
    }

    public static SocialMedias getSocialMedias(final String qid) {
        return new SocialMedias().setTwitter(getTwitter(qid)).setYt(getYt(qid));
    }

    public static SocialMedia getTwitter(final String qid) {
        final String queryString = """
                PREFIX bd: <http://www.bigdata.com/rdf#>
                PREFIX wd: <http://www.wikidata.org/entity/>
                PREFIX pq: <http://www.wikidata.org/prop/qualifier/>
                PREFIX ps: <http://www.wikidata.org/prop/statement/>
                PREFIX wikibase: <http://wikiba.se/ontology#>
                PREFIX p: <http://www.wikidata.org/prop/>
                SELECT (Max(?nbSubs) as ?max_nbSubs) ?id
                WHERE
                {
                     wd:$qid p:P8687 ?statement.
                     ?statement ps:P8687 ?nbSubs.
                     ?statement pq:P6552 ?id .
                     SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
                }
                GROUP BY ?id
                """.replace("$qid", qid);
        return getSocialMedia(queryString);
    }

    public static SocialMedia getYt(final String qid) {
        final String queryString = """
                PREFIX bd: <http://www.bigdata.com/rdf#>
                PREFIX wd: <http://www.wikidata.org/entity/>
                PREFIX pq: <http://www.wikidata.org/prop/qualifier/>
                PREFIX ps: <http://www.wikidata.org/prop/statement/>
                PREFIX wikibase: <http://wikiba.se/ontology#>
                PREFIX p: <http://www.wikidata.org/prop/>
                SELECT (Max(?nbSubs) as ?max_nbSubs)  ?id
                WHERE
                {
                     wd:$qid p:P8687 ?statement.
                     ?statement ps:P8687 ?nbSubs.
                     ?statement pq:P2397 ?id .
                                
                     SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
                }
                GROUP BY ?id
                """.replace("$qid", qid);
        return getSocialMedia(queryString);
    }

    private static SocialMedia getSocialMedia(final String queryString) {
        final List<String> entries = Arrays.asList("?max_nbSubs", "?id");
        final Query query = QueryFactory.create(queryString);
        final QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
        final ResultSet results = qexec.execSelect();
        final List<List<String>> resultAsStringListOpt = getResultAsStringList(entries, results);
        if (resultAsStringListOpt.size() > 0) {
            final List<String> resultAsStringList = resultAsStringListOpt.get(0);
            return new SocialMedia().setNbSubs(Long.parseLong(resultAsStringList.get(0))).setUrl(resultAsStringList.get(1));
        } else {
            return new SocialMedia();
        }

    }

    public static List<List<String>> getVtuberData(final String qid) {
        final List<String> entries = Arrays.asList("?label", "?date_debut", "?native_name", "?eye_color_label", "?hair_color_label", "?height");
        final String queryString = """
                PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX wd: <http://www.wikidata.org/entity/>
                SELECT  ?label ?date_debut ?native_name ?eye_color_label ?hair_color_label ?height
                WHERE {
                  wd:$qid rdfs:label ?label.
                  OPTIONAL { wd:$qid wdt:P2048 ?height. }
                  OPTIONAL { wd:$qid wdt:P2031 ?date_debut. }
                  OPTIONAL { wd:$qid wdt:P1559 ?native_name. }
                  OPTIONAL {
                    wd:$qid wdt:P1340 ?eye_color.
                    ?eye_color rdfs:label ?eye_color_label.
                    FILTER(LANGMATCHES(LANG(?eye_color_label), "en"))
                  }
                  OPTIONAL {
                    wd:$qid wdt:P1884 ?hair_color.
                    ?hair_color rdfs:label ?hair_color_label.
                    FILTER(LANGMATCHES(LANG(?hair_color_label), "en"))
                  }
                  FILTER(LANGMATCHES(LANG(?label), "EN"))
                }
                LIMIT 1""".replace("$qid", qid);
        final Query query = QueryFactory.create(queryString);
        final QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
        final ResultSet results = qexec.execSelect();
        return getResultAsStringList(entries, results);
    }

    private static List<List<String>> getResultAsStringList(final List<String> entries, final ResultSet results) {
        final List<List<String>> resultsString = new ArrayList<>();
        while (results.hasNext()) {
            final ArrayList<String> values = new ArrayList<>();
            final QuerySolution next = results.nextSolution();
            entries.stream().map(entry -> Optional.ofNullable(next.get(entry))).forEach(value -> {
                if (value.isPresent()) {
                    String val = value.get().toString();
                    if (val.contains("^")) {
                        val = StringUtils.substringBefore(val, '^');
                    } else if (val.contains("@")) {
                        val = StringUtils.substringBefore(val, '@');
                    }
                    values.add(val);
                } else {
                    values.add("");
                }
            });
            resultsString.add(values);
        }
        results.close();
        return resultsString;
    }

}

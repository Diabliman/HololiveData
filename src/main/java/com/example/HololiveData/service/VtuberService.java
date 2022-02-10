package com.example.HololiveData.service;

import com.example.HololiveData.model.Vtuber;
import com.example.HololiveData.queries.SparqlQueries;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VtuberService {

    List<Vtuber> vtubers = SparqlQueries.getVtuberList().stream().map(SparqlQueries::buildVtuber).collect(Collectors.toList());

    public List<Vtuber> getVtubers() {
        return this.vtubers;
    }



    public Set<String> getGenerations() {
        return vtubers.stream().map(Vtuber::getGeneration).collect(Collectors.toSet());
    }

    public Vtuber getVtuberByID(String id){
        for (Vtuber v: vtubers) {
            if (v.getId().equals(id)){
                return v;
            }
        }
        return null;
    }

    public Map<String, List<Vtuber>> getVtuberOrderedPerGen() {
        final Map<String, List<Vtuber>> vtuberOrdered = new HashMap<>();
        for (final Vtuber v : vtubers) {
            if (vtuberOrdered.containsKey(v.getGeneration())) {
                vtuberOrdered.get(v.getGeneration()).add(v);
            } else {
                vtuberOrdered.put(v.getGeneration(), new ArrayList<>());
                vtuberOrdered.get(v.getGeneration()).add(v);
            }
        }
        return vtuberOrdered;
    }
}

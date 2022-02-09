package com.example.HololiveData.service;

import com.example.HololiveData.model.Vtuber;
import com.example.HololiveData.queries.SparqlQueries;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VtuberService {

    public List<Vtuber> getVtubers() {
        return SparqlQueries.getVtuberList().stream().map(SparqlQueries::buildVtuber).collect(Collectors.toList());
    }
}
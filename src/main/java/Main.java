import model.Vtuber;
import queries.SparqlQueries;

import java.util.List;
import java.util.stream.Collectors;

import static queries.SparqlQueries.getVtuberList;

public class Main {
    public static void main(final String[] args) {
        final List<Vtuber> vtubers;
        vtubers = getVtuberList().stream().map(SparqlQueries::buildVtuber).collect(Collectors.toList());
        vtubers.forEach(it -> System.out.println(it.getName()));
    }


}

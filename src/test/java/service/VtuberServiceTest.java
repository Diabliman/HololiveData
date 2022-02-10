package service;

import com.example.HololiveData.model.SocialMedias;
import com.example.HololiveData.model.Vtuber;
import com.example.HololiveData.queries.SparqlQueries;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ComponentScan
public class VtuberServiceTest {

    @Test
    public void testGeneration() {
        final Map<String, List<String>> vtubers = SparqlQueries.getVtuberGenerationAndDate();
        Assertions.assertFalse(vtubers.keySet().stream().anyMatch(it -> !it.startsWith("Q")));

        final Set<String> generations = SparqlQueries.getGenerations();
        assertThat(generations.size()).isEqualTo(11);
    }

    @Test
    public void testFollowers() {
        final SocialMedias socialMedia = SparqlQueries.getSocialMedias("Q60649413");

        assertThat(socialMedia.getTwitter().getUrl()).isNotNull();
        assertThat(socialMedia.getYt().getUrl()).isNotNull();
    }

    @Test
    public void buildVtuber() {
        final Vtuber vtuber = SparqlQueries.buildVtuber("Q104732671");

        System.out.println(vtuber.getEyeColor());
    }

    @Test
    public void buildAllVtubers() {
        final List<Vtuber> collect = SparqlQueries.getVtuberList().stream().map(SparqlQueries::buildVtuber).collect(Collectors.toList());

        assertThat(collect.size()).isGreaterThan(0);
    }
}

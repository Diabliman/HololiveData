package service;

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
        final List<List<String>> socialMedia = SparqlQueries.getSocialMedias("Q60649413");

        assertThat(socialMedia.size()).isEqualTo(2);
    }

    @Test
    public void buildVtuber() {
        final Vtuber sora = SparqlQueries.buildVtuber("Q60649413");

        System.out.println(sora.getEyeColor());
    }
}

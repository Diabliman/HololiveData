package service;

import com.example.HololiveData.queries.SparqlQueries;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@ComponentScan
public class VtuberServiceTest {
    

    @Test
    public void test() {
        final List<List<String>> list = SparqlQueries.getAllInfo("Q60649413");
        System.out.println(list);
    }
}

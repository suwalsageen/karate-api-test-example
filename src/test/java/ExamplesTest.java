import com.intuit.karate.KarateOptions;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.junit4.Karate;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertTrue;

@KarateOptions(
        features =
                {
                        "classpath:examples/s1-crud-users.feature",

                }
)
public class ExamplesTest {

    @Test
    public void testParallel() {

        System.out.println("System Property [karate.env]:" + System.getProperty("karate.env"));

        Results results = Runner.parallel(getClass(), 1);
        assertTrue(results.getErrorMessages(), results.getFailCount() == 0);
    }
}
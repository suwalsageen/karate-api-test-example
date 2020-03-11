import com.intuit.karate.KarateOptions;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.junit4.Karate;
import examples.utils.FileUtilities;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        cleanReportDirectory();
        Results results = Runner.parallel(getClass(), 1);
        generateReport(results.getReportDir());
        assertTrue(results.getErrorMessages(), results.getFailCount() == 0);
    }

    public static void generateReport(String karateOutputPath) {
        Collection<File> jsonFiles = FileUtils.listFiles(new File(karateOutputPath), new String[]{"json"}, true);
        List<String> jsonPaths = new ArrayList(jsonFiles.size());
        jsonFiles.forEach(file -> jsonPaths.add(file.getAbsolutePath()));
        Configuration config = new Configuration(new File("target"), "Fuse AI");
        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        reportBuilder.generateReports();
        ReportGenerator.generateReport();
    }

    public static void cleanReportDirectory(){
        try {
            File reportDirectory = new File(FileUtilities.getReportFolder());
            FileUtils.cleanDirectory(reportDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
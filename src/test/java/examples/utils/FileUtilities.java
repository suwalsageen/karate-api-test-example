package examples.utils;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileUtilities {

    public static void main(String[] args) {
        listAllJsonFiles();
    }

    public static String getResourceFolder() throws IOException {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        return s + "/src/test/resources";

    }

    public static String getReportFolder() {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        return s + "/target/surefire-reports";

    }

    public static String readReportJson(String fullPath) {
        File file = new File(fullPath);
        String concatString = "";
        try {
            List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                concatString = concatString.concat(line);
            }
            return concatString.trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<String> listAllJsonFiles(){
        String filePath = getReportFolder();
        Collection<File> jsonFiles = FileUtils.listFiles(new File(filePath), new String[]{"json"}, true);
        List<String> jsonPaths = new ArrayList(jsonFiles.size());
        jsonFiles.forEach(file -> jsonPaths.add(file.getAbsolutePath()));
//        jsonFiles.forEach(file -> System.out.println("-->"+ file));
        return jsonPaths;
    }
}

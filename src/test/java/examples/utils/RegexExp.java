package examples.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExp {
    public static void main(String[] args) {

        String s1 = "def applicationOnBatch = call read('classpath:api_test/school_admin/batches/batches-create.feature') newBatchApplicationOn";
        String s2 = "api_test/school_admin/batches/batches-create.feature";
//        System.out.println(compareFileName(s1,s2));
//        System.out.println(isCallPatternMatch(s2));
        System.out.println(getCallPath(s1));

    }

    public static boolean compareFileName(String s1, String s2){
        Pattern p = Pattern.compile("(call read\\('classpath:)(.+\\.feature)");//. represents single character
        Matcher m = p.matcher(s1);
        if(m.find()) {
            System.out.println(m.group(2));
            String findFileNameCall = m.group(2);
            if (s2.equals(findFileNameCall)) {
                return true;
            }
        }
            return false;
    }

    public static boolean isCallPatternMatch(String callString){
        //1st way
        Pattern p = Pattern.compile("(call read\\('classpath:)(.+\\.feature)");//. represents single character
        Matcher m = p.matcher(callString);
        if(m.find()) {

                return true;
        }
        return false;
    }

    public static String getCallPath(String s1){
        Pattern p = Pattern.compile("(call read\\('classpath:)(.+\\.feature)");//. represents single character
        Matcher m = p.matcher(s1);
        if(m.find()) {
//            System.out.println(m.group(2));
            return m.group(2);

        }
        return null;
    }

    public static String getApiName(String str){
//        api_test/school_admin/batches/batches-create.feature
        String[] strSplit = str.split("/");
        String apiName = strSplit[strSplit.length-1].replace(".feature","");
        apiName = apiName.replaceAll("-"," ");
        return StringUtils.capitalize(apiName);
    }
}

package examples.utils;

import org.apache.commons.lang.RandomStringUtils;

public class StringUtils {

    public static void main(String[] args) {
        getRandomString();
    }
 
    public static String getNumberFromString(String text) {
        return text.replaceAll("\\D+", "");
    }

    public static String getRandomString() {

        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);

        System.out.println(generatedString);
        return generatedString;
    }
}
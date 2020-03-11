package examples.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    private static long dayInMillis =  86400000;


    public static void main(String[] args) {



//        OffsetDateTime odt = OffsetDateTime.of( 2019 , 11 , 28 , 0 , 0 , 0 , 0 , ZoneOffset.UTC);
//        ZonedDateTime asdf = odt.toZonedDateTime();
//        System.out.println(asdf.toString());
//        System.out.println("to epoch"+odt.toEpochSecond());


        System.out.println(getCurrentDateTime());

    }

    public static String getStartOfTheDay(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        System.out.println("After Reset: " +calendar.getTimeInMillis());
        return Long.toString(calendar.getTimeInMillis());
    }
    public static String getEndOfTheDay(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        System.out.println("After Reset: " +calendar.getTimeInMillis());
        return Long.toString(calendar.getTimeInMillis());
    }


    public static String getCurrentYear(){

        int year =  Calendar.getInstance().get(Calendar.YEAR);
        return Integer.toString(year);
    }
    public static String getCurrentMonth(){

        int month =  Calendar.getInstance().get(Calendar.MONTH);
        return Integer.toString(month);
    }

    public static long getTimeInMillis(int day){
        long currentTime = System.currentTimeMillis();
        return currentTime + (day * dayInMillis);
    }
    public static String getInvoiceDateFormat(String strDate){

//        String strDate = "22nd Nov, 2019";
        String dateTh = strDate.split(" ")[0];
        dateTh = dateTh.replaceAll("([A-Z | a-z]*)","");

        String month = strDate.split(" ")[1].replace(",","").trim();
        String year = strDate.split(" ")[2].trim();

        String finalInputDate = dateTh.concat(" ").concat(month).concat(" ").concat(year);
        System.out.println(finalInputDate);

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd MMM yyyy");
        try {
            Date date= inputDateFormat.parse(finalInputDate);
            System.out.println(date.toString());
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String outputDate = outputDateFormat.format(date);
            System.out.println(outputDate);
            return outputDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCurrentDateTime(){
        try {

            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMM dd, yyyy-HH:mm:ss");
            String outputDate = outputDateFormat.format(new Date());
            System.out.println(outputDate);
            return outputDate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

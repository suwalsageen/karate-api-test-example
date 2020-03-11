package examples.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelWriter {

    //    private static String[] columns = {"Name", "Email", "Date Of Birth", "Salary"};
    private static String[] columns = {"File", "Feature", "APIs", "Scenarios", "Status"};
//    private static List<Employee> employees = new ArrayList<>();

    public static void generateExcelReport(JSONObject apiReportJson) {

        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
        CreationHelper createHelper = workbook.getCreationHelper();
        // Create a Sheet
        Sheet sheet = workbook.createSheet("karate report");
        // Create a Font for styling header cells
        CellStyle headerCellStyle = getHeaderCellStyle(workbook);
        CellStyle passCellStyle = getPassCellStyle(workbook);
        CellStyle failCellStyle = getFailCessStyle(workbook);

        // Create a Row
        Row headerRow = sheet.createRow(0);
        // Create cells
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        JSONArray apiReportArray = apiReportJson.getJSONArray("featureReports");
        int rowNum = 1;
        for (int a = 0; a < apiReportArray.length(); a++) {
            JSONObject featureJsonObject = apiReportArray.getJSONObject(a);

            boolean isNewFeature = true;

            String featureFile = featureJsonObject.getString("featureFile");
            String featureName = featureJsonObject.getString("feature");

            JSONArray apiArray = featureJsonObject.getJSONArray("apis");

            for (int i = 0; i < apiArray.length(); i++) {
                JSONObject stepJson = apiArray.getJSONObject(i);
                try{
                    String apiName = stepJson.getString("apiName");
                    JSONArray scenarioArray = stepJson.getJSONArray("scenarios");
                    for (int j = 0; j < scenarioArray.length(); j++) {
                        JSONObject scenarioJson = scenarioArray.getJSONObject(j);
                        String scenarioName = scenarioJson.getString("scenarioName");
                        String status = scenarioJson.getString("status");

                        //ADD ROW
//                    {"Feature", "Steps", "Scenarios", "Status", "Total Scenarios", "File"};
                        Row row = sheet.createRow(rowNum++);

                        if (a >0 && isNewFeature && j == 0) {

                            row = sheet.createRow(rowNum++);
                        }
                        if (isNewFeature && j == 0) {
                            row.createCell(0).setCellValue(featureFile);
                            row.createCell(1).setCellValue(featureName);
                            row.createCell(2).setCellValue(apiName);
                            row.createCell(3).setCellValue(scenarioName);
                            if(status.equals("Passed")){
                                Cell statusCell = row.createCell(4);
                                statusCell.setCellValue(status);
                                statusCell.setCellStyle(passCellStyle);
                            }
                            else{
                                Cell statusCell = row.createCell(4);
                                statusCell.setCellValue(status);
                                statusCell.setCellStyle(failCellStyle);
                            }
                            isNewFeature = false;
                        } else if (!isNewFeature && j != 0) {
                            row.createCell(0).setCellValue("");
                            row.createCell(1).setCellValue("");
                            row.createCell(2).setCellValue("");
                            row.createCell(3).setCellValue(scenarioName);
                            if(status.equals("Passed")){
                                Cell statusCell = row.createCell(4);
                                statusCell.setCellValue(status);
                                statusCell.setCellStyle(passCellStyle);
                            }
                            else{
                                Cell statusCell = row.createCell(4);
                                statusCell.setCellValue(status);
                                statusCell.setCellStyle(failCellStyle);
                            }
//                        row.createCell(5).setCellValue(0);
                        } else if (!isNewFeature && j == 0) {
                            row.createCell(0).setCellValue("");
                            row.createCell(1).setCellValue("");
                            row.createCell(2).setCellValue(apiName);
                            row.createCell(3).setCellValue(scenarioName);
                            if(status.equals("Passed")){
                                Cell statusCell = row.createCell(4);
                                statusCell.setCellValue(status);
                                statusCell.setCellStyle(passCellStyle);
                            }
                            else{
                                Cell statusCell = row.createCell(4);
                                statusCell.setCellValue(status);
                                statusCell.setCellStyle(failCellStyle);
                            }
//                        row.createCell(5).setCellValue(0);
                        }

                    }
                }
                catch (Exception e){}


            }
        }

        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Total APIs Tested: "+ apiReportJson.getInt("totalApis"));

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        // Write the output to a file
        try{
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh-mm-ss");
            String strDate = formatter.format(date);
            System.out.println("Date Format with dd-M-yyyy hh:mm:ss : "+strDate);

            new File(FileUtilities.getResourceFolder()).mkdirs();
            String fileName = FileUtilities.getResourceFolder().concat("/").concat("Fuse-AI-API-Test-Report-").concat(strDate).concat(".xlsx");
            FileOutputStream fileOut = new FileOutputStream(fileName);
//                FileOutputStream fileOut = new FileOutputStream("poi-generated-file.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            // Closing the workbook
            workbook.close();
        }catch (Exception e){}

    }

    public static CellStyle getHeaderCellStyle(Workbook workbook) {
        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());

        return headerCellStyle;
    }

    public static CellStyle getPassCellStyle(Workbook workbook) {

        Font passFont = workbook.createFont();
        passFont.setBold(true);
        passFont.setFontHeightInPoints((short) 10);
        passFont.setColor(IndexedColors.BLACK.getIndex());

        // Create a CellStyle with the font
        CellStyle passCellStyle = workbook.createCellStyle();
        passCellStyle.setFont(passFont);
        passCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        passCellStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN1.getIndex());
        return passCellStyle;

    }

    public static CellStyle getFailCessStyle(Workbook workbook) {
        Font failFont = workbook.createFont();
        failFont.setBold(true);
        failFont.setFontHeightInPoints((short) 10);
        failFont.setColor(IndexedColors.BLACK.getIndex());

        // Create a CellStyle with the font
        CellStyle failCellStyle = workbook.createCellStyle();
        failCellStyle.setFont(failFont);
        failCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        failCellStyle.setFillForegroundColor(IndexedColors.RED1.getIndex());
        return failCellStyle;
    }
}
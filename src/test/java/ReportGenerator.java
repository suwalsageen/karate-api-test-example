import examples.utils.DateUtils;
import examples.utils.ExcelWriter;
import examples.utils.FileUtilities;
import examples.utils.RegexExp;
import examples.utils.enums.EnvVarType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ReportGenerator {

    private static String JSON_PATH = "examples.s";
    public static void main(String[] args) {
        generateReport();

    }

    public static void generateReport() {
        List<String> jsonFilePathList = FileUtilities.listAllJsonFiles();

        List<String> sortedJsonFilePathList = Arrays.asList(
                jsonFilePathList.stream().sorted(
                        (s1, s2) -> s1.compareToIgnoreCase(s2)
                ).toArray(String[]::new)
        );

        Map<Integer,String> pathMap = new TreeMap<>();

        for(String jsonFilePath: jsonFilePathList){
            String[] splitString = jsonFilePath.split(JSON_PATH);
            String fileName = splitString[splitString.length-1];
            String numStr = fileName.split("-")[0];
            Integer numInt = Integer.parseInt(numStr);

            pathMap.put(numInt, jsonFilePath);
        }

        for (Map.Entry<Integer, String> entry : pathMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        int totalAPI = 0;
        int totalScenarios = 0;
        int totalApiFailed = 0;
        JSONObject mainJsonReport = new JSONObject();
        JSONArray mainArrayReport = new JSONArray();

        Set<String> apiNameSet = new HashSet<>();
        List<String> apiNameList = new ArrayList<>();
//        for (String jsonFilePath : sortedJsonFilePathList) {
            for (Map.Entry<Integer, String> entry : pathMap.entrySet()) {
                String jsonFilePath = entry.getValue();
            JSONArray jsonArray = new JSONArray(FileUtilities.readReportJson(jsonFilePath));
            JSONObject sourceJsonObject = jsonArray.getJSONObject(0);

            JSONObject featureReportJson = new JSONObject();
            featureReportJson.put("featureFile", sourceJsonObject.getString("name"));
            featureReportJson.put("feature", sourceJsonObject.getString("description"));

            JSONArray scenariosArray = new JSONArray();
            JSONObject scenarioJson = new JSONObject();
            JSONArray apisArray = new JSONArray();
            JSONObject apiJson = new JSONObject();

            JSONArray elementArray = sourceJsonObject.getJSONArray("elements");

            long totalTimeTaken = 0;
            String currentApiName= "";
            String previousApiName= "";
            boolean isAllSubStepsPass = true;
            for (int i = 0; i < elementArray.length(); i++) {

                JSONArray stepArray = elementArray.getJSONObject(i).getJSONArray("steps");
//                boolean isStepBegins = false;
                String currentStepName = "";
                boolean isApiCallSameAsPrev = false;
                String previousApiCall = "";
                String errorMessage = "";

                for (int j = 0; j < stepArray.length(); j++) {


                    //logic starts from here
                    JSONObject stepJson = stepArray.getJSONObject(j);
                    String stepName = stepJson.getString("name");
                    JSONObject resultJson = stepJson.getJSONObject("result");
                    String status = resultJson.getString("status");
                    long duration = resultJson.getLong("duration");

                    if (stepName.contains("<##")) {
                        errorMessage = "";
                        isAllSubStepsPass = true;
                        totalTimeTaken = 0;

                        totalTimeTaken = totalTimeTaken + duration;

                        currentStepName = stepName.split("<##")[1];
                        scenarioJson.put("scenarioName", currentStepName.replace("'", ""));
                    }
                    //check if api is called.. EG: "def applicationOnBatch = call read('classpath:api_test/school_admin/batches/batches-create.feature') newBatchApplicationOn";
                    else if (RegexExp.isCallPatternMatch(stepName)) {
                        String currentApiCall = RegexExp.getCallPath(stepName);
                        totalTimeTaken = totalTimeTaken + duration;

                        currentApiName = RegexExp.getApiName(currentApiCall);

                        isApiCallSameAsPrev = (previousApiCall.equals(currentApiCall)) ? true : false;

                        //if api is different from previous, and previous api is not empty
                        if(!isApiCallSameAsPrev && !previousApiCall.isEmpty()){
                            apiNameSet.add(previousApiName);    apiNameList.add(previousApiName);
                            apiJson.put("apiName", previousApiName);
                            apiJson.put("scenarios",scenariosArray);
                            boolean isAllScenariosPassed = true;
                            for (int k = 0; k < scenariosArray.length() ; k++) {
                                String scenarioStatus = scenariosArray.getJSONObject(k).getString("status");
                                if(scenarioStatus.equals("Failed")){
                                    isAllScenariosPassed = false;
                                    break;
                                }
                            }
                            if(isAllScenariosPassed){
                                apiJson.put("status","Passed");
                            }
                            else{
                                apiJson.put("status","Failed");
                                totalApiFailed++;
                            }
                            apisArray.put(apiJson);

                            apiJson = new JSONObject();
                            scenariosArray = new JSONArray();
                        }
                        previousApiCall = currentApiCall;
                    }
                    else if(stepName.contains("##>")){
                        totalTimeTaken = totalTimeTaken + duration;
                        if(isAllSubStepsPass){
                            scenarioJson.put("status", "Passed");
                        }
                        else{
                            scenarioJson.put("status", "Failed");
                            scenarioJson.put("error_message", errorMessage);
                        }
                        scenarioJson.put("duration", totalTimeTaken);
                        scenariosArray.put(scenarioJson);
                        scenarioJson = new JSONObject();
                        totalTimeTaken=0;
                        previousApiName = currentApiName;
                    }

                    if (status.equals("failed")) {
                        isAllSubStepsPass = false;
                        if(errorMessage.isEmpty()){
                            errorMessage = resultJson.getString("error_message");
                        }
                    }
                }//Actual logic


            }
                apiNameSet.add(previousApiName);    apiNameList.add(previousApiName);
            apiJson.put("apiName", currentApiName);
            apiJson.put("scenarios",scenariosArray);
            boolean isAllScenariosPassed = true;
            for (int k = 0; k < scenariosArray.length() ; k++) {
                String scenarioStatus = scenariosArray.getJSONObject(k).getString("status");
                if(scenarioStatus.equals("Failed")){
                    isAllScenariosPassed = false;
                    break;
                }
            }
            if(isAllScenariosPassed){
                apiJson.put("status","Passed");
            }
            else{
                apiJson.put("status","Failed");
                totalApiFailed++;
            }
            apisArray.put(apiJson);

            featureReportJson.put("apis", apisArray);

            boolean isAllApiPassed = true;
            for (int i = 0; i < apisArray.length() ; i++) {
                String statusPerApi = apisArray.getJSONObject(i).getString("status");
                if(statusPerApi.equals("Failed")){
                    isAllApiPassed = false;
                }
            }

            if(isAllApiPassed){
                featureReportJson.put("status", "Passed");
            }
            else{
                featureReportJson.put("status", "Failed");
            }

            mainArrayReport.put(featureReportJson);

        }
        mainJsonReport.put("featureReports", mainArrayReport);
        mainJsonReport.put("totalUniqueApi", apiNameSet.size());
        mainJsonReport.put("totalApisCalled", apiNameList.size());
        mainJsonReport.put("totalApiPassed", apiNameSet.size()-totalApiFailed);
        mainJsonReport.put("totalApiFailed", totalApiFailed);
        mainJsonReport.put("totalApis", 70);
        mainJsonReport.put("runDate", DateUtils.getCurrentDateTime());
        mainJsonReport.put("project_id", "5dede716beacde00227d5b06");
        String envVar = System.getProperty("karate.env");
        EnvVarType envVarType = EnvVarType.STAGE;

        try{
            if(envVar.equals("stage")){
                envVarType = EnvVarType.STAGE;
            }else if(envVar.equals("dev")){
                envVarType = EnvVarType.DEV;
            }
        }catch (Exception e){
            System.out.println("No ENV were set, Using Default: "+ envVarType);
        }
        mainJsonReport.put("env", envVarType);
        System.out.println(envVarType);
        System.out.println("Final Output: " + mainJsonReport.toString());
//        try {
//            HttpRequest.submitReportHttpRequest(envVarType, mainJsonReport.toString());
//
////            String sampleJsonBody = "{\"featureReports\":[{\"featureFile\":\"api_test/s1-sa-batch-crud.feature\",\"feature\":\"<b>School Admin</b> Create, Retrieve, Update, Delete the Batch\",\"apis\":[{\"apiName\":\"Batches create\",\"scenarios\":[{\"duration\":4633641551,\"scenarioName\":\"Create New Batch with applicaiton ON\",\"status\":\"Passed\"},{\"duration\":1420060753,\"scenarioName\":\"Create New Batch with applicaiton OFF\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches get all\",\"scenarios\":[{\"duration\":1324364593,\"scenarioName\":\"Get All Batches\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches get by id\",\"scenarios\":[{\"duration\":1254628516,\"scenarioName\":\"Get Batch by ID\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches get by search term\",\"scenarios\":[{\"duration\":1209545212,\"scenarioName\":\"Search Batch By Search Term\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches update by id\",\"scenarios\":[{\"duration\":1320795559,\"scenarioName\":\"Update Batch By ID\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches delete by id\",\"scenarios\":[{\"duration\":1445009719,\"scenarioName\":\"Delete batch With Application ON\",\"status\":\"Passed\"},{\"duration\":1250257906,\"scenarioName\":\"Delete batch With Application OFF\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"featureFile\":\"api_test/s10-st-starts-course.feature\",\"feature\":\"Student selects the program and start course\",\"apis\":[{\"apiName\":\"Get all public program\",\"scenarios\":[{\"duration\":2035639513,\"scenarioName\":\"Student get all public program list\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Global\",\"scenarios\":[{\"duration\":1196778847,\"scenarioName\":\"Student get global course, batch related information\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get notifications\",\"scenarios\":[{\"duration\":1232716436,\"scenarioName\":\"Student get all notifications\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get nonenrolled program\",\"scenarios\":[{\"duration\":1122312574,\"scenarioName\":\"Student get non enrolled programs\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Goto program\",\"scenarios\":[{\"duration\":1235063032,\"scenarioName\":\"Student goto selected program\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get progress tracker 1\",\"scenarios\":[{\"error_message\":\"com.intuit.karate.exception.KarateException: s10-st-starts-course.feature:31 - \\nget-progress-tracker-1.feature:18 - status code was: 400, expected: 200, response time: 1287, url: https://fuse-ai-stage-api.fuse.ai/v2/track?programId=5d92f2d1acc8900e2ee13e69&batchId=5dedcd48acc8903eb944b966, response: {\\\"status\\\":400,\\\"message\\\":\\\"User not bound with program\\\",\\\"timeStamp\\\":1575865728968,\\\"developerMessage\\\":\\\"com.fusemachines.lms.exception.CustomBackendExceptions\\\",\\\"errors\\\":{}}\",\"duration\":1323164277,\"scenarioName\":\"Student get progress tracker report\",\"status\":\"Failed\"}],\"status\":\"Failed\"},{\"apiName\":\"Get modules\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Student get Module Detail\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get unit details\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Student Get Unit Detail by module Id\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get progress tracker by courseId\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Student get Progress tracker under particular course\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Submit quiz\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Student submit the Quiz\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"User delete\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Delete Instructor by User Id\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches stop\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Stop Batch\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches delete by id\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Delete batch\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Failed\"},{\"featureFile\":\"api_test/s11-st-get-programs-by-location.feature\",\"feature\":\"Get program list based on location\",\"apis\":[{\"apiName\":\"Get programs by location\",\"scenarios\":[{\"duration\":1579566536,\"scenarioName\":\"Get Program List based on location Kathmandu\",\"status\":\"Passed\"},{\"duration\":1170896769,\"scenarioName\":\"Get Program List based on location Santo Domino\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"featureFile\":\"api_test/s2-sa-batch-create-run.feature\",\"feature\":\"School Admin creates Batch and then Run\",\"apis\":[{\"apiName\":\"Batches create\",\"scenarios\":[{\"duration\":1484660158,\"scenarioName\":\"Add New Batch With Exam False To generate Invoice and Accept Payment\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches run\",\"scenarios\":[{\"duration\":1253818309,\"scenarioName\":\"Run Batch\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"featureFile\":\"api_test/s3-sa-class-instructor-operation.feature\",\"feature\":\"School Admin Creates new Class Instructor, Retrieve it, Update and delete\",\"apis\":[{\"apiName\":\"Instructor add\",\"scenarios\":[{\"duration\":2504475680,\"scenarioName\":\"Add New Class Instructor for CRUD Operation\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"User get\",\"scenarios\":[{\"duration\":1705948240,\"scenarioName\":\"Get Instructor Detail by User Id\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Instructor update\",\"scenarios\":[{\"duration\":1634318965,\"scenarioName\":\"Update Class Instructor By User Binder ID\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Instructor add\",\"scenarios\":[{\"duration\":1226462107,\"scenarioName\":\"Delete Instructor by User Id\",\"status\":\"Passed\"},{\"duration\":2333602632,\"scenarioName\":\"Add New Class Instructor Once again\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"featureFile\":\"api_test/s4-sa-school-finance.feature\",\"feature\":\"School Admin Creates new Finance Manager, Retrieve it, Update and delete\",\"apis\":[{\"apiName\":\"Finance manager add\",\"scenarios\":[{\"duration\":2305000363,\"scenarioName\":\"Add New Finance Manager For CRUD\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"User get\",\"scenarios\":[{\"duration\":1623603819,\"scenarioName\":\"Get Finance Manager\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Finance manager update\",\"scenarios\":[{\"duration\":1649562518,\"scenarioName\":\"Update Finance Manager\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Finance manager add\",\"scenarios\":[{\"duration\":1738354412,\"scenarioName\":\"Delete Finance Manager\",\"status\":\"Passed\"},{\"duration\":2324911982,\"scenarioName\":\"Add New Finance Manager\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"featureFile\":\"api_test/s5-st-application.feature\",\"feature\":\"Student view his/her profile, View available programs, view course detail under a program, save and update Application\",\"apis\":[{\"apiName\":\"Profile get\",\"scenarios\":[{\"error_message\":\"com.intuit.karate.exception.KarateException: s5-st-application.feature:12 - \\nprofile-get.feature:15 - status code was: 200, expected: 400, response time: 1203, url: https://fuse-ai-stage-api.fuse.ai/api/v2/profiles/profile, response: {\\\"id\\\":\\\"5dc4fbabacc890469b32d412\\\",\\\"fullName\\\":\\\"student three\\\",\\\"email\\\":\\\"student1_stage@yopmail.com\\\",\\\"dob\\\":920419200000,\\\"country\\\":\\\"Nepal\\\",\\\"linkedinId\\\":\\\"https://www.linkedin.com/karateman3\\\",\\\"githubUrl\\\":\\\"https://github.com/karateman3\\\",\\\"rawImage\\\":\\\"\\\",\\\"resumeUrl\\\":\\\"https://s3-us-east-2.amazonaws.com/fuse-ai-resources-stage/resumes/student3_stage@yopmail.com/Untitled+1.doc\\\",\\\"skills\\\":[],\\\"interest\\\":[],\\\"language\\\":[],\\\"profileImage\\\":\\\"\\\",\\\"phoneNumber\\\":[{\\\"id\\\":\\\"5dde734facc8901a49ec0d2f\\\",\\\"phoneNumber\\\":1234567890519,\\\"dialCode\\\":\\\"1\\\",\\\"phoneCountry\\\":\\\"us\\\"}],\\\"studentId\\\":\\\"5dc4fbabacc890469b32d411\\\",\\\"streetAddress\\\":\\\"\\\",\\\"designation\\\":\\\"\\\",\\\"summary\\\":\\\"\\\",\\\"gender\\\":\\\"male\\\",\\\"certificateDetails\\\":[],\\\"educationDetails\\\":[{\\\"academicDegree\\\":\\\"Undergraduate UpdatedProfile\\\",\\\"collegeName\\\":\\\"college Name\\\",\\\"startDate\\\":\\\"2010\\\",\\\"endDate\\\":\\\"2014\\\",\\\"id\\\":\\\"5dd7766facc8903fc6195151\\\",\\\"gpa\\\":3.0}],\\\"experienceDetails\\\":[{\\\"id\\\":\\\"5dd7766facc8903fc6195152\\\",\\\"companyName\\\":\\\"Company Name\\\",\\\"designation\\\":\\\"Designation 1\\\",\\\"startDate\\\":\\\"2013\\\",\\\"endDate\\\":\\\"2015\\\",\\\"jobDescription\\\":\\\"\\\"}],\\\"socialAccountsDetails\\\":[],\\\"accomplishments\\\":[]}\",\"duration\":1229270531,\"scenarioName\":\"View Student Profile\",\"status\":\"Failed\"}],\"status\":\"Failed\"},{\"apiName\":\"Get available program\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Get All available programs\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get course syllabus by program id\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Get Course detail\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Save application\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Save Application to Draft\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Update application\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Complete Application\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Update profile\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Update Student Profile\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Failed\"},{\"featureFile\":\"api_test/s6-sa-student-selection.feature\",\"feature\":\"School Admin Get List of Applicant, Select Applicant then move selected Applicant To Exam\",\"apis\":[{\"apiName\":\"Applicant get all\",\"scenarios\":[{\"duration\":1184558925,\"scenarioName\":\"Get All Applicants\",\"status\":\"Passed\"},{\"duration\":253673,\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Applicant status\",\"scenarios\":[{\"error_message\":\"com.intuit.karate.exception.KarateException: s6-sa-student-selection.feature:17 - \\napplicant-status.feature:18 - status code was: 500, expected: 200, response time: 1289, url: https://fuse-ai-stage-api.fuse.ai/applications/application/status, response: {\\\"timestamp\\\":1575865697941,\\\"status\\\":500,\\\"error\\\":\\\"Internal Server Error\\\",\\\"exception\\\":\\\"java.lang.NullPointerException\\\",\\\"message\\\":\\\"No message available\\\",\\\"path\\\":\\\"/applications/application/status\\\"}\",\"duration\":1319563296,\"scenarioName\":\"Change Application Status Pending to Selected\",\"status\":\"Failed\"}],\"status\":\"Failed\"},{\"apiName\":\"Applicant move to batch\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Move Students to Batch\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Failed\"},{\"featureFile\":\"api_test/s7-fm-gen-invoice.feature\",\"feature\":\"Finance Manager gets those applicants which have been moved to batch, and generate invoice for them and Accept Payment\",\"apis\":[{\"apiName\":\"Get billing detail\",\"scenarios\":[{\"duration\":1323051184,\"scenarioName\":\"Get Billing Detail By School Id\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get programs by school id\",\"scenarios\":[{\"duration\":1195504013,\"scenarioName\":\"Get programs by school Id\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get selected applicant\",\"scenarios\":[{\"duration\":1262469593,\"scenarioName\":\"Get applicants which have been moved to batch\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Send invoice\",\"scenarios\":[{\"error_message\":\"com.intuit.karate.exception.KarateException: s7-fm-gen-invoice.feature:28 - \\nsend-invoice.feature:21 - status code was: 401, expected: 200, response time: 1331, url: https://fuse-ai-stage-api.fuse.ai/api/v2/billing/editandsend?invoiceId=1882888000000909001, response: \",\"duration\":1353325906,\"scenarioName\":\"Send Billing Invoice to Student\",\"status\":\"Failed\"}],\"status\":\"Failed\"},{\"apiName\":\"Billing view\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"View Billing Invoice\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Accept pay\",\"scenarios\":[{\"duration\":0,\"scenarioName\":\"Finance Manager Accept Payment In full\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Failed\"},{\"featureFile\":\"api_test/s8-sa-change-class-opening-date.feature\",\"feature\":\"School Admin Changes Class Opening Date to past for Class Instructor to Start taking Attendance\",\"apis\":[{\"apiName\":\"Batches get by id\",\"scenarios\":[{\"duration\":1284256014,\"scenarioName\":\"Get Batch by ID to update Object Store\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Batches update by id\",\"scenarios\":[{\"duration\":1355019342,\"scenarioName\":\"Update Batch With Exam False For Class Instructor\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"featureFile\":\"api_test/s9-ci-start-course-attendance.feature\",\"feature\":\"Class Instructor starts course, Save record of the student's presence in the classroom\",\"apis\":[{\"apiName\":\"Get program list\",\"scenarios\":[{\"duration\":1279647685,\"scenarioName\":\"Get list of program\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get batch detail\",\"scenarios\":[{\"duration\":1227129522,\"scenarioName\":\"Get Batch Detail\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get course detail for attendance\",\"scenarios\":[{\"duration\":1222381060,\"scenarioName\":\"Get Course Detail\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get module detail\",\"scenarios\":[{\"duration\":1234245348,\"scenarioName\":\"Get Module Detail\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get units\",\"scenarios\":[{\"duration\":1225273084,\"scenarioName\":\"Get Units\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get chapter details\",\"scenarios\":[{\"duration\":1218312103,\"scenarioName\":\"Show Chapters\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Set chapter status\",\"scenarios\":[{\"duration\":2413342685,\"scenarioName\":\"Set Chapter Status\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get attendance student\",\"scenarios\":[{\"duration\":1276002502,\"scenarioName\":\"Get Attendance Student\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Take attendance\",\"scenarios\":[{\"duration\":1207915720,\"scenarioName\":\"save attendance as present\",\"status\":\"Passed\"},{\"duration\":1228792040,\"scenarioName\":\"save attendance as Absent\",\"status\":\"Passed\"}],\"status\":\"Passed\"},{\"apiName\":\"Get attendance report\",\"scenarios\":[{\"duration\":1202960192,\"scenarioName\":\"Get Attendance Report\",\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"status\":\"Passed\"}],\"total\":0,\"totalApis\":50,\"env\":\"STAGE\",\"project_id\":\"5d564763cedb0c24d860c196\"}";
////            HttpRequest.submitReportHttpRequest(envVarType, sampleJsonBody);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ExcelWriter.generateExcelReport(mainJsonReport);
    }


}

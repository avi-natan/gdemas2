package gdemas;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gdemas.Utils.listDirectories;
import static gdemas.Utils.print;

public class P06ResultsCollector {

    public static void execute(int[] faultNumbers, int repeatNumber, String[] observabilities) {
        File inputFolder = new File("benchmarks/mastrips/06 - results");
        File outputFile = new File("benchmarks/mastrips/06 - results/resultsFile.xls");

        List<Record2> records = new ArrayList<>();

        int countYes = 0;
        int countNo = 0;

        File[] domainFolders = listDirectories(inputFolder);
        for (File domainFolder: domainFolders) {
            File[] problemFolders = listDirectories(domainFolder);
            for (File problemFolder: problemFolders) {
                for (Integer f : faultNumbers) {
                    for (int r = 0; r < repeatNumber; r++) {
                        for (String o : observabilities) {
                            File resultSimple = new File(problemFolder,
                                    "/" + f + "/" + domainFolder.getName() + "-" + problemFolder.getName() +
                                    "-f[" + f + "]" + "-r[" + r + "]" + "-" + o + "-simple-results.txt");
                            File resultSmart = new File(problemFolder,
                                    "/" + f + "/" + domainFolder.getName() + "-" + problemFolder.getName() +
                                    "-f[" + f + "]" + "-r[" + r + "]" + "-" + o + "-smart-results.txt");
                            File resultAmazing5 = new File(problemFolder,
                                    "/" + f + "/" + domainFolder.getName() + "-" + problemFolder.getName() +
                                            "-f[" + f + "]" + "-r[" + r + "]" + "-" + o + "-amazing5-results.txt");
                            File resultWow = new File(problemFolder,
                                    "/" + f + "/" + domainFolder.getName() + "-" + problemFolder.getName() +
                                            "-f[" + f + "]" + "-r[" + r + "]" + "-" + o + "-wow-results.txt");
                            File resultSuperb = new File(problemFolder,
                                    "/" + f + "/" + domainFolder.getName() + "-" + problemFolder.getName() +
                                            "-f[" + f + "]" + "-r[" + r + "]" + "-" + o + "-superb-results.txt");
                            print(resultSimple.getAbsolutePath());

                            String comparable = checkComparable(new File[] {resultSimple, resultSmart, resultAmazing5, resultWow, resultSuperb});

                            if (resultSimple.exists()) {
                                records.add(createSuccessfulRecord(resultSimple, comparable));
                                countYes += 1;
                            } else {
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "simple"));
                                countNo += 1;
                            }

                            if (resultSmart.exists()) {
                                records.add(createSuccessfulRecord(resultSmart, comparable));
                                countYes += 1;
                            } else {
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "smart"));
                                countNo += 1;
                            }

                            if (resultAmazing5.exists()) {
                                records.add(createSuccessfulRecord(resultAmazing5, comparable));
                                countYes += 1;
                            } else {
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "amazing5"));
                                countNo += 1;
                            }

                            if (resultWow.exists()) {
                                records.add(createSuccessfulRecord(resultWow, comparable));
                                countYes += 1;
                            } else {
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "wow"));
                                countNo += 1;
                            }

                            if (resultSuperb.exists()) {
                                records.add(createSuccessfulRecord(resultSuperb, comparable));
                                countYes += 1;
                            } else {
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "superb"));
                                countNo += 1;
                            }

                        }
                    }
                }
            }
        }
        print("yes: " + countYes);
        print("no: " + countNo);

        saveRecordsToExcel(records, outputFile);
        print(9);
    }

    private static String checkComparable(File[] resultFiles) {
        boolean comparable = true;
        for (File resultFile : resultFiles) {
            if (!resultFile.exists()) {
                comparable = false;
            } else {
                String resultString = Parser.readFromFile(resultFile);
                String timeOutEntry = resultString.split("\r\n")[27];
                String timeoutValue = timeOutEntry.split(":")[1];
                if (timeoutValue.equals("1")) {
                    comparable = false;
                }
            }
        }
        if (comparable) {
            return "yes";
        } else {
            return "no";
        }
    }

    private static Record2 createSuccessfulRecord(File resultsFile, String comparable) {
        Record2 rec = new Record2();

        String[] resultStrings = Parser.readFromFile(resultsFile).split("\r\n");

        // output members
        // controlled parameters
        int c = 0;
        rec._BENCHMARK_NAME            = resultStrings[c++].split(":")[1];
        rec._DOMAIN_NAME               = resultStrings[c++].split(":")[1];
        rec._PROBLEM_NAME              = resultStrings[c++].split(":")[1];
        rec._FAULTS_NUM                = resultStrings[c++].split(":")[1];
        rec._REPETITION_NUM            = resultStrings[c++].split(":")[1];
        rec._OBSERVABILITY             = resultStrings[c++].split(":")[1];
        rec._REASONER_NAME             = resultStrings[c++].split(":")[1];

        // instance non-controlled parameters
        rec._AGENTS_NUM                = resultStrings[c++].split(":")[1];
        rec._GOAL_SIZE                 = resultStrings[c++].split(":")[1];
        rec._GOAL_AGENT_RATIO          = resultStrings[c++].split(":")[1];
        rec._PLAN_LENGTH               = resultStrings[c++].split(":")[1];
        rec._COEFFICIENT_OF_VARIATION  = resultStrings[c++].split(":")[1];

        // measurement members
        rec._MODELLING_AGENT_NAME      = resultStrings[c++].split(":")[1];
        rec._MODELLING_PREDICATES_NUM  = resultStrings[c++].split(":")[1];
        rec._MODELLING_ACTIONS_NUM     = resultStrings[c++].split(":")[1];
        rec._MODELLING_VARIABLES_NUM   = resultStrings[c++].split(":")[1];
        rec._MODELLING_CONSTRAINTS_NUM = resultStrings[c++].split(":")[1];
        rec._MODELLING_RUNTIME         = resultStrings[c++].split(":")[1];
        rec._SOLVING_AGENT_NAME        = resultStrings[c++].split(":")[1];
        rec._SOLVING_PREDICATES_NUM    = resultStrings[c++].split(":")[1];
        rec._SOLVING_ACTIONS_NUM       = resultStrings[c++].split(":")[1];
        rec._SOLVING_VARIABLES_NUM     = resultStrings[c++].split(":")[1];
        rec._SOLVING_CONSTRAINTS_NUM   = resultStrings[c++].split(":")[1];
        rec._SOLVING_DIAGNOSES_NUM     = resultStrings[c++].split(":")[1];
        rec._SOLVING_RUNTIME           = resultStrings[c++].split(":")[1];
        rec._COMBINING_RUNTIME         = resultStrings[c++].split(":")[1];
        rec._TOTAL_RUNTIME             = resultStrings[c++].split(":")[1];
        rec._TIMEDOUT                  = resultStrings[c++].split(":")[1];
        rec._LOCAL_INTERNAL_ACTIONS_NUMBERS   = resultStrings[c++].split(":")[1];
        rec._LOCAL_INTERNAL_ACTIONS_MIN       =resultStrings[c++].split(":")[1];
        rec._LOCAL_INTERNAL_ACTIONS_MAX       =resultStrings[c++].split(":")[1];
        rec._LOCAL_EXTERNAL_ACTIONS_NUMBERS   = resultStrings[c++].split(":")[1];
        rec._LOCAL_EXTERNAL_ACTIONS_MIN       =resultStrings[c++].split(":")[1];
        rec._LOCAL_EXTERNAL_ACTIONS_MAX       =resultStrings[c++].split(":")[1];
        rec._LOCAL_TOTAL_ACTIONS_NUMBERS   = resultStrings[c++].split(":")[1];
        rec._LOCAL_TOTAL_ACTIONS_MIN       =resultStrings[c++].split(":")[1];
        rec._LOCAL_TOTAL_ACTIONS_MAX       =resultStrings[c++].split(":")[1];
        rec._LOCAL_DIAGNOSES_NUMBERS   = resultStrings[c++].split(":")[1];
        rec._LOCAL_DIAGNOSES_MIN       = resultStrings[c++].split(":")[1];
        rec._LOCAL_DIAGNOSES_MAX       = resultStrings[c++].split(":")[1];
        rec._SIZE_MAX_SUBGROUP         = resultStrings[c++].split(":")[1];
        rec._PERCENT_MAX_SUBGROUP      = resultStrings[c++].split(":")[1];
        rec._DIAGNOSES_NUM             = resultStrings[c].split(":")[1];
        rec._SUCCESSFUL                = "yes";
        rec._COMPARABLE                = comparable;
        return rec;
    }

    private static Record2 createFailedRecord(File domainFolder, File problemFolder, int f, int r, String o, String reasonerName) {
        Record2 rec = new Record2();

        // output members
        // controlled parameters
        rec._BENCHMARK_NAME            = "mastrips";
        rec._DOMAIN_NAME               = domainFolder.getName();
        rec._PROBLEM_NAME              = problemFolder.getName();
        rec._FAULTS_NUM                = String.valueOf(f);
        rec._REPETITION_NUM            = String.valueOf(r);
        rec._OBSERVABILITY             = o;
        rec._REASONER_NAME             = reasonerName;

        // instance non-controlled parameters
        rec._AGENTS_NUM                = String.valueOf(Parser.parseAgentNames(new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-agents.txt")).size());
        rec._GOAL_SIZE                 = String.valueOf(Parser.parseGoal(new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + ".pddl")).size());
        rec._GOAL_AGENT_RATIO          = String.valueOf(Integer.parseInt(rec._GOAL_SIZE) * 1.0 / Integer.parseInt(rec._AGENTS_NUM));
        rec._PLAN_LENGTH               = String.valueOf(Parser.parseCombinedPlan(new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-combined_plan.solution")).size());
        rec._COEFFICIENT_OF_VARIATION  = String.valueOf(Parser.calculateCoefficientOfVariation(Integer.parseInt(rec._AGENTS_NUM), Parser.parseCombinedPlan(new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-combined_plan.solution"))));

        // measurement members
        rec._MODELLING_AGENT_NAME      = "-";
        rec._MODELLING_PREDICATES_NUM  = "-";
        rec._MODELLING_ACTIONS_NUM     = "-";
        rec._MODELLING_VARIABLES_NUM   = "-";
        rec._MODELLING_CONSTRAINTS_NUM = "-";
        rec._MODELLING_RUNTIME         = "-";
        rec._SOLVING_AGENT_NAME        = "-";
        rec._SOLVING_PREDICATES_NUM    = "-";
        rec._SOLVING_ACTIONS_NUM       = "-";
        rec._SOLVING_VARIABLES_NUM     = "-";
        rec._SOLVING_CONSTRAINTS_NUM   = "-";
        rec._SOLVING_DIAGNOSES_NUM     = "-";
        rec._SOLVING_RUNTIME           = "-";
        rec._COMBINING_RUNTIME         = "-";
        rec._TOTAL_RUNTIME             = "-";
        rec._TIMEDOUT                  = "-";
        rec._LOCAL_INTERNAL_ACTIONS_NUMBERS   = "-";
        rec._LOCAL_INTERNAL_ACTIONS_MIN       = "-";
        rec._LOCAL_INTERNAL_ACTIONS_MAX       = "-";
        rec._LOCAL_EXTERNAL_ACTIONS_NUMBERS   = "-";
        rec._LOCAL_EXTERNAL_ACTIONS_MIN       = "-";
        rec._LOCAL_EXTERNAL_ACTIONS_MAX       = "-";
        rec._LOCAL_TOTAL_ACTIONS_NUMBERS   = "-";
        rec._LOCAL_TOTAL_ACTIONS_MIN       = "-";
        rec._LOCAL_TOTAL_ACTIONS_MAX       = "-";
        rec._LOCAL_DIAGNOSES_NUMBERS   = "-";
        rec._LOCAL_DIAGNOSES_MIN       = "-";
        rec._LOCAL_DIAGNOSES_MAX       = "-";
        rec._SIZE_MAX_SUBGROUP         = "-";
        rec._PERCENT_MAX_SUBGROUP      = "-";
        rec._DIAGNOSES_NUM             = "-";
        rec._SUCCESSFUL                = "no";
        rec._COMPARABLE                = "no";
        return rec;
    }

    private static void saveRecordsToExcel(List<Record2> records, File outputFile) {
        try {
            // open a workbook
            FileOutputStream file = new FileOutputStream(outputFile);
            Workbook wb = new HSSFWorkbook();
            // adding a sheet
            Sheet resultsSheet = wb.createSheet("results");

            // writing the column names
            Row headersRow = resultsSheet.createRow(0);
            for (int i = 0; i < Record2.headers.length; i++) {
                headersRow.createCell(i).setCellValue(Record2.headers[i]);
            }

            // writing the data
            for (int i = 0; i < records.size(); i++) {
                Row row = resultsSheet.createRow(i+1);
                records.get(i).recordToRow(row);
            }

            // closing workbook
            wb.close();
            // save the workbook
            wb.write(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        print(9);
    }
}

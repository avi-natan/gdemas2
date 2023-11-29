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
                            if (resultSimple.exists() && resultSmart.exists()) {
                                records.add(createSuccessfulRecord(resultSimple, "yes"));
                                records.add(createSuccessfulRecord(resultSmart, "yes"));
                                print("both");
                                countYes += 2;
                            } else if (resultSimple.exists() && !resultSmart.exists()) {
                                records.add(createSuccessfulRecord(resultSimple, "no"));
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "smart"));
                                print("simple");
                                countYes += 1;
                                countNo += 1;
                            } else if (!resultSimple.exists() && resultSmart.exists()) {
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "simple"));
                                records.add(createSuccessfulRecord(resultSmart, "no"));
                                print("smart");
                                countNo += 1;
                                countYes += 1;
                            } else {
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "simple"));
                                records.add(createFailedRecord(domainFolder, problemFolder, f, r, o, "smart"));
                                print("none");
                                countNo += 2;
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

    private static Record2 createSuccessfulRecord(File resultsFile, String comparable) {
        Record2 rec = new Record2();

        String[] resultStrings = Parser.readFromFile(resultsFile).split("\r\n");

        // output members
        // controlled parameters
        rec._BENCHMARK_NAME            = resultStrings[0].split(":")[1];
        rec._DOMAIN_NAME               = resultStrings[1].split(":")[1];
        rec._PROBLEM_NAME              = resultStrings[2].split(":")[1];
        rec._FAULTS_NUM                = resultStrings[3].split(":")[1];
        rec._REPETITION_NUM            = resultStrings[4].split(":")[1];
        rec._OBSERVABILITY             = resultStrings[5].split(":")[1];
        rec._REASONER_NAME             = resultStrings[6].split(":")[1];

        // instance non-controlled parameters
        rec._AGENTS_NUM                = resultStrings[7].split(":")[1];
        rec._GOAL_SIZE                 = resultStrings[8].split(":")[1];
        rec._GOAL_AGENT_RATIO          = resultStrings[9].split(":")[1];
        rec._PLAN_LENGTH               = resultStrings[10].split(":")[1];
        rec._COEFFICIENT_OF_VARIATION  = resultStrings[11].split(":")[1];

        // measurement members
        rec._MODELLING_AGENT_NAME      = resultStrings[12].split(":")[1];
        rec._MODELLING_PREDICATES_NUM  = resultStrings[13].split(":")[1];
        rec._MODELLING_ACTIONS_NUM     = resultStrings[14].split(":")[1];
        rec._MODELLING_VARIABLES_NUM   = resultStrings[15].split(":")[1];
        rec._MODELLING_CONSTRAINTS_NUM = resultStrings[16].split(":")[1];
        rec._MODELLING_RUNTIME         = resultStrings[17].split(":")[1];
        rec._SOLVING_AGENT_NAME        = resultStrings[18].split(":")[1];
        rec._SOLVING_PREDICATES_NUM    = resultStrings[19].split(":")[1];
        rec._SOLVING_ACTIONS_NUM       = resultStrings[20].split(":")[1];
        rec._SOLVING_VARIABLES_NUM     = resultStrings[21].split(":")[1];
        rec._SOLVING_CONSTRAINTS_NUM   = resultStrings[22].split(":")[1];
        rec._SOLVING_RUNTIME           = resultStrings[23].split(":")[1];
        rec._COMBINING_RUNTIME         = resultStrings[24].split(":")[1];
        rec._SOLV_AND_COMB_RUNTIME     = resultStrings[25].split(":")[1];
        rec._DIAGNOSES_NUM             = resultStrings[26].split(":")[1];
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
        rec._SOLVING_RUNTIME           = "-";
        rec._COMBINING_RUNTIME         = "-";
        rec._SOLV_AND_COMB_RUNTIME     = "-";
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

    private static void saveRecords(List<Record> records, File resultsFile) {
        try {
            // open a workbook
            FileOutputStream file = new FileOutputStream(resultsFile);
            Workbook wb = new HSSFWorkbook();
            // adding a sheet
            Sheet resultsSheet = wb.createSheet("results");

            // writing the column names
            Row headersRow = resultsSheet.createRow(0);
            for (int i = 0; i < Record.headers.length; i++) {
                headersRow.createCell(i).setCellValue(Record.headers[i]);
            }

            // writing the data
            for (int j = 0; j < records.size(); j++) {
                Row row = resultsSheet.createRow(j+1);
                records.get(j).recordToRow(row);
            }

            // closing workbook
            wb.close();
            // save the workbook
            wb.write(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

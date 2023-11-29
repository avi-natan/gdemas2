package gdemas;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static gdemas.Utils.print;
import static gdemas.Utils.listDirectories;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!!!!");

        // Pipeline 04 - faulty execution
        // execution modes: "new", "continue"
//        String p04executionMode = "new";
        int[] faultNumbers = new int[] {1,2,3,4,5};
        int repeatNumber = 10;
//        P04FaultyExecutioner.execute(p04executionMode, faultNumbers, repeatNumber);

        // Pipeline 05 - diagnosis
        // execution modes: "new", "continue", "continueSkipFailed"
//        String p05executionMode = "new";
        String[] observabilities = {
//                "1p",
                "25p",
                "50p",
                "75p",
                "99p"
        };
//        P05DiagnosisRunner.execute(p05executionMode, observabilities);

        // pipeline 06 - results collection
        P06ResultsCollector.execute(faultNumbers, repeatNumber, observabilities);

//        manualExecutionWhileWritingAlg();
//        PlanGenerator p = new PlanGenerator();
//        p.generatePlan();
    }

    private static void manualExecutionWhileWritingAlg() {
        String benchmarkName = "mastrips";
        String domainName = "logistics00";
        String problemName = "probLOGISTICS-4-0";
        File domainFile = new File("benchmarks - sandbox/mastrips/logistics00/logistics00-domain.pddl");
        File problemFile = new File("benchmarks - sandbox/mastrips/logistics00/probLOGISTICS-4-0/logistics00-probLOGISTICS-4-0.pddl");
        File agentsFile = new File("benchmarks - sandbox/mastrips/logistics00/probLOGISTICS-4-0/logistics00-probLOGISTICS-4-0-agents.txt");
        File combinedPlanFile = new File("benchmarks - sandbox/mastrips/logistics00/probLOGISTICS-4-0/logistics00-probLOGISTICS-4-0-combined_plan.solution");

        File faultsFile = new File("benchmarks - sandbox/mastrips/logistics00/probLOGISTICS-4-0/2/logistics00-probLOGISTICS-4-0-f[2]-r[1]-faults.txt");
        File trajectoryFile = new File("benchmarks - sandbox/mastrips/logistics00/probLOGISTICS-4-0/2/logistics00-probLOGISTICS-4-0-f[2]-r[1]-combined_trajectory.trajectory");
        File resultsFile = new File("benchmarks - sandbox/mastrips/logistics00/probLOGISTICS-4-0/2/logistics00-probLOGISTICS-4-0-f[2]-r[1]-results.xls");
        String[] observabilities = {
                "1p",
                "25p",
                "50p",
                "75p",
                "99p"
        };
        String[] reasoners = {
                "simple",
                "smart"
        };
        List<Record> records = new ArrayList<>();
        for (String observability : observabilities) {
            for (String s: reasoners) {
                switch (s) {
                    case "simple":
                        Reasoner simple = new ReasonerSimple(
                                benchmarkName,
                                domainName,
                                problemName,
                                domainFile,
                                problemFile,
                                agentsFile,
                                combinedPlanFile,
                                faultsFile,
                                trajectoryFile,
                                observability
                        );
                        simple.diagnoseProblem();
                        records.add(new Record(simple));
                        print(9);
                        break;
                    case "smart":
                        Reasoner smart = new ReasonerSmart(
                                benchmarkName,
                                domainName,
                                problemName,
                                domainFile,
                                problemFile,
                                agentsFile,
                                combinedPlanFile,
                                faultsFile,
                                trajectoryFile,
                                observability
                        );
                        smart.diagnoseProblem();
                        records.add(new Record(smart));
                        print(9);
                        break;
                }
            }
        }
        saveRecords(records, resultsFile);
        print(34);
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
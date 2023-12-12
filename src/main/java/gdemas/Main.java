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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static gdemas.Utils.print;
import static gdemas.Utils.listDirectories;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!!!!");

        // Pipeline 02 - plan generator
        // execution modes: "new", "continue", "continueSkipFailed"
//        String p02executionMode = "continue";
//        P02PlanGenerator.execute(p02executionMode);
//        P02PlanValidator.execute();

        // Pipeline 03 - plan combiner
        // execution modes: "new", "continue", "continueSkipFailed"
//        String p03executionMode = "new";
//        P03PlanCombiner.execute(p03executionMode);
//        P03PlanValidator.execute();

        // Pipeline 04 - faulty execution
        // execution modes: "new", "continue", "continueSpecific"
//        String p04executionMode = "continue";
        int[] faultNumbers = new int[] {1,2,3,4,5};
        int repeatNumber = 10;
//        P04FaultyExecutioner.execute(p04executionMode, faultNumbers, repeatNumber);

        // Pipeline 05 - diagnosis
        // execution modes: "new", "continue", "continueSkipFailed"
        String p05executionMode = "continueSkipFailed";
        String[] observabilities = {
//                "1p",
//                "5p",
//                "10p",
//                "12p",
//                "15p",
//                "17p",
//                "20p",
//                "25p",
//                "50p",
//                "75p",
                "99p"
        };
//        P05DiagnosisRunner.execute(p05executionMode, observabilities);

        // pipeline 06 - results collection
//        P06ResultsCollector.execute(faultNumbers, repeatNumber, observabilities);

        manualExecutionWhileWritingAlg();
//        PlanGenerator p = new PlanGenerator();
//        p.generatePlan();
    }

    private static void manualExecutionWhileWritingAlg() {
        // parameters for easier changing
        String benchmarkName = "mastrips";
        String domainName = "logistics00";
        String problemName = "probLOGISTICS-4-0";
        int faultsNum = 2;
        int repetitionNum = 1;
        String observability = "99p";

        // input files based on the parameters
        File domainFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + domainName + "-domain.pddl");
        File problemFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + ".pddl");
        File agentsFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-agents.txt");
        File combinedPlanFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-combined_plan.solution");
        File faultsFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-faults.txt");
        File trajectoryFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-combined_trajectory.trajectory");

        File resultsFileSimple = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-simple-results.txt");
        File resultsFileSmart = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-smart-results.txt");
        File resultsFileAmazing = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-amazing-results.txt");

        List<Record> records = new ArrayList<>();

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
        Record record = new Record(simple);
        record.recordToTxtFile(resultsFileSimple);

        print(9);
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
        record = new Record(smart);
        record.recordToTxtFile(resultsFileSmart);

        print(9);
        Reasoner amazing = new ReasonerAmazing(
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
        amazing.diagnoseProblem();
        record = new Record(amazing);
        record.recordToTxtFile(resultsFileAmazing);

        print(34);
    }
}
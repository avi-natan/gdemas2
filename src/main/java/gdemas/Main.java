package gdemas;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static gdemas.Utils.print;

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
        String p05executionMode = "new";
        String[] observabilities = {
                "1p",
                "5p",
                "10p",
                "12p",
                "15p",
                "17p",
                "20p",
                "25p",
                "50p",
                "75p",
                "99p"
        };
//        P05DiagnosisRunner.execute(p05executionMode, observabilities);

        // pipeline 06 - results collection
//        P06ResultsCollector.execute(faultNumbers, repeatNumber, observabilities);

        manualExecutionWhileWritingAlg();
//        PlanGenerator p = new PlanGenerator();
//        p.generatePlan();

//        chocoLibraryTest();
    }

    private static void chocoLibraryTest() {
        Model model = new Model();
        BoolVar h = model.boolVar("x1");
        BoolVar f = model.boolVar("x2");
        BoolVar c = model.boolVar("x3");
        BoolVar[] vars = {h,f,c};
        model.sum(vars, "=", 1).post();

        Solver solver = model.getSolver();
        Solution s = solver.findSolution();
        while (s != null) {
            print("h: " + s.getIntVal(h));
            print("h: " + s.getIntVal(f));
            print("c: " + s.getIntVal(c));
            print("");
            s = solver.findSolution();
        }
    }

    private static void manualExecutionWhileWritingAlg() {
        // parameters for easier changing
        String benchmarkName = "mastrips";
        String domainName = "logistics00";
        String problemName = "probLOGISTICS-14-0";
        int faultsNum = 2;
        int repetitionNum = 1;
        String observability = "1p";

        // input files based on the parameters
        File domainFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + domainName + "-domain.pddl");
        File problemFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + ".pddl");
        File agentsFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-agents.txt");
        File combinedPlanFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-combined_plan.solution");
        File faultsFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-faults.txt");
        File trajectoryFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-combined_trajectory.trajectory");

        File resultsFileSimple = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-simple-results.txt");
        File resultsFileSmart = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-smart-results.txt");
//        File resultsFileAmazing = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-amazing-results.txt");
//        File resultsFileAmazing2 = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-amazing2-results.txt");
//        File resultsFileAmazing3 = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-amazing3-results.txt");
//        File resultsFileAmazing4 = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-amazing4-results.txt");
        File resultsFileAmazing5 = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-amazing5-results.txt");

        List<Record> records = new ArrayList<>();
        Record record;

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
        record = new Record(simple);
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

//        print(9);
//        Reasoner amazing = new ReasonerAmazing(
//                benchmarkName,
//                domainName,
//                problemName,
//                domainFile,
//                problemFile,
//                agentsFile,
//                combinedPlanFile,
//                faultsFile,
//                trajectoryFile,
//                observability
//        );
//        amazing.diagnoseProblem();
//        record = new Record(amazing);
//        record.recordToTxtFile(resultsFileAmazing);
//
//        print(9);
//        Reasoner amazing2 = new ReasonerAmazing2(
//                benchmarkName,
//                domainName,
//                problemName,
//                domainFile,
//                problemFile,
//                agentsFile,
//                combinedPlanFile,
//                faultsFile,
//                trajectoryFile,
//                observability
//        );
//        amazing2.diagnoseProblem();
//        record = new Record(amazing2);
//        record.recordToTxtFile(resultsFileAmazing2);
//
//        print(9);
//        Reasoner amazing3 = new ReasonerAmazing3(
//                benchmarkName,
//                domainName,
//                problemName,
//                domainFile,
//                problemFile,
//                agentsFile,
//                combinedPlanFile,
//                faultsFile,
//                trajectoryFile,
//                observability
//        );
//        amazing3.diagnoseProblem();
//        record = new Record(amazing3);
//        record.recordToTxtFile(resultsFileAmazing3);
//
//        print(9);
//        Reasoner amazing4 = new ReasonerAmazing4(
//                benchmarkName,
//                domainName,
//                problemName,
//                domainFile,
//                problemFile,
//                agentsFile,
//                combinedPlanFile,
//                faultsFile,
//                trajectoryFile,
//                observability
//        );
//        amazing4.diagnoseProblem();
//        record = new Record(amazing4);
//        record.recordToTxtFile(resultsFileAmazing4);

        print(9);
        Reasoner amazing5 = new ReasonerAmazing5(
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
        amazing5.diagnoseProblem();
        record = new Record(amazing5);
        record.recordToTxtFile(resultsFileAmazing5);

        print(777);
    }
}
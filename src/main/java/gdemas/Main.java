package gdemas;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
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
//                "5p",
                "10p",
//                "12p",
//                "15p",
//                "17p",
                "20p",
//                "25p",
//                "50p",
//                "75p",
                "99p"
        };
        long timeout = 10000;

//        Instant start = Instant.now();
//        P05DiagnosisRunner.execute(p05executionMode, observabilities, timeout);
//        Instant end = Instant.now();
//        long p5duration = Duration.between(start, end).toMinutes();
//
//        // pipeline 06 - results collection
//        start = Instant.now();
//        P06ResultsCollector.execute(faultNumbers, repeatNumber, observabilities);
//        end = Instant.now();
//        long p6duration = Duration.between(start, end).toMinutes();
//
//        print(java.time.LocalTime.now() + ": p5 duration: " + p5duration);
//        print(java.time.LocalTime.now() + ": p6 duration: " + p6duration);


        manualExecutionWhileWritingAlg();

//        chocoLibraryTest();
    }

    private static void manualExecutionWhileWritingAlg() {
        // parameters for easier changing
        String benchmarkName = "mastrips";
        String domainName = "logistics00";
        String problemName = "probLOGISTICS-4-0";
        int faultsNum = 2;
        int repetitionNum = 1;
        String observability = "1p";
        long timeout = 4000;

        // input files based on the parameters
        File domainFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + domainName + "-domain.pddl");
        File problemFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + ".pddl");
        File agentsFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-agents.txt");
        File combinedPlanFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-combined_plan.solution");
        File graphFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-graph.gml");
        File faultsFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-faults.txt");
        File trajectoryFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-combined_trajectory.trajectory");

        File resultsFileSimple = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-simple-results.txt");
        File resultsFileSmart = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-smart-results.txt");
        File resultsFileAmazing5 = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-amazing5-results.txt");
        File resultsFileWow = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-wow-results.txt");
        File resultsFileSuperb = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-" + observability + "-superb-results.txt");

        Record record;

        print("initialization simple just to hit the model on the head so it focuses");
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
                observability,
                timeout
        );
        simple.diagnoseProblem();
        outputNodesData(simple._NODES_AGENTS, simple._NODES_ACTIONS, simple._NODES_PREDICATES, graphFile);
        record = new Record(simple);
        record.recordToTxtFile(resultsFileSimple);

        simple = new ReasonerSimple(
                benchmarkName,
                domainName,
                problemName,
                domainFile,
                problemFile,
                agentsFile,
                combinedPlanFile,
                faultsFile,
                trajectoryFile,
                observability,
                timeout
        );
        simple.diagnoseProblem();
        outputNodesData(simple._NODES_AGENTS, simple._NODES_ACTIONS, simple._NODES_PREDICATES, graphFile);
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
                observability,
                timeout
        );
        smart.diagnoseProblem();
        record = new Record(smart);
        record.recordToTxtFile(resultsFileSmart);

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
                observability,
                timeout
        );
        amazing5.diagnoseProblem();
        record = new Record(amazing5);
        record.recordToTxtFile(resultsFileAmazing5);

        print(9);
        Reasoner wow = new ReasonerWow(
                benchmarkName,
                domainName,
                problemName,
                domainFile,
                problemFile,
                agentsFile,
                combinedPlanFile,
                faultsFile,
                trajectoryFile,
                observability,
                timeout
        );
        wow.diagnoseProblem();
        record = new Record(wow);
        record.recordToTxtFile(resultsFileWow);

        print(9);
        Reasoner superb = new ReasonerSuperb(
                benchmarkName,
                domainName,
                problemName,
                domainFile,
                problemFile,
                agentsFile,
                combinedPlanFile,
                faultsFile,
                trajectoryFile,
                observability,
                timeout
        );
        superb.diagnoseProblem();
        record = new Record(superb);
        record.recordToTxtFile(resultsFileSuperb);

        print(777);
    }

    private static void outputNodesData(List<NodeAgent> nodesAgents, List<NodeAction> nodesActions, List<NodePredicate> nodesPredicates, File graphFile) {
        StringBuilder graph = new StringBuilder();

        // open the graph
        graph.append("Creator \"Avi\"\ngraph\n[\n");

        // put nodes of the agents
        for (NodeAgent n : nodesAgents) {
            graph.append("  node\n  [\n    id ").append(n.id).append("\n    type \"agent\"").append("\n    label \"").append(n.name).append("\"\n  ]\n");
        }

        // put the nodes of the actions
        for (NodeAction n : nodesActions) {
            graph.append("  node\n  [\n    id ").append(n.id).append("\n    type \"action\"").append("\n    label \"").append(n.string).append("\"\n  ]\n");
        }

        // put the nodes of the predicates
        for (NodePredicate n : nodesPredicates) {
            graph.append("  node\n  [\n    id ").append(n.id).append("\n    type \"predicate\"").append("\n    label \"").append(n.string).append("\"\n  ]\n");
        }

        // put the edges between the agents and the actions
        for (NodeAgent nAg : nodesAgents) {
            for (NodeAction nAc : nAg.relevantActions) {
                graph.append("  edge\n  [\n    source ").append(nAg.id).append("\n    target ").append(nAc.id).append("\n    value 1\n  ]\n");
            }
        }

        // put the edges between the actions and the predicates
        for (NodeAction nAc : nodesActions) {
            for (NodePredicate nPr : nAc.relevantPredicates) {
                graph.append("  edge\n  [\n    source ").append(nAc.id).append("\n    target ").append(nPr.id).append("\n    value 1\n  ]\n");
            }
        }

        // close the graph
        graph.append("]\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(graphFile))) {
            writer.write(graph.toString());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }

}
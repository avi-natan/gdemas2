package gdemas;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static gdemas.Utils.print;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!!!!");

        // Pipeline 02 - plan generator
        // execution modes: "new", "continue", "continueSkipFailed"
//        String p02executionMode = "new";
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
//        Instant start = Instant.now();
//        P04FaultyExecutioner.execute(p04executionMode, faultNumbers, repeatNumber);
//        Instant end = Instant.now();
//        long duration = Duration.between(start, end).toMinutes();
//        print(java.time.LocalTime.now() + ": p4 duration: " + duration);

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

        // pipeline 06 - results collection
        Instant start2 = Instant.now();
        P06ResultsCollector.execute(faultNumbers, repeatNumber, observabilities);
        Instant end2 = Instant.now();
        long p6duration = Duration.between(start2, end2).toMinutes();

//        print(java.time.LocalTime.now() + ": p5 duration: " + p5duration);
        print(java.time.LocalTime.now() + ": p6 duration: " + p6duration);


//        manualExecutionWhileWritingAlg();

//        chocoLibraryTest();
    }

    private static void manualExecutionWhileWritingAlg() {
        // parameters for easier changing
        String benchmarkName = "mastrips";
        String domainName = "rovers";
        String problemName = "p10";
        int faultsNum = 1;
        int repetitionNum = 8;
        String observability = "99p";
        long timeout = 10000;

        // input files based on the parameters
        File domainFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + domainName + "-domain.pddl");
        File problemFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + ".pddl");
        File agentsFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-agents.txt");
        File combinedPlanFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-combined_plan.solution");
        File graphFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + domainName + "-" + problemName + "-graph.gml");
        File faultsFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-faults.txt");
        File trajectoryFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-combined_trajectory.trajectory");
        File faultImageFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-faultImage.txt");

//        generateActionStateImage(domainFile, problemFile, agentsFile, combinedPlanFile, faultsFile, faultImageFile);

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
                timeout,
                null
        );
        superb.diagnoseProblem();
        record = new Record(superb);
        record.recordToTxtFile(resultsFileSuperb);

        print(777);
    }

    private static void generateActionStateImage(File domainFile, File problemFile, File agentsFile, File combinedPlanFile, File faultsFile, File faultImageFile) {
        List<List<String>> faultImage = new ArrayList<>();

        List<String> faultStrings = Arrays.asList(Parser.readFromFile(faultsFile).split("\r\n"));

        Domain domain = Parser.parseDomain(domainFile);
        Problem problem = Parser.parseProblem(problemFile);
        List<String> agentNames = Parser.parseAgentNames(agentsFile);
        List<List<String>> combinedPlanActions = Parser.parseCombinedPlan(combinedPlanFile);
        List<List<Map<String, String>>> combinedPlanConditions = Parser.computePlanConditions(combinedPlanActions, agentNames.size(), domain);

        // put the initial predicates into the trajectory
        List<List<String>> trajectory = new ArrayList<>();
        List<String> initState = new ArrayList<>(problem.init);
        trajectory.add(initState);

        print(9);

        // go over the time-steps and the actions. each time, check if an action can be performed
        // if yes, and if it is in the fault strings, register faulty action
        // if yes, and if it is not in the fault strings, register healthy action
        // if no, and it is in the fault strings, raise exception
        // if no, and it is not in the fault strings, register a conflicted action

        // go over the time steps
        for (int t = 0; t < combinedPlanActions.size(); t++) {
            // create a list representing the fault images of this step
            List<String> stepFaultImage = new ArrayList<>();
            // create the new state as a copy of the old state
            List<String> newState = new ArrayList<>(trajectory.get(t));
            // go over the agent actions
            for (int a = 0; a < agentNames.size(); a++) {
                // if it's not a nop action
                print("\n=================> " + t + "," + a + " " + combinedPlanActions.get(t).get(a) + ":");
                if (!combinedPlanActions.get(t).get(a).equals("(nop)")) {
                    List<String> shouldAdd = extractShouldAddPreds(combinedPlanConditions.get(t).get(a).get("eff"));
                    List<String> shouldDel = extractShouldDelPreds(combinedPlanConditions.get(t).get(a).get("eff"));
                    print("apply attempt");
                    print("pre       : " + combinedPlanConditions.get(t).get(a).get("pre"));
                    print("should add: " + String.join(" ", shouldAdd));
                    print("should del: " + String.join(" ", shouldDel));
                    // if the action can be performed (the preconditions are valid)
                    if (validPreconditions(combinedPlanConditions.get(t).get(a).get("pre"), newState)) {
                        // if the action is in the faulty actions database
                        if (faultStrings.contains("t:" + t + ",a:" + a)) {
                            stepFaultImage.add("f");
                            print("faulted");
                            print("added: none");
                            print("deled: none");
                        } else {
                            applyEffects(combinedPlanConditions.get(t).get(a).get("eff"), newState);
                            stepFaultImage.add("h");
                            print("healthy");
                            print("added: " + String.join(" ", shouldAdd));
                            print("deled: " + String.join(" ", shouldDel));
                        }
                        print(9);
                    } else {
                        List<String> invalidPre = calculateInvalidPre(combinedPlanConditions.get(t).get(a).get("pre"), newState);
                        stepFaultImage.add("c");
                        print("conflicted");
                        print("invalid pre: " + String.join(" ", invalidPre));
                        print("added: none");
                        print("deled: none");
                        print(9);
                    }
                } else {
                    stepFaultImage.add("x");
                    print("skipped");
                    print(9);
                }
            }
            faultImage.add(stepFaultImage);
            trajectory.add(newState);
        }

        StringBuilder faultImageString = new StringBuilder("Fault Image:");
        for (int t = 0; t < faultImage.size(); t++) {
            faultImageString.append("\n").append(t).append(":\t");
            for (String s : faultImage.get(t)) {
                faultImageString.append(s).append(" ");
            }
            faultImageString.delete(faultImageString.length()-1, faultImageString.length());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(faultImageFile))) {
            writer.write(faultImageString.toString());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }

        print(9);
    }

    private static boolean validPreconditions(String preconditions, List<String> state) {
        String[] arr = preconditions.split("(?=\\() |(?<=\\)) ");
        for (String p : arr) {
            if (!state.contains(p)) {
                return false;
            }
        }
        return true;
    }

    private static List<String> calculateInvalidPre(String preconditions, List<String> state) {
        List<String> invalidPre = new ArrayList<>();
        String[] arr = preconditions.split("(?=\\() |(?<=\\)) ");
        for (String p : arr) {
            if (!state.contains(p)) {
                invalidPre.add(p);
            }
        }
        return invalidPre;
    }

    private static void applyEffects(String effects, List<String> state) {
        String[] arr = effects.split("(?=\\() |(?<=\\)) ");
        for (String e: arr) {
            if (e.contains("(not (")) {
                String pred = e.substring(5, e.length()-1);
                state.remove(pred);
            } else {
                state.add(e);
            }
        }
    }

    private static List<String> extractShouldAddPreds(String effects) {
        List<String> shouldAdd = new ArrayList<>();
        String[] arr = effects.split("(?=\\() |(?<=\\)) ");
        for (String e: arr) {
            if (!e.contains("(not (")) {
                shouldAdd.add(e);
            }
        }
        return shouldAdd;
    }

    private static List<String> extractShouldDelPreds(String effects) {
        List<String> shouldDel = new ArrayList<>();
        String[] arr = effects.split("(?=\\() |(?<=\\)) ");
        for (String e: arr) {
            if (e.contains("(not (")) {
                shouldDel.add(e.substring(5, e.length()-1));
            }
        }
        return shouldDel;
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
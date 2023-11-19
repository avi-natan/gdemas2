package gdemas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static gdemas.Utils.*;

public class P04FaultyExecutioner {

    public static void execute(String executionMode, int[] faultNumbers, int instancesNumber) {
        File inputFolder = new File("benchmarks/mastrips/04 - combined plans");
        File outputFolder = new File("benchmarks/mastrips/05 - faulty executions");

        Report report;

        if (executionMode.equals("new")) {
            deleteFolderContents(outputFolder);
            report = new Report();
            report.executionType = "new";
        } else {
            report = new Report();
            report.executionType = "continue";
            report.parseFromLastReport(new File("benchmarks/mastrips/05 - faulty executions/report.txt"));
        }
        print(9);

        File[] domainFolders = listDirectories(inputFolder);
        for (File domainFolder: domainFolders) {
            File domainFolder05 = new File(outputFolder, domainFolder.getName());
            mkdirIfDoesntExist(domainFolder05);
            File domainFile = new File(domainFolder, domainFolder.getName() + "-domain.pddl");
            File domainFile05 = new File(domainFolder05, domainFolder05.getName() + "-domain.pddl");
            copyFileIfDoesntExist(domainFile, domainFile05);
            print(domainFile05.getAbsolutePath());
            File[] problemFolders = listDirectories(domainFolder);
            for (File problemFolder: problemFolders) {
                File problemFolder05 = new File(domainFolder05, problemFolder.getName());
                mkdirIfDoesntExist(problemFolder05);
                File problemFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + ".pddl");
                File problemFile05 = new File(problemFolder05, domainFolder05.getName() + "-" + problemFolder05.getName() + ".pddl");
                copyFileIfDoesntExist(problemFile, problemFile05);
                print(problemFile05.getAbsolutePath());
                File agentsFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-agents.txt");
                File agentsFile05 = new File(problemFolder05, domainFolder05.getName() + "-" + problemFolder05.getName() + "-agents.txt");
                copyFileIfDoesntExist(agentsFile, agentsFile05);
                print(agentsFile05.getAbsolutePath());
                File combinedPlanFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-combined_plan.solution");
                File combinedPlanFile05 = new File(problemFolder05, domainFolder05.getName() + "-" + problemFolder05.getName() + "-combined_plan.solution");
                copyFileIfDoesntExist(combinedPlanFile, combinedPlanFile05);
                print(combinedPlanFile05.getAbsolutePath());
                File planFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-plan.txt");
                File planFile05 = new File(problemFolder05, domainFolder05.getName() + "-" + problemFolder05.getName() + "-plan.txt");
                copyFileIfDoesntExist(planFile, planFile05);
                print(planFile05.getAbsolutePath());

                Domain domain = Parser.parseDomain(domainFile05);
                Problem problem = Parser.parseProblem(problemFile05);
                List<String> agentNames = Parser.parseAgentNames(agentsFile05);
                List<List<String>> combinedPlanActions = Parser.parseCombinedPlan(combinedPlanFile05);
                List<List<Map<String, String>>> combinedPlanConditions = Parser.computePlanConditions(combinedPlanActions, agentNames.size(), domain);
                int totalActionsNumber = calculateTotalActionsNumber(combinedPlanActions);
                double probability = 1.0 / totalActionsNumber;
                for (Integer f: faultNumbers) {
                    // create a fault folder if it doesn't exist
                    File faultFolder05 = new File(problemFolder05, "" + f);
                    mkdirIfDoesntExist(faultFolder05);

                    for (int i = 0; i < instancesNumber; i++) {
                        String instanceString = domainFolder05.getName() + "-" + problemFolder05.getName() + "-" + "f[" + f + "]-r[" + i + "]";
                        File faultsFile05 = new File(faultFolder05, instanceString + "-faults.txt");
                        File trajectoryFile05 = new File(faultFolder05, instanceString + "-combined_trajectory.trajectory");

                        // if the faults file exists, continue
                        if (!faultsFile05.exists()) {
                            report.attempts += 1;
                            // attempt up to 10 times to generate and execute with given number of faults
                            int attempt = 0;
                            FaultyExecution fe = null;
                            while (attempt < 10000) {
                                fe = executeInstanceWithFaults(problem, agentNames, combinedPlanActions, combinedPlanConditions, f, probability);
                                if (fe != null) {
                                    break;
                                }
                                attempt += 1;
                            }

                            // upon a successful attempt, save the instance to the faults and trajectory files
                            if (fe != null) {
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(faultsFile05))) {
                                    // Write the content to the file
                                    writer.write(String.join("\r\n", fe.faults));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    // Handle the exception as needed
                                }
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(trajectoryFile05))) {
                                    // Create the trajectory string
                                    StringBuilder str = new StringBuilder("((:init  (");
                                    String init = String.join(") (", fe.trajectory.get(0));
                                    str.append(init).append("))");
                                    for (int t = 0; t < combinedPlanActions.size(); t++){
                                        String jointAction = "(" + String.join(") (", combinedPlanActions.get(t)) + ")";
                                        jointAction = jointAction.replaceAll("\\(nop", "(nop ");
                                        jointAction = "(operators: " + jointAction + ")";
                                        String nextState = "(:state  (" + String.join(") (", fe.trajectory.get(t+1)) + "))";
                                        str.append("\r\n").append(jointAction).append("\r\n").append(nextState);
                                    }
                                    str.append("\r\n)");
                                    writer.write(str.toString());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    // Handle the exception as needed
                                }
                                print("success");
                                report.success += 1;
                                report.successfulFiles.add(faultsFile05.getAbsolutePath());
                            } else {
                                print("fail");
                                report.fail += 1;
                                report.failedFiles.add(faultsFile05.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }

        report.totalExisting = report.totalExistingLast + report.success;
        File reportFile = new File("benchmarks/mastrips/05 - faulty executions/report.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(report.toString());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }

        print(9);
    }

    private static int calculateTotalActionsNumber(List<List<String>> combinedPlanActions) {
        int res = 0;
        for (List<String> combinedPlanAction : combinedPlanActions) {
            for (String s : combinedPlanAction) {
                if (!s.equals("nop")) {
                    res += 1;
                }
            }
        }
        return res;
    }

    private static FaultyExecution executeInstanceWithFaults(Problem problem, List<String> agentNames, List<List<String>> combinedPlanActions, List<List<Map<String, String>>> combinedPlanConditions, Integer f, double p) {
        int remainingFaults = f;
        List<String> faults = new ArrayList<>();
        List<List<String>> trajectory = new ArrayList<>();

        // put the initial predicates into the trajectory
        List<String> initStep = new ArrayList<>();
        for (String str : problem.init) {
            initStep.add(str.substring(1, str.length()-1));
        }
        trajectory.add(initStep);

        // go over the time-steps and the actions. each time, check if an action can be performed
        // if yes, and if there are still faults to be done, decide using p whether the
        // action should fail or no.

        // go over the time steps
        for (int t = 0; t < combinedPlanActions.size(); t++) {
            // create the new state as a copy of the old state
            List<String> newState = new ArrayList<>(trajectory.get(t));
            // go over the agent actions
            for (int a = 0; a < agentNames.size(); a++) {
                // if it's not a nop action
                if (!combinedPlanActions.get(t).get(a).equals("nop")) {
//                    print("\n=================> attempt to apply action " + t + "," + a +" (" + combinedPlanActions.get(t).get(a) + ")");
                    // if the action can be performed (the preconditions are valid)
                    if (validPreconditions(combinedPlanConditions.get(t).get(a).get("pre"), newState)) {
                        // if there are still faults to be carried out
                        if (remainingFaults > 0) {
                            // randomly decide whether to introduce a fault or not
                            Random rand = new Random();
                            double prob = rand.nextDouble();
                            if (prob < p) {
                                // if yes, add a fault to the list and do not do anything.
                                faults.add("t:" + t + ",a:" + a);
//                                print("faulted: " + t + "," + a +" (" + combinedPlanActions.get(t).get(a) + ")");
                                remainingFaults = remainingFaults - 1;
                            } else {
                                // if no, execute the action
                                applyEffects(combinedPlanConditions.get(t).get(a).get("eff"), newState);
//                                print("applied: " + t + "," + a +" (" + combinedPlanActions.get(t).get(a) + ")");
                            }
                        } else {
                            applyEffects(combinedPlanConditions.get(t).get(a).get("eff"), newState);
//                            print("applied: " + t + "," + a +" (" + combinedPlanActions.get(t).get(a) + ")");
                        }
                    } //else {
//                        print("conflicted: " + t + "," + a +" (" + combinedPlanActions.get(t).get(a) + ")");
                    //}
                }
            }
            trajectory.add(newState);
        }

        if (remainingFaults > 0) {
            return null;
        } else {
            return new FaultyExecution(faults, trajectory);
        }

    }

    private static boolean validPreconditions(String preconditions, List<String> state) {
        String[] arr = preconditions.substring(1, preconditions.length()-1).split("\\) \\(");
        for (String p : arr) {
            if (!state.contains(p)) {
                return false;
            }
        }
        return true;
    }

    private static void applyEffects(String effects, List<String> state) {
        String[] arr = effects.substring(1, effects.length()-1).split("\\) \\(");
        for (String e: arr) {
            if (e.contains("not ")) {
                state.remove(e.substring(5, e.length()-1));
            } else {
                state.add(e);
            }
        }
    }


}

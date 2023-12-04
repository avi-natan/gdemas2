package gdemas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static gdemas.Utils.*;

public class P03PlanCombiner {
    public static void execute(String executionMode) {
        File inputFolder = new File("benchmarks/mastrips/03 - plans");
        File outputFolder = new File("benchmarks/mastrips/04 - combined plans");

        Report report;

        if (executionMode.equals("new")) {
            deleteFolderContents(outputFolder);
            report = new Report();
            report.executionType = "new";
        } else {
            report = new Report();
            report.executionType = "continue";
            report.parseFromLastReport(new File("benchmarks/mastrips/04 - combined plans/report.txt"));
        }

        File[] domainFolders = listDirectories(inputFolder);
        for (File domainFolder: domainFolders) {
            File domainFolder04 = new File(outputFolder, domainFolder.getName());
            mkdirIfDoesntExist(domainFolder04);
            File domainFile = new File(domainFolder, domainFolder.getName() + "-domain.pddl");
            File domainFile04 = new File(domainFolder04, domainFolder04.getName() + "-domain.pddl");
            copyFileIfDoesntExist(domainFile, domainFile04);
            print(domainFile04.getAbsolutePath());
            File[] problemFolders = listDirectories(domainFolder);
            for (File problemFolder: problemFolders) {
                // prepare working files
                File problemFolder04 = new File(domainFolder04, problemFolder.getName());
                mkdirIfDoesntExist(problemFolder04);
                File problemFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + ".pddl");
                File problemFile04 = new File(problemFolder04, domainFolder04.getName() + "-" + problemFolder04.getName() + ".pddl");
                copyFileIfDoesntExist(problemFile, problemFile04);
                print(problemFile04.getAbsolutePath());
                File agentsFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-agents.txt");
                File agentsFile04 = new File(problemFolder04, domainFolder04.getName() + "-" + problemFolder04.getName() + "-agents.txt");
                copyFileIfDoesntExist(agentsFile, agentsFile04);
                print(agentsFile04.getAbsolutePath());
                File planFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-plan.txt");
                File planFile04 = new File(problemFolder04, domainFolder04.getName() + "-" + problemFolder04.getName() + "-plan.txt");
                copyFileIfDoesntExist(planFile, planFile04);
                print(planFile04.getAbsolutePath());

                // combine the plan
                Domain domain = Parser.parseDomain(domainFile04);
                List<String> agentNames = Parser.parseAgentNames(agentsFile04);
                List<List<String>> combinedPlan = combinePlan(domain, agentNames, planFile04);

                // prepare the combined plan as a string
                StringBuilder combinedPlanStringBuilder = new StringBuilder();
                for (List<String> action : combinedPlan) {
                    StringBuilder jointAction = new StringBuilder("[");
                    for (String s : action) {
                        jointAction.append(s).append(",");
                    }
                    jointAction = new StringBuilder(jointAction.substring(0, jointAction.length() - 1) + "]\n");
                    combinedPlanStringBuilder.append(jointAction);
                }
                String combinedPlanString = combinedPlanStringBuilder.toString();
                combinedPlanString = combinedPlanString.substring(0, combinedPlanString.length()-1);

                // save the combined plan string to disk
                File combinedPlanFile04 = new File(problemFolder04, domainFolder04.getName() + "-" + problemFolder04.getName() + "-combined_plan.solution");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(combinedPlanFile04))) {
                    writer.write(combinedPlanString);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle the exception as needed
                }

                // execute the plan without faults
                Problem problem = Parser.parseProblem(problemFile04);
                List<List<String>> combinedPlanActions = Parser.parseCombinedPlan(combinedPlanFile04);
                List<List<Map<String, String>>> combinedPlanConditions = Parser.computePlanConditions(combinedPlanActions, agentNames.size(), domain);
                FaultyExecution fe = P04FaultyExecutioner.executeInstanceWithFaults(problem, agentNames, combinedPlanActions, combinedPlanConditions, 0, 0.0);

                // save the non-faulty execution
                File healthyTrajectory = new File(problemFolder04, domainFolder04.getName() + "-" + problemFolder04.getName() + "-healthy_execution.trajectory");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(healthyTrajectory))) {
                    // Create the trajectory string
                    StringBuilder str = new StringBuilder("((:init  (");
                    assert fe != null;
                    String init = String.join(") (", fe.trajectory.get(0));
                    str.append(init).append("))");
                    for (int t = 0; t < combinedPlan.size(); t++){
                        String jointAction = String.join(" ", combinedPlan.get(t));
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
            }
        }

        report.totalExisting = report.totalExistingLast + report.success;
        File reportFile = new File("benchmarks/mastrips/04 - combined plans/report.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(report.toString());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }

    private static List<List<String>> combinePlan(Domain domain, List<String> agentNames, File planFile04) {
        // initialize action lists
        print("initializing action lists");
        List<List<Action>> actions = agentNames.stream().<List<Action>>map(name -> new ArrayList<>()).collect(Collectors.toList());

        // populate actions lists
        print("populating lists");
        String[] plan = Parser.readFromFile(planFile04).split("\r\n");
        for (int s = 0; s < plan.length; s++) {
            String action = plan[s];
            String agentName = action.split(" ")[1];
            String preconditions = extractActionGroundedConditions(action, "preconditions", domain);
            String effects = extractActionGroundedConditions(action, "effects", domain);
            List<String> effectedPredicates = extractEffectedPredicates(effects);
            actions.get(agentNames.indexOf(agentName)).add(new Action(s, action, preconditions, effects, effectedPredicates));
        }

        // build dependencies of the actions
        print("building action dependencies");
        buildDependencies(actions);

        // pop everytime the heads of the lists, and add the actions with no effecting actions. update the actions in the rest of the list
        print("popping actions");
        List<List<String>> jointActions = new ArrayList<>();
        while (!completelyEmpty(actions)) {
            // prepare the resulting joint action string list
            List<String> jointAction = new ArrayList<>();
            // take the top actions and put them in a list to be executed
            List<Action> topActions = new ArrayList<>();
            for (List<Action> actionList : actions) {
                if (actionList.isEmpty()) {
                    topActions.add(null);
                } else {
                    topActions.add(actionList.get(0));
                }
            }
            // filter out the actions that have other actions effecting them
            for (int a = 0; a < topActions.size(); a++) {
                if (topActions.get(a) != null && !topActions.get(a).effectingActions.isEmpty()) {
                    topActions.set(a, null);
                }
            }
            // for the remaining actions, put their strings into the joint action string list,
            // remove its effect on other actions, and finally remove it from the actions list
            for (int a = 0; a < topActions.size(); a++) {
                if (topActions.get(a) == null) {
                    jointAction.add("(nop )");
                } else {
                    Action action = topActions.get(a);
                    jointAction.add(action.actionString);
                    for (Action effected: action.effectedActions) {
                        effected.effectingActions.remove(action);
                    }
                    actions.get(a).remove(action);
                }
            }
            jointActions.add(jointAction);
        }

        // returning the joint actions
        print("returning the joint actions");
        return jointActions;
    }

    private static boolean completelyEmpty(List<List<Action>> actions) {
        for (List<Action> list: actions) {
            if (!list.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void buildDependencies(List<List<Action>> actions) {
        for (int A = 0; A < actions.size(); A++) {
            for (Action action : actions.get(A)) {
                calculateEffectedActions(actions, action, A);
            }
        }
    }

    private static void calculateEffectedActions(List<List<Action>> actions, Action action, int A) {
        for (int a = 0; a < actions.size(); a++) {
            if (a != A) {
                for (Action act: actions.get(a)) {
                    if (action.actionOriginalStep < act.actionOriginalStep) {
                        if (someEffectedArePreconditions(action.effectedPredicates, act.preconditions)) {
                            action.effectedActions.add(act);
                            act.effectingActions.add(action);
                        }
                    }
                }
            }
        }
    }

    private static boolean someEffectedArePreconditions(List<String> effectedPredicates, String preconditions) {
        for (String predicate: effectedPredicates) {
            if (preconditions.contains(predicate)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> extractEffectedPredicates(String effects) {
        String[] splitted = effects.split("(?=\\() |(?<=\\)) ");

        List<String> effectedPredicates = new ArrayList<>();
        for (String s: splitted) {
            if (s.contains("(not (")) {
                effectedPredicates.add(s.substring(5, s.length()-1));
            } else {
                effectedPredicates.add(s);
            }
        }
        return effectedPredicates;
    }

    private static String extractActionGroundedConditions(String groundedAction, String conditionsType, Domain domain) {
        String[] actionSignature = groundedAction.substring(1, groundedAction.length()-1).split(" ");
        String actionName = actionSignature[0];
        String[] actionArguments = Arrays.copyOfRange(actionSignature, 1, actionSignature.length);
        String conditions = domain.actions.get(actionName).get(conditionsType);
        String[] actionParams = domain.actions.get(actionName).get("parameters").replaceAll("\\s+-\\s+\\S+", "").split(" ");
        for (int i = 0; i < actionArguments.length; i++) {
            conditions = conditions.replaceAll("\\?" + actionParams[i].substring(1), actionArguments[i]);
        }
        return conditions;
    }


}

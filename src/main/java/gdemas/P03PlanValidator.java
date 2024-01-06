package gdemas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static gdemas.Utils.listDirectories;
import static gdemas.Utils.print;

public class P03PlanValidator {

    public static void execute() {
        File workingFolder = new File("benchmarks/mastrips/04 - combined plans");

        Report report = new Report();
        report.executionType = "new";
        File reportFile = new File("benchmarks/mastrips/04 - combined plans/validation report.txt");

        File[] domainFolders = listDirectories(workingFolder);
        for (File domainFolder: domainFolders) {
            File domainFile = new File(domainFolder, domainFolder.getName() + "-domain.pddl");
            print(domainFile.getAbsolutePath());
            File[] problemFolders = listDirectories(domainFolder);
            for (File problemFolder: problemFolders) {
                File problemFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + ".pddl");
                print(problemFile.getAbsolutePath());
                File agentsFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-agents.txt");
                print(agentsFile.getAbsolutePath());
                File combinedPlanFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-combined_plan.solution");
                print(combinedPlanFile.getAbsolutePath());
                File healthyTrajectoryFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-healthy_trajectory.trajectory");
                print(healthyTrajectoryFile.getAbsolutePath());

                if (combinedPlanFile.exists()) {
                    report.attempts += 1;
                    print("attempt");

                    boolean valid = validateCombinedPlan(domainFile, problemFile, combinedPlanFile, healthyTrajectoryFile);

                    if (valid) {
                        report.success += 1;
                        report.successfulFiles.add(combinedPlanFile.getAbsolutePath());
                        print("success");
                    } else {
                        report.fail += 1;
                        report.failedFiles.add(combinedPlanFile.getAbsolutePath());
                        print("fail");
                    }
                } else {
                    report.skipped += 1;
                    report.skippedFiles.add(combinedPlanFile.getAbsolutePath());
                    print("skip");
                }
            }
        }

        report.totalExisting = report.totalExistingLast + report.success;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(report.toString());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }

    private static boolean validateCombinedPlan(File domainFile, File problemFile, File combinedPlanFile, File healthyTrajectoryFile) {
        Domain domain = Parser.parseDomain(domainFile);
        Problem problem = Parser.parseProblem(problemFile);
        List<List<String>> combinedPlan = Parser.parseCombinedPlan(combinedPlanFile);

        List<List<Map<String, String>>> conditions = computeConditions(combinedPlan, domain);

        List<List<String>> states = new ArrayList<>();
        List<String> state = new ArrayList<>(problem.init);
        states.add(new ArrayList<>(state));

        for (int t = 0; t < combinedPlan.size(); t++) {
            for (int a = 0; a < combinedPlan.get(t).size(); a++) {
                if (!combinedPlan.get(t).get(a).equals("(nop)")) {
                    if (preconditionsMet(state, conditions.get(t).get(a).get("pre"))) {
                        applyActionEffectsToState(state, conditions.get(t).get(a).get("eff"));
                    } else {
                        print("action " + t + "," + a + ": " + combinedPlan.get(t).get(a) + ", failed");
                        return false;
                    }
                }
            }
            states.add(new ArrayList<>(state));
        }

        boolean valid = goalReached(state, problem.goal);

        if (valid) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(healthyTrajectoryFile))) {
                // Create the trajectory string
                StringBuilder str = new StringBuilder("((:init  ");
                String init = String.join(" ", states.get(0));
                str.append(init).append(")");
                for (int t = 0; t < combinedPlan.size(); t++){
                    String jointAction = String.join(" ", combinedPlan.get(t));
                    jointAction = jointAction.replaceAll("\\(nop", "(nop ");
                    jointAction = "(operators: " + jointAction + ")";
                    String nextState = "(:state  " + String.join(" ", states.get(t+1)) + ")";
                    str.append("\r\n").append(jointAction).append("\r\n").append(nextState);
                }
                str.append("\r\n)");
                writer.write(str.toString());
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception as needed
            }
        }

        return valid;
    }

    private static boolean goalReached(List<String> state, List<String> goal) {
        for (String s : goal) {
            if (!state.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private static void applyActionEffectsToState(List<String> state, String eff) {
        String[] splitted = eff.split("(?=\\() |(?<=\\)) ");
        for (String s : splitted) {
            if (s.contains("(not (")) {
                String predicate = s.substring(5, s.length()-1);
                state.remove(predicate);
            } else {
                if (!state.contains(s)) {
                    state.add(s);
                }
            }
        }
    }

    private static boolean preconditionsMet(List<String> state, String pre) {
        String[] splitted = pre.split("(?=\\() |(?<=\\)) ");
        for (String s : splitted) {
            if (!state.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private static List<List<Map<String, String>>> computeConditions(List<List<String>> combinedPlan, Domain domain) {
        List<List<Map<String, String>>> conditions = new ArrayList<>();
        for (List<String> strings : combinedPlan) {
            List<Map<String, String>> jointConditions = new ArrayList<>();
            for (String p : strings) {
                if (p.equals("(nop)")) {
                    Map<String, String> c = new HashMap<>();
                    c.put("pre", "");
                    c.put("eff", "");
                    jointConditions.add(c);
                } else {
                    String[] signature = p.substring(1, p.length()-1).split(" ");
                    String name = signature[0];
                    String[] arguments = Arrays.copyOfRange(signature, 1, signature.length);
                    String[] parameters = domain.actions.get(name).get("parameters").replaceAll("\\s+-\\s+\\S+", "").split(" ");
                    String preconditions = domain.actions.get(name).get("preconditions");
                    String effects = domain.actions.get(name).get("effects");
                    for (int i = 0; i < arguments.length; i++) {
                        preconditions = preconditions.replaceAll("\\?" + parameters[i].substring(1), arguments[i]);
                        effects = effects.replaceAll("\\?" + parameters[i].substring(1), arguments[i]);
                    }
                    Map<String, String> c = new HashMap<>();
                    c.put("pre", preconditions);
                    c.put("eff", effects);
                    jointConditions.add(c);
                }
            }
            conditions.add(jointConditions);
        }
        return conditions;
    }
}

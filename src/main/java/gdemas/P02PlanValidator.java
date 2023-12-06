package gdemas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static gdemas.Utils.*;
import static gdemas.Utils.listDirectories;

public class P02PlanValidator {

    public static void execute() {
        File workingFolder = new File("benchmarks/mastrips/03 - plans");

        Report report = new Report();
        report.executionType = "new";
        File reportFile = new File("benchmarks/mastrips/03 - plans/validation report.txt");

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
                File planFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-plan.txt");
                print(planFile.getAbsolutePath());

                if (planFile.exists()) {
                    report.attempts += 1;
                    print("attempt");

                    boolean valid = validatePlan(domainFile, problemFile, planFile);

                    if (valid) {
                        report.success += 1;
                        report.successfulFiles.add(planFile.getAbsolutePath());
                        print("success");
                    } else {
                        report.fail += 1;
                        report.failedFiles.add(planFile.getAbsolutePath());
                        print("fail");
                    }
                } else {
                    report.skipped += 1;
                    report.skippedFiles.add(planFile.getAbsolutePath());
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

    private static boolean validatePlan(File domainFile, File problemFile, File planFile) {
        Domain domain = Parser.parseDomain(domainFile);
        Problem problem = Parser.parseProblem(problemFile);
        List<String> plan = Parser.parsePlan(planFile);

        List<Map<String, String>> conditions = computeConditions(plan, domain);
        List<String> state = new ArrayList<>(problem.init);

        for (int t = 0; t < plan.size(); t++) {
            if (preconditionsMet(state, conditions.get(t).get("pre"))) {
                applyActionEffectsToState(state, conditions.get(t).get("eff"));
            } else {
                print("action #" + t + ": " + plan.get(t) + ", failed");
                return false;
            }
        }

        return goalReached(state, problem.goal);
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
                state.add(s);
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

    private static List<Map<String, String>> computeConditions(List<String> plan, Domain domain) {
        List<Map<String, String>> conditions = new ArrayList<>();
        for (String p : plan) {
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
            conditions.add(c);
        }
        return conditions;
    }
}

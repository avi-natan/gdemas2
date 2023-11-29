package gdemas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Parser {

    public static String readFromFile(File file) {
        try {
            return String.join(System.lineSeparator(), Files.readAllLines(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int parseRepetitionNum(File faultsFile) {
        return Integer.parseInt(faultsFile.getName().split("-r\\[")[1].split("]-")[0]);
    }

    public static Domain parseDomain(File domainFile) {
        String name = domainFile.getParentFile().getName();
        List<String> requirements = parseRequirements(domainFile);
        List<String> types = parseTypes(domainFile);
        List<String> predicates = parsePredicates(domainFile);
        Map<String, Map<String, String>> actions = parseActions(domainFile);
        return new Domain(name, requirements, types, predicates, actions);
    }

    private static List<String> parseRequirements(File domainFile) {
        String domainString = readFromFile(domainFile);
        String[] parts = domainString.split("\\(:requirements\\s*:")[1].split("\\)")[0].replaceAll("\\s*", "").split(":");
        return new ArrayList<>(Arrays.asList(parts));
    }

    private static List<String> parseTypes(File domainFile) {
        String domainString = readFromFile(domainFile);
        String typesString = domainString.split("\\(:types\\s*")[1].split("\\)")[0].replaceAll("\\s+", " ");
        String[] parts = typesString.split(" - ");
        List<String> types = new ArrayList<>();
        String[] s1 = parts[0].split(" ");
        String[] s2 = parts[1].split(" ");
        for (String s: s1) {
            types.add(s + " - " + s2[0]);
        }
        for (int i = 1; i < parts.length-1; i++) {
            s1 = parts[i].split(" ");
            s2 = parts[i+1].split(" ");
            for (int j = 1; j < s1.length; j++) {
                types.add(s1[j] + " - " + s2[0]);
            }
        }
        return types;
    }

    private static List<String> parsePredicates(File domainFile) {
        String domainString = readFromFile(domainFile);
        String predicatesString  = domainString.split(("\\(:predicates"))[1].split("\\(:action")[0].replaceAll("\\s+"," ").replaceFirst("\\s*", "").replaceAll("\\s*\\)\\s*$", "");
        String[] splitted = predicatesString.split("\\)\\s*\\(");

        List<String> predicates = new ArrayList<>();
        for (int i = 0; i < splitted.length; i++) {
            if (i == 0) {
                predicates.add(splitted[i] + ")");
            } else if (i == splitted.length-1) {
                predicates.add("(" + splitted[i]);
            } else {
                predicates.add("(" + splitted[i] + ")");
            }
        }
        return predicates;
    }

    private static Map<String, Map<String, String>> parseActions(File domainFile) {
        String domainString = readFromFile(domainFile);
        String[] splitted = domainString.replaceAll("\\s*\\)\\s*\\)\\s*$", "").split("\\)\\s*\\(:action\\s*");
        Map<String, Map<String, String>> actions = new HashMap<>();
        for (int i = 1; i < splitted.length; i++) {
            String[] actionParts = splitted[i].replaceAll("\\s+", " ").replaceFirst("\\s+$", "").split(" :");
            String actionName = actionParts[0];
            Map<String, String> actionMap = createActionMap(actionParts);
            actions.put(actionName, actionMap);
        }
        return actions;
    }

    private static Map<String, String> createActionMap(String[] actionParts) {
        String parameters = actionParts[1].substring(12, actionParts[1].length()-1);
        String preconditions = actionParts[2].substring(18, actionParts[2].length()-1);
        String effects = actionParts[3].substring(12, actionParts[3].length()-1);
        Map<String, String> actionMap = new HashMap<>();
        actionMap.put("parameters", parameters);
        actionMap.put("preconditions", preconditions);
        actionMap.put("effects", effects);
        return actionMap;
    }

    public static Problem parseProblem(File problemFile) {
        String name = problemFile.getParentFile().getName();
        List<String> objects = parseObjects(problemFile);
        List<String> init = parseInit(problemFile);
        List<String> goal = parseGoal(problemFile);
        return new Problem(name, objects, init, goal);
    }

    private static List<String> parseObjects(File problemFile) {
        String problemFileString = readFromFile(problemFile);
        return new ArrayList<>(Arrays.asList(problemFileString.split("\\(:objects")[1].split("\\(:init")[0].replaceAll("\\s*\\)\\s*$", "").replaceAll("\\s+", ":").replaceAll(":-:", " - ").substring(1).split(":")));
    }

    private static List<String> parseInit(File problemFile) {
        String problemFileString = readFromFile(problemFile);
        return new ArrayList<>(Arrays.asList(problemFileString.split("\\(:init")[1].split("\\(:goal")[0].replaceAll("\\s*\\)\\s*$", "").replaceAll("\\s*\\(", "(").split("(?=\\()|(?<=\\))")));
    }

    public static List<String> parseGoal(File problemFile) {
        String problemFileString = readFromFile(problemFile);
        return new ArrayList<>(Arrays.asList(problemFileString.split("\\(:goal")[1].replaceAll("\\s+", " ").substring(6).replaceAll("\\s*\\)\\s*\\)\\s*\\)$", "").split("(?=\\() |(?<=\\)) ")));
    }

    public static List<String> parseAgentNames(File agentsFile) {
        String agentsFileString = readFromFile(agentsFile);
        return new ArrayList<>(Arrays.asList(agentsFileString.split(",")));
    }

    public static List<List<String>> parseCombinedPlan(File combinedPlanFile) {
        String combinedPlanFileString = readFromFile(combinedPlanFile);
        String[] jointActions = combinedPlanFileString.substring(1, combinedPlanFileString.length()-1).replaceAll("\\(nop \\)", "(nop)").split("]\\s*\\[");
        List<List<String>> combinedPlan = new ArrayList<>();
        for (String s : jointActions) {
            List<String> actions = new ArrayList<>(Arrays.asList(s.substring(1, s.length()-1).split("\\),\\(")));
            combinedPlan.add(actions);
        }
        return combinedPlan;
    }

    public static List<List<Map<String, String>>> computePlanConditions(List<List<String>> planActions, int agentsNum, Domain domain) {
        List<List<Map<String, String>>> pc = new ArrayList<>();
        for (int t = 0; t < planActions.size(); t++) {
            List<Map<String, String>> tpc = new ArrayList<>();
            for (int a = 0; a < agentsNum; a++) {
                Map<String, String> atpc = new HashMap<>();
                atpc.put("pre", extractActionGroundedConditions(planActions, t, a, "preconditions", domain));
                atpc.put("eff", extractActionGroundedConditions(planActions, t, a, "effects", domain));
                tpc.add(atpc);
            }
            pc.add(tpc);
        }
        return pc;
    }

    private static String extractActionGroundedConditions(List<List<String>> planActions, int t, int a, String conditionsType, Domain domain) {
        if (planActions.get(t).get(a).equals("nop")) {
            return "";
        } else {
            String groundedAction = planActions.get(t).get(a);
            String[] actionSignature = groundedAction.split(" ");
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

    public static List<String> parseFaults(File faultsFile) {
        String faultsFileString = readFromFile(faultsFile);
        String[] faults = faultsFileString.split("\r\n");
        return Arrays.asList(faults);
    }

    public static List<List<String>> parseTrajectory(File trajectoryFile) {
        String trajectoryFileString = readFromFile(trajectoryFile);
        String[] sequence = trajectoryFileString.substring(1,trajectoryFileString.length()-3).replace("(:init", "(:state").replace(":state  ", "").replace("operators: ", "").replaceAll("\\(nop \\)", "(nop)").split("\r\n");
        List<List<String>> trajectory = new ArrayList<>();
        for (int i = 0; i < sequence.length; i=i+2) {
            String s = sequence[i];
            List<String> l = new ArrayList<>(Arrays.asList(s.substring(2, s.length()-2).split("\\) \\(")));
            trajectory.add(l);
        }
        return trajectory;
    }

    public static double calculateCoefficientOfVariation(int agentsNum, List<List<String>> planActions) {
        int totalNumberOfActions = 0;
        double[] agentActionNumbers = new double[agentsNum];
        for (List<String> combinedPlanAction : planActions) {
            for (int a = 0; a < agentsNum; a++) {
                if (!combinedPlanAction.get(a).equals("nop")) {
                    agentActionNumbers[a] += 1;
                    totalNumberOfActions += 1;
                }
            }
        }
        double mean = totalNumberOfActions * 1.0 / agentsNum;

        double enumerator = 0;
        for (double agentActionNumber : agentActionNumbers) {
            enumerator += Math.pow(agentActionNumber - mean, 2);
        }

        double std = Math.sqrt(enumerator / agentsNum);

        return std / mean;
    }
}

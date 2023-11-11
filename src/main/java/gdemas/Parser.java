package gdemas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Parser {

    private static String readFromFile(File file) {
        try {
            return String.join(System.lineSeparator(), Files.readAllLines(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Map<String, String>> parseActionsOld(File domainFile) {
        String domainString = readFromFile(domainFile);
        String[] splitted = domainString.split("\\(:action");
        List<String> actionStrings = new ArrayList<>();
        for (int i = 1; i < splitted.length; i++) {
            if (i == splitted.length-1) {
                actionStrings.add(splitted[i].substring(1).replaceFirst("\\s+\\)\\s*\\)\\s*$", ""));
            } else {
                actionStrings.add(splitted[i].substring(1).replaceFirst("\\s+\\)\\s*$", ""));
            }
        }
        Map<String, Map<String, String>> liftedActions = new HashMap<>();
        for (String str : actionStrings) {
            String[] actionParts = str.replaceAll("\\s+", " ").split(":");
            String actionName = actionParts[0].replaceFirst("\\s+$", "");
            String actionParameters = actionParts[1].replaceFirst("\\)\\s+$", "").substring(12);
            String actionPreconditions = actionParts[2].replaceFirst("\\s+$", "").substring(13);
            String actionPostconditions = actionParts[3].replaceFirst("\\s+$", "").substring(7);
            Map<String, String> actionMap = new HashMap<>();
            actionMap.put("parameters", actionParameters);
            actionMap.put("preConditions", actionPreconditions);
            actionMap.put("postConditions", actionPostconditions);
            liftedActions.put(actionName, actionMap);
        }
        return liftedActions;
    }

    public static List<String> parseInitOld(File problemFile) {
        String problemFileString = readFromFile(problemFile);
        String initString = problemFileString.split("\\(:init")[1].split("\\(:goal")[0].replaceAll("\\s+", " ").replaceFirst("\\)\\s+$", "").substring(1);
        String[] splitted = initString.split("\\) ");
        List<String> initialPredicates = new ArrayList<>();
        for (String str: splitted) {
            initialPredicates.add(str + ")");
        }
        return initialPredicates;
    }

    public static List<List<String>> parseCombinedPlanActions(File combinedPlanFile) {
        String combinedPlanFileString = readFromFile(combinedPlanFile);
        String[] jointActions = combinedPlanFileString.split("\r\n");
        List<List<String>> combinedPlan = new ArrayList<>();
        for (String actionsString : jointActions) {
            String[] actions = actionsString.substring(1, actionsString.length() - 1).split(",");
            List<String> planStepActions = new ArrayList<>(Arrays.asList(actions));
            combinedPlan.add(planStepActions);
        }
        return combinedPlan;
    }

    public static List<List<Map<String, String>>> parseCombinedPlanConditions(List<List<String>> planActions, Map<String, Map<String, String>> liftedActions) {
        List<List<Map<String, String>>> planConditions = new ArrayList<>();
        for (List<String> planAction : planActions) {
            List<Map<String, String>> actionsAsConditions = new ArrayList<>();
            for (String s : planAction) {
                Map<String, String> actionAsConditions = convertActionToConditions(s, liftedActions);
                actionsAsConditions.add(actionAsConditions);
            }
            planConditions.add(actionsAsConditions);
        }
        return planConditions;
    }

    private static Map<String, String> convertActionToConditions(String actionString, Map<String, Map<String, String>> liftedActions) {
        Map<String, String> actionAsConditions = new HashMap<>();

        String[] actionSignature = actionString.substring(1, actionString.length()-1).split(" ");
        String actionName = actionSignature[0];
        if (actionName.equals("nop")) {
            actionAsConditions.put("preConditions", "");
            actionAsConditions.put("postConditions", "");
        } else {
            String[] actionParameters = liftedActions.get(actionName).get("parameters").split( " ");
            Map<String, String> actionArgs = new HashMap<>();

            for (int i = 1; i < actionSignature.length; i++) {
                actionArgs.put(actionParameters[(i-1)*3], actionSignature[i]);
            }

            String preC = liftedActions.get(actionName).get("preConditions");
            for (String p: actionArgs.keySet()) {
                preC = preC.replaceAll("\\?" + p.substring(1), actionArgs.get(p));
            }
            actionAsConditions.put("preConditions", preC);

            String postC = liftedActions.get(actionName).get("postConditions");
            for (String p: actionArgs.keySet()) {
                postC = postC.replaceAll("\\?" + p.substring(1), actionArgs.get(p));
            }
            actionAsConditions.put("postConditions", postC);
        }
        return actionAsConditions;
    }

    public static List<List<String>> parseFaults(File faultsFile) {
        List<List<String>> faults = new ArrayList<>();
        String faultsString = readFromFile(faultsFile);
        String[] splitted = faultsString.split("\r\n");
        for (String s : splitted) {
            String[] spl2 = s.split(" ");
            List<String> stepFaults = new ArrayList<>(Arrays.asList(spl2));
            faults.add(stepFaults);
        }
        return faults;
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

    private static List<String> parseGoal(File problemFile) {
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

    public static List<String> parseFaultsAsFlatList(File faultsFile) {
        String faultsFileString = readFromFile(faultsFile);
        String[] jointFaults = faultsFileString.split("\r\n");
        List<String> faultsAsFlatList = new ArrayList<>();
        for (int i = 0; i < jointFaults.length; i++) {
            String[] stepFaults = jointFaults[i].split(" ");
            for (int j = 0; j < stepFaults.length; j++) {
                if (stepFaults[j].equals("f")) {
                    faultsAsFlatList.add("t:" + i + ",a:" + j);
                }
            }
        }
        return faultsAsFlatList;
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
}

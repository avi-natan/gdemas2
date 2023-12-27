package gdemas;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gdemas.Utils.print;

public class ReasonerSuperb extends Reasoner {

    private final List<List<Integer>>                   diagnosersAgentNumbers;
    private final List<List<String>>                    diagnosersPredicates;
    private final List<List<List<String>>>              diagnosersPlanActions;
    private final List<List<List<Map<String, String>>>> diagnosersPlanConditions;
    private Model model;
    private long                                        xi;
    private BijectiveMap<String, BoolVar>               vmap;
    private final List<List<Diagnosis>>                 diagnosersDiagnoses;
    private List<GlobalDiagnosis>                       globalDiagnoses;

    public ReasonerSuperb(String     benchmarkName,
                          String     domainName,
                          String     problemName,
                          File       domainFile,
                          File       problemFile,
                          File       agentsFile,
                          File       combinedPlanFile,
                          File       faultsFile,
                          File       trajectoryFile,
                          String     observability,
                          long       timeout) {
        super(benchmarkName, domainName, problemName, domainFile, problemFile, agentsFile, combinedPlanFile, faultsFile, trajectoryFile, observability, timeout);
        this._REASONER_NAME = "superb";
        this.diagnosersAgentNumbers = this.computeDiagnosersAgentNumbers();
        this.diagnosersPredicates = this.computeDiagnosersPredicates();
        this.diagnosersPlanActions = this.computeDiagnosersPlanActions();
        this.diagnosersPlanConditions = this.computeDiagnosersPlanConditions();
        List<Integer> diagnosersInternalActionsNumbers = this.countInternalActions();
        List<Integer> diagnosersExternalActionsNumbers = this.countExternalActions();
        List<Integer> diagnosersTotalActionsNumbers = this.countTotalActions();
        this._LOCAL_INTERNAL_ACTIONS_NUMBERS = new ArrayList<>(diagnosersInternalActionsNumbers).toString();
        this._LOCAL_INTERNAL_ACTIONS_MIN = diagnosersInternalActionsNumbers.stream().mapToInt(Integer::intValue).min().orElseThrow(NoSuchElementException::new);
        this._LOCAL_INTERNAL_ACTIONS_MAX = diagnosersInternalActionsNumbers.stream().mapToInt(Integer::intValue).max().orElseThrow(NoSuchElementException::new);
        this._LOCAL_EXTERNAL_ACTIONS_NUMBERS = new ArrayList<>(diagnosersExternalActionsNumbers).toString();
        this._LOCAL_EXTERNAL_ACTIONS_MIN = diagnosersExternalActionsNumbers.stream().mapToInt(Integer::intValue).min().orElseThrow(NoSuchElementException::new);
        this._LOCAL_EXTERNAL_ACTIONS_MAX = diagnosersExternalActionsNumbers.stream().mapToInt(Integer::intValue).max().orElseThrow(NoSuchElementException::new);
        this._LOCAL_TOTAL_ACTIONS_NUMBERS = new ArrayList<>(diagnosersTotalActionsNumbers).toString();
        this._LOCAL_TOTAL_ACTIONS_MIN = diagnosersTotalActionsNumbers.stream().mapToInt(Integer::intValue).min().orElseThrow(NoSuchElementException::new);
        this._LOCAL_TOTAL_ACTIONS_MAX = diagnosersTotalActionsNumbers.stream().mapToInt(Integer::intValue).max().orElseThrow(NoSuchElementException::new);
        this._SIZE_MAX_SUBGROUP = this.diagnosersAgentNumbers.stream().mapToInt(List::size).max().orElse(0);;
        this._PERCENT_MAX_SUBGROUP = this._SIZE_MAX_SUBGROUP * 100.0 / this._AGENTS_NUM;
        this.diagnosersDiagnoses = new ArrayList<>();
        for (int D = 0; D < this.diagnosersAgentNumbers.size(); D++) {
            this.diagnosersDiagnoses.add(new ArrayList<>());
        }
        this.globalDiagnoses = new ArrayList<>();
    }

    private List<List<Integer>> computeDiagnosersAgentNumbers() {
        List<List<Integer>> diagnosersAgentNumbers = new ArrayList<>();
        List<Integer> odd = new ArrayList<>();
        List<Integer> even = new ArrayList<>();
        for (int a = 0; a < this._AGENTS_NUM; a++) {
            if (a % 2 == 0) {
                even.add(a);
            } else {
                odd.add(a);
            }
        }
        diagnosersAgentNumbers.add(even);
        diagnosersAgentNumbers.add(odd);
        return diagnosersAgentNumbers;
    }

    private List<List<String>> computeDiagnosersPredicates() {
        List<List<String>> diagnosersPredicates = new ArrayList<>();
        for (List<Integer> dList : this.diagnosersAgentNumbers) {
            List<String> dp = new ArrayList<>();
            for (Integer a : dList) {
                for (int t = 0; t < this._PLAN_LENGTH; t++) {
                    this.addPredicatesOfCondition(t, a, "pre", dp);
                    this.addPredicatesOfCondition(t, a, "eff", dp);
                }
            }
            diagnosersPredicates.add(dp);
        }
        return diagnosersPredicates;
    }

    private List<List<List<String>>> computeDiagnosersPlanActions() {
        List<List<List<String>>> diagnosersPlanActions = new ArrayList<>();

        for (List<Integer> dList : this.diagnosersAgentNumbers) {
            List<List<String>> singleDiagnoserPlanActions = new ArrayList<>();
            for (int t = 0; t < this._PLAN_LENGTH; t++) {
                List<String> stepPlanActions = new ArrayList<>();
                for (int a = 0; a < this._AGENTS_NUM; a++) {
                    String aStepAction = this._COMBINED_PLAN_ACTIONS.get(t).get(a);
                    if (aStepAction.equals("(nop)")) {
                        stepPlanActions.add("(nop)");
                    } else if (this.actionIsRelevantToDiagnoser(this.diagnosersAgentNumbers.indexOf(dList), t, a)) {
                        stepPlanActions.add(aStepAction);
                    } else {
                        stepPlanActions.add("(nop)");
                    }
                }
                singleDiagnoserPlanActions.add(stepPlanActions);
            }
            diagnosersPlanActions.add(singleDiagnoserPlanActions);
        }
        return diagnosersPlanActions;
    }

    private boolean actionIsRelevantToDiagnoser(int D, int t, int a) {
        List<String> actionPredicates = new ArrayList<>();
        this.addPredicatesOfCondition(t, a, "pre", actionPredicates);
        this.addPredicatesOfCondition(t, a, "eff", actionPredicates);
        for (String ap: actionPredicates) {
            if (this.diagnosersPredicates.get(D).contains(ap)) {
                return true;
            }
        }
        return false;
    }

    private List<List<List<Map<String, String>>>> computeDiagnosersPlanConditions() {
        List<List<List<Map<String, String>>>> agentsPlanConditions = new ArrayList<>();
        for (int d = 0; d < this.diagnosersAgentNumbers.size(); d++) {
            List<List<Map<String, String>>> planConditions = Parser.computePlanConditions(this.diagnosersPlanActions.get(d), this._AGENT_NAMES.size(), this._DOMAIN);
            agentsPlanConditions.add(planConditions);
        }
        return agentsPlanConditions;
    }

    private List<Integer> countInternalActions() {
        List<Integer> diagnosersInternalActionsNumbers = new ArrayList<>();
        for (int d = 0; d < this.diagnosersAgentNumbers.size(); d++) {
            diagnosersInternalActionsNumbers.add(this.countInternalActions(d));
        }
        return diagnosersInternalActionsNumbers;
    }

    private int countInternalActions(int D) {
        int count = 0;
        for (List<String> planAction : this.diagnosersPlanActions.get(D)) {
            for (int a = 0; a < planAction.size(); a++) {
                if (this.diagnosersAgentNumbers.get(D).contains(a)) {
                    if (!planAction.get(a).equals("(nop)")) {
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

    private List<Integer> countExternalActions() {
        List<Integer> diagnosersInternalActionsNumbers = new ArrayList<>();
        for (int d = 0; d < this.diagnosersAgentNumbers.size(); d++) {
            diagnosersInternalActionsNumbers.add(this.countExternalActions(d));
        }
        return diagnosersInternalActionsNumbers;
    }

    private int countExternalActions(int D) {
        int count = 0;
        for (List<String> planAction : this.diagnosersPlanActions.get(D)) {
            for (int a = 0; a < planAction.size(); a++) {
                if (!this.diagnosersAgentNumbers.get(D).contains(a)) {
                    if (!planAction.get(a).equals("(nop)")) {
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

    private List<Integer> countTotalActions() {
        List<Integer> diagnosersInternalActionsNumbers = new ArrayList<>();
        for (int d = 0; d < this.diagnosersAgentNumbers.size(); d++) {
            diagnosersInternalActionsNumbers.add(this.countTotalActions(d));
        }
        return diagnosersInternalActionsNumbers;
    }

    private int countTotalActions(int D) {
        int count = 0;
        for (List<String> planAction : this.diagnosersPlanActions.get(D)) {
            for (int a = 0; a < planAction.size(); a++) {
                if (!planAction.get(a).equals("(nop)")) {
                    count += 1;
                }
            }
        }
        return count;
    }

    @Override
    public void diagnoseProblem() {
        for (int D = 0; D < this.diagnosersAgentNumbers.size(); D++) {
            // create the model
            this.model = new Model();

            // set the variable name count number
            this.xi = 1;

            // initialize the variable map
            this.vmap = new BijectiveMap<>();

            // model problem
//            print(java.time.LocalTime.now() + " diagnoser " + (D+1) + "/" + this.diagnosersAgentNumbers.size() + ": " + "modelling...");
            // offline part modelling
            this.modelProblemOfflinePart(D);

            // online part modelling
            Instant start = Instant.now();
            this.modelProblemOnlinePart(D);
            Instant end = Instant.now();
            long runtime = Duration.between(start, end).toMillis();
            if (this._MODELLING_AGENT_NAME.isEmpty()) {
                this._MODELLING_AGENT_NAME = "diagnoser" + D;
                this._MODELLING_PREDICATES_NUM = this.diagnosersPredicates.get(D).size();
                this._MODELLING_ACTIONS_NUM = this.countActionsNumber(this.diagnosersPlanActions.get(D));
                this._MODELLING_VARIABLES_NUM = this.model.getNbVars();
                this._MODELLING_CONSTRAINTS_NUM = this.model.getNbCstrs();
                this._MODELLING_RUNTIME = runtime;
            } else if (runtime > this._MODELLING_RUNTIME) {
                this._MODELLING_AGENT_NAME = "diagnoser" + D;
                this._MODELLING_PREDICATES_NUM = this.diagnosersPredicates.get(D).size();
                this._MODELLING_ACTIONS_NUM = this.countActionsNumber(this.diagnosersPlanActions.get(D));
                this._MODELLING_VARIABLES_NUM = this.model.getNbVars();
                this._MODELLING_CONSTRAINTS_NUM = this.model.getNbCstrs();
                this._MODELLING_RUNTIME = runtime;
            }

//            print(999);

            // solve problem (metrics are recorded within the problem-solving function)
            this.solveProblem(D);
//            print(9);
        }

        // measurements after local diagnoses
        this._LOCAL_DIAGNOSES_NUMBERS = this.diagnosersDiagnoses.stream().map(List::size).collect(Collectors.toList()).toString();
        this._LOCAL_DIAGNOSES_MIN = this.diagnosersDiagnoses.stream().mapToInt(List::size).min().orElse(0);
        this._LOCAL_DIAGNOSES_MAX = this.diagnosersDiagnoses.stream().mapToInt(List::size).max().orElse(0);

        // combining diagnoses
//        print(java.time.LocalTime.now() + ": " + "combining...");
        this.combineDiagnoses();
        print(java.time.LocalTime.now() + ": " + this._REASONER_NAME + " - success. Diagnoses num: " + this._DIAGNOSES_NUM + ", Combine time in MS: " + this._COMBINING_RUNTIME + ", Total time in MS: " + this._TOTAL_RUNTIME + ", Timedout: " + this._TIMEDOUT);

        // print diagnoses
//        this.printDiagnoses();
//        print(34);
    }

    private void modelProblemOfflinePart(int D) {
        // initialize state variables
        this.initializeStateVariables(D);

        // initialize health variables
        this.initializeHealthVariables(D);

        // adding constraints
        // health states mutual exclusiveness
        this.constraintHealthStatesMutualExclusive(D);

        // transition of non-effected variables
        this.constraintTransitionNonEffected(D);

        // transition of variables in the effects of an action in a normal state
        this.constraintTransitionNormalState(D);

        // transition of variables in the effects of an action in a faulty state
        this.constraintTransitionFaultyState(D);

        // transition of variables in the effects of an action in a conflict state
        this.constraintTransitionConflictState(D);

        // transition of variables in the effects of an action in an innocent state
        this.constraintTransitionInnocentState(D);

        // transition of variables in the effects of an action in a guilty state
        this.constraintTransitionGuiltyState(D);
    }

    private void modelProblemOnlinePart (int D) {
        // observation
        this.constraintObservation(D);
    }

    private void initializeStateVariables(int D) {
        // create csp boolean variables that represent the states for diagnoser D
        for (int t = 0; t < this.diagnosersPlanActions.get(D).size()+1; t++) {
            for (String predicate: this.diagnosersPredicates.get(D)) {
                this.vmap.put("S:" + t + ":" + predicate, this.model.boolVar("x" + this.xi++));
            }
        }
    }

    private void initializeHealthVariables(int D) {
        for (int t = 0; t < this.diagnosersPlanActions.get(D).size(); t++) {
            for (int a = 0; a < this.diagnosersPlanActions.get(D).get(t).size(); a++) {
                if (!this.diagnosersPlanActions.get(D).get(t).get(a).equals("(nop)")) {
                    if (this.diagnosersAgentNumbers.get(D).contains(a)) {
                        this.vmap.put("H:" + t + ":" + a + ":h", this.model.boolVar("x" + this.xi++));
                        this.vmap.put("H:" + t + ":" + a + ":f", this.model.boolVar("x" + this.xi++));
                        this.vmap.put("H:" + t + ":" + a + ":c", this.model.boolVar("x" + this.xi++));
                    } else {
                        this.vmap.put("H:" + t + ":" + a + ":i", this.model.boolVar("x" + this.xi++));
                        this.vmap.put("H:" + t + ":" + a + ":g", this.model.boolVar("x" + this.xi++));
                    }
                }
            }
        }
    }

    private void constraintHealthStatesMutualExclusive(int D) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (this.diagnosersAgentNumbers.get(D).contains(a)) {
                    if (!this.diagnosersPlanActions.get(D).get(t).get(a).equals("(nop)")) {
                        BoolVar h = this.vmap.getValue("H:" + t + ":" + a + ":h");
                        BoolVar f = this.vmap.getValue("H:" + t + ":" + a + ":f");
                        BoolVar c = this.vmap.getValue("H:" + t + ":" + a + ":c");
                        BoolVar[] vars = {h, f, c};
                        this.model.sum(vars, "=", 1).post();
                    }
                } else {
                    if (!this.diagnosersPlanActions.get(D).get(t).get(a).equals("(nop)")) {
                        BoolVar i = this.vmap.getValue("H:" + t + ":" + a + ":i");
                        BoolVar g = this.vmap.getValue("H:" + t + ":" + a + ":g");
                        BoolVar[] vars = {i, g};
                        this.model.sum(vars, "=", 1).post();
                    }
                }
            }
        }
    }

    private void constraintTransitionNonEffected(int D) {
        for (int t = 1; t < this._TRAJECTORY.size(); t++) {
            for (String p: this.diagnosersPredicates.get(D)) {
                if (this.nonEffectedPredicate(D, p, t-1)) {
                    BoolVar v = this.vmap.getValue("S:" + t + ":" + p);
                    BoolVar v_prev = this.vmap.getValue("S:" + (t-1) + ":" + p);
                    this.model.ifOnlyIf(this.model.and(v), this.model.and(v_prev));
                }
            }
        }
    }

    private boolean nonEffectedPredicate(int D, String predicate, int jointActionTime) {
        for (Map<String, String> m : this.diagnosersPlanConditions.get(D).get(jointActionTime)) {
            if (m.get("eff").contains(predicate)) {
                return false;
            }
        }
        return true;
    }

    private void constraintTransitionNormalState(int D) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (Integer a : this.diagnosersAgentNumbers.get(D)) {
                if (!this.diagnosersPlanActions.get(D).get(t).get(a).equals("(nop)")) {
                    BoolVar n = this.vmap.getValue("H:" + t + ":" + a + ":h");
                    BoolVar[] n_pre = addValidPre(D, t, a, n);
                    Constraint normalAndPreValid = this.model.and(n_pre);

                    String[] eff = this.diagnosersPlanConditions.get(D).get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
                    int nextVarT = t + 1;
                    Constraint[] effConstraints = Arrays.stream(eff).map(s -> s.contains("not") ? this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(5, s.length() - 1)))) : this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s))).toArray(Constraint[]::new);

                    Constraint effOccur = this.model.and(effConstraints);
                    this.model.ifThen(normalAndPreValid, effOccur);
                }
            }
        }
    }

    private void constraintTransitionFaultyState(int D) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (Integer a : this.diagnosersAgentNumbers.get(D)) {
                if (!this.diagnosersPlanActions.get(D).get(t).get(a).equals("(nop)")) {
                    BoolVar f = this.vmap.getValue("H:" + t + ":" + a + ":f");
                    BoolVar[] f_pre = addValidPre(D, t, a, f);
                    Constraint faultyAndPreValid = this.model.and(f_pre);

                    Constraint effNotOccur = effNotOccurCons(D, t, a);

                    this.model.ifThen(faultyAndPreValid, effNotOccur);
                }
            }
        }
    }

    private void constraintTransitionConflictState(int D) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (Integer a : this.diagnosersAgentNumbers.get(D)) {
                if (!this.diagnosersPlanActions.get(D).get(t).get(a).equals("(nop)")) {
                    Constraint c = this.model.and(this.vmap.getValue("H:" + t + ":" + a + ":c"));
                    String[] pre = this.diagnosersPlanConditions.get(D).get(t).get(a).get("pre").replaceAll("\\(", "S:" + t + ":(").split("(?=\\() |(?<=\\)) ");
                    Constraint[] all_pre = Stream.concat(Stream.of(this.model.trueConstraint()), Arrays.stream(Arrays.stream(pre).map(this.vmap::getValue).map(this.model::and).toArray(Constraint[]::new))).toArray(Constraint[]::new);
                    Constraint notAllPreValid = this.model.not(this.model.and(all_pre));
                    this.model.ifOnlyIf(c, notAllPreValid);

                    Constraint effNotOccur = effNotOccurCons(D, t, a);

                    this.model.ifThen(c, effNotOccur);
                }
            }
        }
    }

    private Constraint effNotOccurCons(int D, int t, int a) {
        String[] eff = this.diagnosersPlanConditions.get(D).get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
        int nextVarT = t + 1;
        Constraint[] effConstraints = Arrays.stream(eff).map(s -> s.contains("(not ") ?
                this.model.and(
                        this.model.or(this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(5, s.length()-1)))), this.model.and(this.vmap.getValue("S:" + (nextVarT-1) + ":" + s.substring(5, s.length()-1)))),
                        this.model.or(this.model.not(this.model.and(this.vmap.getValue("S:" + (nextVarT-1) + ":" + s.substring(5, s.length()-1)))), this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(5, s.length()-1))))
                ) :
                this.model.and(
                        this.model.or(this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s))), this.model.and(this.vmap.getValue("S:" + (nextVarT-1) + ":" + s))),
                        this.model.or(this.model.not(this.model.and(this.vmap.getValue("S:" + (nextVarT-1) + ":" + s))), this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s)))
                )
        ).toArray(Constraint[]::new);
        return this.model.and(effConstraints);
    }

    private BoolVar[] addValidPre(int D, int t, int a, BoolVar v) {
        String[] pre = this.diagnosersPlanConditions.get(D).get(t).get(a).get("pre").replaceAll("\\(", "S:" + t + ":(").split("(?=\\() |(?<=\\)) ");
        return Stream.concat(Stream.of(v), Arrays.stream(Arrays.stream(pre).map(this.vmap::getValue).toArray(BoolVar[]::new))).toArray(BoolVar[]::new);
    }

    private void constraintTransitionInnocentState(int D) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (!this.diagnosersAgentNumbers.get(D).contains(a)) {
                    if (!this.diagnosersPlanActions.get(D).get(t).get(a).equals("(nop)")) {
                        Constraint i = this.model.and(this.vmap.getValue("H:" + t + ":" + a + ":i"));

                        String[] eff = this.diagnosersPlanConditions.get(D).get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
                        List<String> relevantEff = computeRelevantEffects(D, eff);
                        Constraint[] effOccur = new Constraint[relevantEff.size()+1];
                        effOccur[0] = this.model.trueConstraint();
                        for (int j = 0; j < relevantEff.size(); j++) {
                            String s = relevantEff.get(j);
                            if (s.contains("(not ")) {
                                effOccur[j+1] = this.model.not(this.model.and(this.vmap.getValue("S:" + (t+1) + ":" + s.substring(5, s.length()-1))));
                            } else {
                                effOccur[j+1] = this.model.and(this.vmap.getValue("S:" + (t+1) + ":" + s));
                            }
                        }

                        this.model.ifThen(i, this.model.and(effOccur));
                    }
                }
            }
        }
    }

    private void constraintTransitionGuiltyState(int D) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (!this.diagnosersAgentNumbers.get(D).contains(a)) {
                    if (!this.diagnosersPlanActions.get(D).get(t).get(a).equals("(nop)")) {
                        Constraint g = this.model.and(this.vmap.getValue("H:" + t + ":" + a + ":g"));

                        String[] eff = this.diagnosersPlanConditions.get(D).get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
                        List<String> relevantEff = computeRelevantEffects(D, eff);

                        Constraint[] effNotOccur = new Constraint[relevantEff.size()+1];
                        effNotOccur[0] = this.model.trueConstraint();
                        for (int j = 0; j < relevantEff.size(); j++) {
                            String s = relevantEff.get(j);
                            BoolVar b;
                            BoolVar bp;
                            String substring;
                            if (s.contains("(not ")) {
                                substring = s.substring(5, s.length() - 1);
                            } else {
                                substring = s;
                            }
                            b = this.vmap.getValue("S:" + (t + 1) + ":" + substring);
                            bp = this.vmap.getValue("S:" + t + ":" + substring);
                            effNotOccur[j+1] = this.model.and(
                                    this.model.or(this.model.not(this.model.and(b)), this.model.and(bp)),
                                    this.model.or(this.model.not(this.model.and(bp)), this.model.and(b))
                            );
                        }

                        this.model.ifThen(g, this.model.and(effNotOccur));
                    }
                }
            }
        }
    }

    private List<String> computeRelevantEffects(int D, String[] eff) {
        List<String> rel = new ArrayList<>();
        for (String s : eff) {
            String e = s;
            if (e.contains("(not ")) {
                e = e.substring(5, e.length() - 1);
            }
            if (this.diagnosersPredicates.get(D).contains(e)) {
                rel.add(s);
            }
        }
        return rel;
    }

    private void constraintObservation(int D) {
        for (Integer t : this._OBSERVABLE_STATES) {
            for (String gp: this.diagnosersPredicates.get(D)) {
                if (this._TRAJECTORY.get(t).contains(gp)) {
                    this.model.and(this.vmap.getValue("S:" + t + ":" + gp)).post();
                } else {
                    this.model.not(this.model.and(this.vmap.getValue("S:" + t + ":" + gp))).post();
                }
            }
        }
    }

    private void solveProblem(int D) {
//        print(java.time.LocalTime.now() + " diagnoser " + (D+1) + "/" + this.diagnosersAgentNumbers.size() + " (agents " + this.diagnosersAgentNumbers.get(D) + "): " + "solving...");
        Solver solver = this.model.getSolver();

        solver.limitTime(this._TIMEOUT);
//        int searchCount = 1;
//        long cumulativeRuntimeMSPrev;
        long cumulativeRuntimeMS = 0;
//        long runtimeMS;
        boolean isStopMet;

//        searchCount += 1;
//        print(java.time.LocalTime.now() + " diagnoser " + (D+1) + "/" + this.diagnosersAgentNumbers.size() + "(agents " + this.diagnosersAgentNumbers.get(D) + "): " + "before " + searchCount);
        Solution s = solver.findSolution();
//        cumulativeRuntimeMSPrev = cumulativeRuntimeMS;
        cumulativeRuntimeMS = solver.getTimeCountInNanoSeconds() / (1000 * 1000);
//        runtimeMS = cumulativeRuntimeMS - cumulativeRuntimeMSPrev;
        isStopMet = solver.isStopCriterionMet();
//        print(java.time.LocalTime.now() + " diagnoser " + (D+1) + "/" + this.diagnosersAgentNumbers.size() + "(agents " + this.diagnosersAgentNumbers.get(D) + "): " + "after " + searchCount + ", runtime in MS: " + runtimeMS + ", cumulative runtime in MS: " + cumulativeRuntimeMS + ". Stop criterion met: " + isStopMet);

        while (s != null) {
            Diagnosis d = new Diagnosis(s, this.vmap, this._PLAN_LENGTH, this._AGENTS_NUM);
            if (!this.containsDiagnosis(this.diagnosersDiagnoses.get(D), d)) {
                this.diagnosersDiagnoses.get(D).add(d);
            }

//            searchCount += 1;
//            print(java.time.LocalTime.now() + " diagnoser " + (D+1) + "/" + this.diagnosersAgentNumbers.size() + "(agents " + this.diagnosersAgentNumbers.get(D) + "): " + "before " + searchCount);
            s = solver.findSolution();
//            cumulativeRuntimeMSPrev = cumulativeRuntimeMS;
            cumulativeRuntimeMS = solver.getTimeCountInNanoSeconds() / (1000 * 1000);
//            runtimeMS = cumulativeRuntimeMS - cumulativeRuntimeMSPrev;
            isStopMet = solver.isStopCriterionMet();
//            print(java.time.LocalTime.now() + " diagnoser " + (D+1) + "/" + this.diagnosersAgentNumbers.size() + "(agents " + this.diagnosersAgentNumbers.get(D) + "): " + "after " + searchCount  + ", runtime in MS: " + runtimeMS + ", cumulative runtime in MS: " + cumulativeRuntimeMS + ". Stop criterion met: " + isStopMet);
        }
//        print(java.time.LocalTime.now() + " diagnoser " + (D+1) + "/" + this.diagnosersAgentNumbers.size() + " (agents " + this.diagnosersAgentNumbers.get(D) + "): " + this.diagnosersDiagnoses.get(D).size() + " diagnoses, Time in MS: " + cumulativeRuntimeMS);

        if (this._SOLVING_AGENT_NAME.isEmpty()) {
            this._SOLVING_AGENT_NAME = "diagnoser" + D;
            this._SOLVING_PREDICATES_NUM = this.diagnosersPredicates.get(D).size();
            this._SOLVING_ACTIONS_NUM = this.countActionsNumber(this.diagnosersPlanActions.get(D));
            this._SOLVING_VARIABLES_NUM = this.model.getNbVars();
            this._SOLVING_CONSTRAINTS_NUM = this.model.getNbCstrs();
            this._SOLVING_DIAGNOSES_NUM = this.diagnosersDiagnoses.get(D).size();
            this._SOLVING_RUNTIME = cumulativeRuntimeMS;
        } else if (cumulativeRuntimeMS > this._SOLVING_RUNTIME) {
            this._SOLVING_AGENT_NAME = "diagnoser" + D;
            this._SOLVING_PREDICATES_NUM = this.diagnosersPredicates.get(D).size();
            this._SOLVING_ACTIONS_NUM = this.countActionsNumber(this.diagnosersPlanActions.get(D));
            this._SOLVING_VARIABLES_NUM = this.model.getNbVars();
            this._SOLVING_CONSTRAINTS_NUM = this.model.getNbCstrs();
            this._SOLVING_DIAGNOSES_NUM = this.diagnosersDiagnoses.get(D).size();
            this._SOLVING_RUNTIME = cumulativeRuntimeMS;
        }
        if (isStopMet) {
            this._TIMEDOUT = 1;
        }
    }

    private boolean containsDiagnosis(List<Diagnosis> diagnoses, Diagnosis nd) {
        for (Diagnosis d: diagnoses) {
            if (d.hash.equals(nd.hash)) {
                return true;
            }
        }
        return false;
    }

    private void combineDiagnoses() {
        long remainingAllowedTime = Math.max(0, this._TIMEOUT - this._SOLVING_RUNTIME);
        if (remainingAllowedTime <= 0) {
            this._TIMEDOUT = 1;
            this._COMBINING_RUNTIME = 0;
            this._TOTAL_RUNTIME = this._SOLVING_RUNTIME + this._COMBINING_RUNTIME;
            this._DIAGNOSES_NUM = this.globalDiagnoses.size();
            return;
        }

        // initiating the runtime counter for this function
        long elapsedCombiningTime = 0L;
        Instant start;
        Instant end;

        start = Instant.now();
        List<Integer> sortedByLocalDiagnosesNum = sortByLocalDiagnosesNum(this.diagnosersDiagnoses);
        // creating partial global diagnoses out of the local diagnoses of the first agent
        initialPartialGlobalDiagnoses(sortedByLocalDiagnosesNum.get(0));
        end = Instant.now();
        elapsedCombiningTime += Duration.between(start, end).toMillis();
        if (elapsedCombiningTime >= remainingAllowedTime) {
            this._TIMEDOUT = 1;
            this._COMBINING_RUNTIME = elapsedCombiningTime;
            this._TOTAL_RUNTIME = this._SOLVING_RUNTIME + this._COMBINING_RUNTIME;
            this._DIAGNOSES_NUM = this.globalDiagnoses.size();
            return;
        }

        // combining the global diagnoses with the local diagnoses of every subsequent diagnoser
        for (int d = 1; d < sortedByLocalDiagnosesNum.size(); d++) {
            List<GlobalDiagnosis> newGlobalDiagnoses = new ArrayList<>();
            for (GlobalDiagnosis gd: this.globalDiagnoses) {
                for (int diag = 0; diag < this.diagnosersDiagnoses.get(sortedByLocalDiagnosesNum.get(d)).size(); diag++) {
                    start = Instant.now();
                    Diagnosis ld = this.diagnosersDiagnoses.get(sortedByLocalDiagnosesNum.get(d)).get(diag);
                    GlobalDiagnosis ngd = createNewGlobalDiagnosis(gd, ld, sortedByLocalDiagnosesNum.get(d), diag);
                    if (ngd != null) {
                        if (!this.containsGlobalDiagnosis(newGlobalDiagnoses, ngd)) {
                            newGlobalDiagnoses.add(ngd);
                        }
                    }
                    end = Instant.now();
                    elapsedCombiningTime += Duration.between(start, end).toMillis();
                    if (elapsedCombiningTime >= remainingAllowedTime) {
                        this._TIMEDOUT = 1;
                        this._COMBINING_RUNTIME = elapsedCombiningTime;
                        this._TOTAL_RUNTIME = this._SOLVING_RUNTIME + this._COMBINING_RUNTIME;
                        this._DIAGNOSES_NUM = this.globalDiagnoses.size();
                        return;
                    }
                }
            }
            this.globalDiagnoses = newGlobalDiagnoses;
        }

        this._COMBINING_RUNTIME = elapsedCombiningTime;
        this._TOTAL_RUNTIME = this._SOLVING_RUNTIME + this._COMBINING_RUNTIME;
        this._DIAGNOSES_NUM = this.globalDiagnoses.size();
    }

    private List<Integer> sortByLocalDiagnosesNum(List<List<Diagnosis>> agentsDiagnoses) {
        List<Integer> sortedByLocalDiagnosesNum = new ArrayList<>();

        List<Pair> pairs = new ArrayList<>();
        for (int a = 0; a < agentsDiagnoses.size(); a++) {
            pairs.add(new Pair(a, agentsDiagnoses.get(a).size()));
        }

        pairs.sort(Comparator.comparing(Pair::getNum2));

        for (Pair p : pairs) {
            sortedByLocalDiagnosesNum.add(p.getNum1());
        }

        return sortedByLocalDiagnosesNum;
    }

    private void initialPartialGlobalDiagnoses(int D) {
        for (int d = 0; d < this.diagnosersDiagnoses.get(D).size(); d++) {
            List<List<String>> actionHealthStates = new ArrayList<>();
            for (List<String> ls: this.diagnosersDiagnoses.get(D).get(d).actionHealthStates) {
                List<String> nls = new ArrayList<>(ls);
                actionHealthStates.add(nls);
            }
            GlobalDiagnosis gd = new GlobalDiagnosis(actionHealthStates, List.of(D), List.of(d));
            this.globalDiagnoses.add(gd);
        }
    }

    private GlobalDiagnosis createNewGlobalDiagnosis(GlobalDiagnosis gd, Diagnosis ld, int D, int d) {
        List<List<String>> newHealthStates = new ArrayList<>();
        for (int t = 0; t < gd.actionHealthStates.size(); t++) {
            List<String> snhs = new ArrayList<>();
            for (int a = 0; a < gd.actionHealthStates.get(t).size(); a++) {
                String gds = gd.actionHealthStates.get(t).get(a);
                String lds = ld.actionHealthStates.get(t).get(a);
                String nds = "";
                switch (gds+lds) {
                    case "xx":
                        nds = "x";
                        break;

                    case "xi":
                    case "ix":
                    case "ii":
                        nds = "i";
                        break;

                    case "xg":
                    case "gx":
                    case "gg":
                        nds = "g";
                        break;

                    case "xh":
                    case "ih":
                    case "hx":
                    case "hi":
                    case "hh":
                        nds = "h";
                        break;

                    case "xf":
                    case "gf":
                    case "fx":
                    case "fg":
                    case "ff":
                        nds = "f";
                        break;

                    case "xc":
                    case "gc":
                    case "cx":
                    case "cg":
                    case "cc":
                        nds = "c";
                        break;

                    case "ig":
                    case "if":
                    case "ic":
                    case "gi":
                    case "gh":
                    case "hg":
                    case "hf":
                    case "hc":
                    case "fi":
                    case "fh":
                    case "fc":
                    case "ci":
                    case "ch":
                    case "cf":
                        return null;
                }
                snhs.add(nds);
            }
            newHealthStates.add(snhs);
        }
        List<Integer> newConstituentDiagnosisAgents = new ArrayList<>(gd.constituentDiagnosisAgents);
        newConstituentDiagnosisAgents.add(D);
        List<Integer> newConstituentDiagnosisIndices = new ArrayList<>(gd.constituentDiagnosisIndices);
        newConstituentDiagnosisIndices.add(d);
        return new GlobalDiagnosis(newHealthStates, newConstituentDiagnosisAgents, newConstituentDiagnosisIndices);
    }

    private boolean containsGlobalDiagnosis(List<GlobalDiagnosis> newGlobalDiagnoses, GlobalDiagnosis ngd) {
        for (GlobalDiagnosis gd: newGlobalDiagnoses ) {
            if (gd.hash.equals(ngd.hash)) {
                return true;
            }
        }
        return false;
    }



}

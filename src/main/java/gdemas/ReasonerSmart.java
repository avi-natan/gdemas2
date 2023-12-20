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

public class ReasonerSmart extends Reasoner {

    private final List<List<String>>                    agentsPredicates;
    private final List<List<List<String>>>              agentsPlanActions;
    private final List<List<List<Map<String, String>>>> agentsPlanConditions;
    private Model                                       model;
    private long                                        xi;
    private BijectiveMap<String, BoolVar>               vmap;
    private final List<List<Diagnosis>>                 agentsDiagnoses;
    private List<GlobalDiagnosis>                       globalDiagnoses;

    public ReasonerSmart(String     benchmarkName,
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
        this._REASONER_NAME = "smart";
        this.agentsPredicates = this.computeAgentsPredicates();
        this.agentsPlanActions = this.computeAgentsPlanActions();
        this.agentsPlanConditions = this.computeAgentsPlanConditions();
        List<Integer> agentsInternalActionsNumbers = this.countInternalActions();
        List<Integer> agentsExternalActionsNumbers = this.countExternalActions();
        List<Integer> agentsTotalActionsNumbers = this.countTotalActions();
        this._LOCAL_INTERNAL_ACTIONS_NUMBERS   = new ArrayList<>(agentsInternalActionsNumbers).toString();
        this._LOCAL_INTERNAL_ACTIONS_MIN       = agentsInternalActionsNumbers.stream().mapToInt(Integer::intValue).min().orElseThrow(NoSuchElementException::new);
        this._LOCAL_INTERNAL_ACTIONS_MAX       = agentsInternalActionsNumbers.stream().mapToInt(Integer::intValue).max().orElseThrow(NoSuchElementException::new);
        this._LOCAL_EXTERNAL_ACTIONS_NUMBERS   = new ArrayList<>(agentsExternalActionsNumbers).toString();
        this._LOCAL_EXTERNAL_ACTIONS_MIN       = agentsExternalActionsNumbers.stream().mapToInt(Integer::intValue).min().orElseThrow(NoSuchElementException::new);
        this._LOCAL_EXTERNAL_ACTIONS_MAX       = agentsExternalActionsNumbers.stream().mapToInt(Integer::intValue).max().orElseThrow(NoSuchElementException::new);
        this._LOCAL_TOTAL_ACTIONS_NUMBERS   = new ArrayList<>(agentsTotalActionsNumbers).toString();
        this._LOCAL_TOTAL_ACTIONS_MIN       = agentsTotalActionsNumbers.stream().mapToInt(Integer::intValue).min().orElseThrow(NoSuchElementException::new);
        this._LOCAL_TOTAL_ACTIONS_MAX       = agentsTotalActionsNumbers.stream().mapToInt(Integer::intValue).max().orElseThrow(NoSuchElementException::new);
        this.agentsDiagnoses = new ArrayList<>();
        for (int a = 0; a < this._AGENTS_NUM; a++) {
            this.agentsDiagnoses.add(new ArrayList<>());
        }
        this.globalDiagnoses = new ArrayList<>();
    }

    private List<List<String>> computeAgentsPredicates() {
        List<List<String>> agentsPredicates = new ArrayList<>();
        for (int a = 0; a < this._AGENTS_NUM; a++) {
            List<String> ap = new ArrayList<>();
            for (int t = 0; t < this._PLAN_LENGTH; t++) {
                this.addPredicatesOfCondition(t, a, "pre", ap);
                this.addPredicatesOfCondition(t, a, "eff", ap);
            }
            agentsPredicates.add(ap);
        }
        return agentsPredicates;
    }

    private List<List<List<String>>> computeAgentsPlanActions() {
        List<List<List<String>>> agentsPlanActions = new ArrayList<>();
        for (int A = 0; A < this._AGENTS_NUM; A++) {
            List<List<String>> singleAgentPlanActions = new ArrayList<>();
            for (int t = 0; t < this._PLAN_LENGTH; t++) {
                List<String> stepPlanActions = new ArrayList<>();
                for (int a = 0; a < this._AGENTS_NUM; a++) {
                    String aStepAction = this._COMBINED_PLAN_ACTIONS.get(t).get(a);
                    if (aStepAction.equals("(nop)")) {
                        stepPlanActions.add("(nop)");
                    } else if (this.actionIsRelevantToAgentA(A, t, a)) {
                        stepPlanActions.add(aStepAction);
                    } else {
                        stepPlanActions.add("(nop)");
                    }
                }
                singleAgentPlanActions.add(stepPlanActions);
            }
            agentsPlanActions.add(singleAgentPlanActions);
        }
        return agentsPlanActions;
    }

    private boolean actionIsRelevantToAgentA(int A, int t, int a) {
        List<String> actionPredicates = new ArrayList<>();
        this.addPredicatesOfCondition(t, a, "pre", actionPredicates);
        this.addPredicatesOfCondition(t, a, "eff", actionPredicates);
        for (String ap: actionPredicates) {
            if (this.agentsPredicates.get(A).contains(ap)) {
                return true;
            }
        }
        return false;
    }

    private List<List<List<Map<String, String>>>> computeAgentsPlanConditions() {
        List<List<List<Map<String, String>>>> agentsPlanConditions = new ArrayList<>();
        for (int a = 0; a < this._AGENTS_NUM; a++) {
            List<List<Map<String, String>>> planConditions = Parser.computePlanConditions(this.agentsPlanActions.get(a), this._AGENT_NAMES.size(), this._DOMAIN);
            agentsPlanConditions.add(planConditions);
        }
        return agentsPlanConditions;
    }

    private List<Integer> countInternalActions() {
        List<Integer> agentsInternalActionsNumbers = new ArrayList<>();
        for (int a = 0; a < this._AGENTS_NUM; a++) {
            agentsInternalActionsNumbers.add(this.countInternalActions(a));
        }
        return agentsInternalActionsNumbers;
    }

    private int countInternalActions(int A) {
        int count = 0;
        for (List<String> planAction : this.agentsPlanActions.get(A)) {
            for (int a = 0; a < planAction.size(); a++) {
                if (a==A) {
                    if (!planAction.get(a).equals("(nop)")) {
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

    private List<Integer> countExternalActions() {
        List<Integer> agentsInternalActionsNumbers = new ArrayList<>();
        for (int a = 0; a < this._AGENTS_NUM; a++) {
            agentsInternalActionsNumbers.add(this.countExternalActions(a));
        }
        return agentsInternalActionsNumbers;
    }

    private int countExternalActions(int A) {
        int count = 0;
        for (List<String> planAction : this.agentsPlanActions.get(A)) {
            for (int a = 0; a < planAction.size(); a++) {
                if (a!=A) {
                    if (!planAction.get(a).equals("(nop)")) {
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

    private List<Integer> countTotalActions() {
        List<Integer> agentsInternalActionsNumbers = new ArrayList<>();
        for (int a = 0; a < this._AGENTS_NUM; a++) {
            agentsInternalActionsNumbers.add(this.countTotalActions(a));
        }
        return agentsInternalActionsNumbers;
    }

    private int countTotalActions(int A) {
        int count = 0;
        for (List<String> planAction : this.agentsPlanActions.get(A)) {
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
        for (int A = 0; A < this._AGENTS_NUM; A++) {
            // create the model
            this.model = new Model();

            // set the variable name count number
            this.xi = 1;

            // initialize the variable map
            this.vmap = new BijectiveMap<>();

//            // print some metadata
//            print(java.time.LocalTime.now() + " agent " + (A+1) + "/" + this._AGENTS_NUM + ": metadata:");
//            print("Relevant predicates number: " + this.agentsPredicates.get(A).size());
//            print("Relevant actions number: " + this.countActionsNumber(this.agentsPlanActions.get(A)));
//            double external = this.countExternalActions(A);
//            double internal = this.countInternalActions(A);
//            print("Relevant external actions number: " + external);
//            print("Relevant internal actions number: " + internal);
//            print("External/Internal actions rate: " + external / internal);

            // model problem
//            print(java.time.LocalTime.now() + " agent " + (A+1) + "/" + this._AGENTS_NUM + ": " + "modelling...");
            Instant start = Instant.now();
            this.modelProblem(A);
            Instant end = Instant.now();
            long runtime = Duration.between(start, end).toMillis();
            if (A == 0) {
                this._MODELLING_AGENT_NAME = this._AGENT_NAMES.get(A);
                this._MODELLING_PREDICATES_NUM = this.agentsPredicates.get(A).size();
                this._MODELLING_ACTIONS_NUM = this.countActionsNumber(this.agentsPlanActions.get(A));
                this._MODELLING_VARIABLES_NUM = this.model.getNbVars();
                this._MODELLING_CONSTRAINTS_NUM = this.model.getNbCstrs();
                this._MODELLING_RUNTIME = runtime;
            } else if (runtime > this._MODELLING_RUNTIME) {
                this._MODELLING_AGENT_NAME = this._AGENT_NAMES.get(A);
                this._MODELLING_PREDICATES_NUM = this.agentsPredicates.get(A).size();
                this._MODELLING_ACTIONS_NUM = this.countActionsNumber(this.agentsPlanActions.get(A));
                this._MODELLING_VARIABLES_NUM = this.model.getNbVars();
                this._MODELLING_CONSTRAINTS_NUM = this.model.getNbCstrs();
                this._MODELLING_RUNTIME = runtime;
            }

//            print(999);

            // solve problem
//            print(java.time.LocalTime.now() + " agent " + (A+1) + "/" + this._AGENTS_NUM + ": " + "solving...");
            start = Instant.now();
            this.solveProblem(A);
            end = Instant.now();
            runtime = Duration.between(start, end).toMillis();
            if (A == 0) {
                this._SOLVING_AGENT_NAME = this._AGENT_NAMES.get(A);
                this._SOLVING_PREDICATES_NUM = this.agentsPredicates.get(A).size();
                this._SOLVING_ACTIONS_NUM = this.countActionsNumber(this.agentsPlanActions.get(A));
                this._SOLVING_VARIABLES_NUM = this.model.getNbVars();
                this._SOLVING_CONSTRAINTS_NUM = this.model.getNbCstrs();
                this._SOLVING_DIAGNOSES_NUM = this.agentsDiagnoses.get(A).size();
                this._SOLVING_RUNTIME = runtime;
            } else if (runtime > this._SOLVING_RUNTIME) {
                this._SOLVING_AGENT_NAME = this._AGENT_NAMES.get(A);
                this._SOLVING_PREDICATES_NUM = this.agentsPredicates.get(A).size();
                this._SOLVING_ACTIONS_NUM = this.countActionsNumber(this.agentsPlanActions.get(A));
                this._SOLVING_VARIABLES_NUM = this.model.getNbVars();
                this._SOLVING_CONSTRAINTS_NUM = this.model.getNbCstrs();
                this._SOLVING_DIAGNOSES_NUM = this.agentsDiagnoses.get(A).size();
                this._SOLVING_RUNTIME = runtime;
            }
//            print(java.time.LocalTime.now() + " agent " + (A+1) + "/" + this._AGENTS_NUM + ": " + this.agentsDiagnoses.get(A).size() + " diagnoses, Time in MS: " + runtime);
//            print("");
        }

        // combining diagnoses
//        print(java.time.LocalTime.now() + ": " + "combining...");
        Instant start = Instant.now();
        this.combineDiagnoses();
        Instant end = Instant.now();
        this._COMBINING_RUNTIME = Duration.between(start, end).toMillis();
        this._TOTAL_RUNTIME = this._SOLVING_RUNTIME + this._COMBINING_RUNTIME;
        this._LOCAL_DIAGNOSES_NUMBERS = this.agentsDiagnoses.stream().map(List::size).collect(Collectors.toList()).toString();
        this._LOCAL_DIAGNOSES_MIN = this.agentsDiagnoses.stream().mapToInt(List::size).min().orElse(0);
        this._LOCAL_DIAGNOSES_MAX = this.agentsDiagnoses.stream().mapToInt(List::size).max().orElse(0);
        this._DIAGNOSES_NUM = this.globalDiagnoses.size();
        print(java.time.LocalTime.now() + ": Smart - success. Diagnoses num: " + this._DIAGNOSES_NUM + ", Combine time in MS: " + this._COMBINING_RUNTIME + ", Total time in MS: " + this._TOTAL_RUNTIME);

        // print diagnoses
//        this.printDiagnoses();
//        print(34);
    }

    private void modelProblem(int A) {
        // initialize state variables
        this.initializeStateVariables(A);

        // initialize health variables
        this.initializeHealthVariables(A);

        // adding constraints
        // health states mutual exclusiveness
        this.constraintHealthStatesMutualExclusive(A);

        // transition of non-effected variables
        this.constraintTransitionNonEffected(A);

        // transition of variables in the effects of an action in a normal state
        this.constraintTransitionNormalState(A);

        // transition of variables in the effects of an action in a faulty state
        this.constraintTransitionFaultyState(A);

        // transition of variables in the effects of an action in a conflict state
        this.constraintTransitionConflictState(A);

        // transition of variables in the effects of an action in an innocent state
        this.constraintTransitionInnocentState(A);

        // transition of variables in the effects of an action in a guilty state
        this.constraintTransitionGuiltyState(A);

        // observation
        this.constraintObservation(A);
    }


    private void initializeStateVariables(int A) {
        // create csp boolean variables that represent the states for agent A
        for (int t = 0; t < this.agentsPlanActions.get(A).size()+1; t++) {
            for (String predicate: this.agentsPredicates.get(A)) {
                this.vmap.put("S:" + t + ":" + predicate, this.model.boolVar("x" + this.xi++));
            }
        }
    }

    private void initializeHealthVariables(int A) {
        for (int t = 0; t < this.agentsPlanActions.get(A).size(); t++) {
            for (int a = 0; a < this.agentsPlanActions.get(A).get(t).size(); a++) {
                if (!this.agentsPlanActions.get(A).get(t).get(a).equals("(nop)")) {
                    if (a == A) {
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

    private void constraintHealthStatesMutualExclusive(int A) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (a == A) {
                    BoolVar h = this.vmap.getValue("H:" + t + ":" + a + ":h");
                    if (h != null) {
                        BoolVar f = this.vmap.getValue("H:" + t + ":" + a + ":f");
                        BoolVar c = this.vmap.getValue("H:" + t + ":" + a + ":c");
                        BoolVar[] vars = {h, f, c};
                        this.model.sum(vars, "=", 1).post();
                    }
                } else {
                    BoolVar i = this.vmap.getValue("H:" + t + ":" + a + ":i");
                    if (i != null) {
                        BoolVar g = this.vmap.getValue("H:" + t + ":" + a + ":g");
                        BoolVar[] vars = {i, g};
                        this.model.sum(vars, "=", 1).post();
                    }
                }
            }
        }
    }

    private void constraintTransitionNonEffected(int A) {
        for (int t = 1; t < this._TRAJECTORY.size(); t++) {
            for (String p: this.agentsPredicates.get(A)) {
                if (this.nonEffectedPredicate(A, p, t-1)) {
                    BoolVar v = this.vmap.getValue("S:" + t + ":" + p);
                    BoolVar v_prev = this.vmap.getValue("S:" + (t-1) + ":" + p);
                    this.model.ifOnlyIf(this.model.and(v), this.model.and(v_prev));
                }
            }
        }
    }

    private boolean nonEffectedPredicate(int A, String predicate, int jointActionTime) {
        for (Map<String, String> m : this.agentsPlanConditions.get(A).get(jointActionTime)) {
            if (m.get("eff").contains(predicate)) {
                return false;
            }
        }
        return true;
    }

    private void constraintTransitionNormalState(int A) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            if (!this.agentsPlanActions.get(A).get(t).get(A).equals("(nop)")) {
                BoolVar n = this.vmap.getValue("H:" + t + ":" + A + ":h");
                BoolVar[] n_pre = addValidPre(t, A, n);
                Constraint normalAndPreValid = this.model.and(n_pre);

                String[] eff = this.agentsPlanConditions.get(A).get(t).get(A).get("eff").split("(?=\\() |(?<=\\)) ");
                int nextVarT = t+1;
                Constraint[] effConstraints = Arrays.stream(eff).map(s -> s.contains("not") ? this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(5, s.length()-1)))) : this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s))).toArray(Constraint[]::new);

                Constraint effOccur = this.model.and(effConstraints);
                this.model.ifThen(normalAndPreValid, effOccur);
            }
        }
    }

    private void constraintTransitionFaultyState(int A) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            if (!this.agentsPlanActions.get(A).get(t).get(A).equals("(nop)")) {
                BoolVar f = this.vmap.getValue("H:" + t + ":" + A + ":f");
                BoolVar[] f_pre = addValidPre(t, A, f);
                Constraint faultyAndPreValid = this.model.and(f_pre);

                Constraint effNotOccur = effNotOccurCons(t, A);

                this.model.ifThen(faultyAndPreValid, effNotOccur);
            }
        }
    }

    private void constraintTransitionConflictState(int A) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            if (!this.agentsPlanActions.get(A).get(t).get(A).equals("(nop)")) {
                Constraint c = this.model.and(this.vmap.getValue("H:" + t + ":" + A + ":c"));
                String[] pre = this.agentsPlanConditions.get(A).get(t).get(A).get("pre").replaceAll("\\(", "S:" + t + ":(").split("(?=\\() |(?<=\\)) ");
                Constraint[] all_pre = Stream.concat(Stream.of(this.model.trueConstraint()), Arrays.stream(Arrays.stream(pre).map(this.vmap::getValue).map(this.model::and).toArray(Constraint[]::new))).toArray(Constraint[]::new);
                Constraint notAllPreValid = this.model.not(this.model.and(all_pre));
                this.model.ifOnlyIf(c, notAllPreValid);

                Constraint effNotOccur = effNotOccurCons(t, A);

                this.model.ifThen(c, effNotOccur);
            }
        }
    }

    private Constraint effNotOccurCons(int t, int A) {
        String[] eff = this.agentsPlanConditions.get(A).get(t).get(A).get("eff").split("(?=\\() |(?<=\\)) ");
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

    private BoolVar[] addValidPre(int t, int A, BoolVar v) {
        String[] pre = this.agentsPlanConditions.get(A).get(t).get(A).get("pre").replaceAll("\\(", "S:" + t + ":(").split("(?=\\() |(?<=\\)) ");
        return Stream.concat(Stream.of(v), Arrays.stream(Arrays.stream(pre).map(this.vmap::getValue).toArray(BoolVar[]::new))).toArray(BoolVar[]::new);
    }

    private void constraintTransitionInnocentState(int A) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (a != A) {
                    if (!this.agentsPlanActions.get(A).get(t).get(a).equals("(nop)")) {
                        Constraint i = this.model.and(this.vmap.getValue("H:" + t + ":" + a + ":i"));

                        String[] eff = this.agentsPlanConditions.get(A).get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
                        List<String> relevantEff = computeRelevantEffects(A, eff);
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

    private void constraintTransitionGuiltyState(int A) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (a != A) {
                    if (!this.agentsPlanActions.get(A).get(t).get(a).equals("(nop)")) {
                        Constraint g = this.model.and(this.vmap.getValue("H:" + t + ":" + a + ":g"));

                        String[] eff = this.agentsPlanConditions.get(A).get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
                        List<String> relevantEff = computeRelevantEffects(A, eff);

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

    private List<String> computeRelevantEffects(int A, String[] eff) {
        List<String> rel = new ArrayList<>();
        for (String s : eff) {
            String e = s;
            if (e.contains("(not ")) {
                e = e.substring(5, e.length() - 1);
            }
            if (this.agentsPredicates.get(A).contains(e)) {
                rel.add(s);
            }
        }
        return rel;
    }

    private void constraintObservation(int A) {
        for (Integer t : this._OBSERVABLE_STATES) {
            for (String gp: this.agentsPredicates.get(A)) {
                if (this._TRAJECTORY.get(t).contains(gp)) {
                    this.model.and(this.vmap.getValue("S:" + t + ":" + gp)).post();
                } else {
                    this.model.not(this.model.and(this.vmap.getValue("S:" + t + ":" + gp))).post();
                }
            }
        }
    }

    private void solveProblem(int A) {
        Solver solver = this.model.getSolver();
        Solution s = solver.findSolution();
        while (s != null) {
            Diagnosis d = new Diagnosis(s, this.vmap, this._PLAN_LENGTH, this._AGENTS_NUM);
            if (!this.containsDiagnosis(this.agentsDiagnoses.get(A), d)) {
                this.agentsDiagnoses.get(A).add(d);
            }
            s = solver.findSolution();
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
        List<Integer> sortedByLocalDiagnosesNum = sortByLocalDiagnosesNum(this.agentsDiagnoses);

        // creating partial global diagnoses out of the local diagnoses of the first agent
        initialPartialGlobalDiagnoses(sortedByLocalDiagnosesNum.get(0));

        // combining the global diagnoses with the local diagnoses of every subsequent agent
        for (int a = 1; a < sortedByLocalDiagnosesNum.size(); a++) {
            List<GlobalDiagnosis> newGlobalDiagnoses = new ArrayList<>();
            for (GlobalDiagnosis gd: this.globalDiagnoses) {
                for (int d = 0; d < this.agentsDiagnoses.get(sortedByLocalDiagnosesNum.get(a)).size(); d++) {
                    Diagnosis ld = this.agentsDiagnoses.get(sortedByLocalDiagnosesNum.get(a)).get(d);
                    GlobalDiagnosis ngd = createNewGlobalDiagnosis(gd, ld, sortedByLocalDiagnosesNum.get(a), d);
                    if (ngd != null) {
                        if (!this.containsGlobalDiagnosis(newGlobalDiagnoses, ngd)) {
                            newGlobalDiagnoses.add(ngd);
                        }
                    }
                }
            }
            this.globalDiagnoses = newGlobalDiagnoses;
        }
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

    private void initialPartialGlobalDiagnoses(int A) {
        for (int d = 0; d < this.agentsDiagnoses.get(A).size(); d++) {
            List<List<String>> actionHealthStates = new ArrayList<>();
            for (List<String> ls: this.agentsDiagnoses.get(A).get(d).actionHealthStates) {
                List<String> nls = new ArrayList<>(ls);
                actionHealthStates.add(nls);
            }
            GlobalDiagnosis gd = new GlobalDiagnosis(actionHealthStates, List.of(A), List.of(d));
            this.globalDiagnoses.add(gd);
        }
    }

    private GlobalDiagnosis createNewGlobalDiagnosis(GlobalDiagnosis gd, Diagnosis ld, int A, int d) {
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
        newConstituentDiagnosisAgents.add(A);
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

    private void printDiagnoses() {
        for (int d = 0; d < this.globalDiagnoses.size(); d++) {
            print("Diagnosis #" + (d+1) + ":\n" + this.globalDiagnoses.get(d));
        }
    }
}

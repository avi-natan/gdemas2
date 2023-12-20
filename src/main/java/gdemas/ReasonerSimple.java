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
import java.util.stream.Stream;

import static gdemas.Utils.*;

public class ReasonerSimple extends Reasoner {

    private final List<String>                      relevantGroundedPlanPredicates;
    private Model                                   model;
    private long                                    xi;
    private BijectiveMap<String, BoolVar>           vmap;
    private final List<Diagnosis>                   diagnoses;

    public ReasonerSimple(String    benchmarkName,
                          String    domainName,
                          String    problemName,
                          File      domainFile,
                          File      problemFile,
                          File      agentsFile,
                          File      combinedPlanFile,
                          File      faultsFile,
                          File      trajectoryFile,
                          String    observability) {
        super(benchmarkName, domainName, problemName, domainFile, problemFile, agentsFile, combinedPlanFile, faultsFile, trajectoryFile, observability);
        this._REASONER_NAME = "simple";
        this.relevantGroundedPlanPredicates = this.computeRelevantGroundedPlanPredicates();
        this._LOCAL_INTERNAL_ACTIONS_NUMBERS   = "[" + this.countActions() + "]";
        this._LOCAL_INTERNAL_ACTIONS_MIN       = this.countActions();
        this._LOCAL_INTERNAL_ACTIONS_MAX       = this.countActions();
        this._LOCAL_EXTERNAL_ACTIONS_NUMBERS   = "[" + this.countActions() + "]";
        this._LOCAL_EXTERNAL_ACTIONS_MIN       = this.countActions();
        this._LOCAL_EXTERNAL_ACTIONS_MAX       = this.countActions();
        this._LOCAL_TOTAL_ACTIONS_NUMBERS   = "[" + this.countActions() + "]";
        this._LOCAL_TOTAL_ACTIONS_MIN       = this.countActions();
        this._LOCAL_TOTAL_ACTIONS_MAX       = this.countActions();
        this.diagnoses = new ArrayList<>();
    }

    /**
     * note, state predicates that are never used, are not relevant
     */
    private List<String> computeRelevantGroundedPlanPredicates() {
        // collect grounded predicates from the plan
        List<String> rgppreds = new ArrayList<>();
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                this.addPredicatesOfCondition(t, a, "pre", rgppreds);
                this.addPredicatesOfCondition(t, a, "eff", rgppreds);
            }
        }
        return rgppreds;
    }

    private int countActions() {
        int count = 0;
        for (List<String> planAction : this._COMBINED_PLAN_ACTIONS) {
            for (String a : planAction) {
                if (!a.equals("(nop)")) {
                    count += 1;
                }
            }
        }
        return count;
    }

    @Override
    public void diagnoseProblem() {
        // create the model
        this.model = new Model();

        // set the variable name count number
        this.xi = 1;

        // initialize the variable map
        this.vmap = new BijectiveMap<>();

        // model problem
//        print(java.time.LocalTime.now() + ": " + "modelling...");
        Instant start = Instant.now();
        this.modelProblem();
        Instant end = Instant.now();
        this._MODELLING_AGENT_NAME = String.join(",", this._AGENT_NAMES);
        this._MODELLING_PREDICATES_NUM = this.relevantGroundedPlanPredicates.size();
        this._MODELLING_ACTIONS_NUM = this.countActionsNumber(this._COMBINED_PLAN_ACTIONS);
        this._MODELLING_VARIABLES_NUM = this.model.getNbVars();
        this._MODELLING_CONSTRAINTS_NUM = this.model.getNbCstrs();
        this._MODELLING_RUNTIME = Duration.between(start, end).toMillis();

//        print(999);

        // solve problem
//        print(java.time.LocalTime.now() + ": " + "solving...");
        start = Instant.now();
        this.solveProblem();
        end = Instant.now();
        this._SOLVING_AGENT_NAME = String.join(",", this._AGENT_NAMES);
        this._SOLVING_PREDICATES_NUM = this.relevantGroundedPlanPredicates.size();
        this._SOLVING_ACTIONS_NUM = this.countActionsNumber(this._COMBINED_PLAN_ACTIONS);
        this._SOLVING_VARIABLES_NUM = this.model.getNbVars();
        this._SOLVING_CONSTRAINTS_NUM = this.model.getNbCstrs();
        this._SOLVING_DIAGNOSES_NUM = this.diagnoses.size();
        this._SOLVING_RUNTIME = Duration.between(start, end).toMillis();

        // combine diagnoses
        this._COMBINING_RUNTIME = 0;
        this._TOTAL_RUNTIME = this._SOLVING_RUNTIME;
        this._LOCAL_DIAGNOSES_NUMBERS = "[" + this.diagnoses.size() + "]";
        this._LOCAL_DIAGNOSES_MIN = this.diagnoses.size();
        this._LOCAL_DIAGNOSES_MAX = this.diagnoses.size();
        this._DIAGNOSES_NUM = this.diagnoses.size();
        print(java.time.LocalTime.now() + ": " + this._REASONER_NAME + " - success. Diagnoses num: " + this._DIAGNOSES_NUM + ", Time in MS: " + this._TOTAL_RUNTIME);

        // print diagnoses
//        this.printDiagnoses();
//        print(34);
    }

    private void modelProblem() {
        // initialize state variables
        this.initializeStateVariables();

        // initialize health variables
        this.initializeHealthVariables();

        // adding constraints
        // health states mutual exclusiveness
        this.constraintHealthStatesMutualExclusive();

        // transition of non-effected variables
        this.constraintTransitionNonEffected();

        // transition of variables in the effects of an action in a normal state
        this.constraintTransitionNormalState();

        // transition of variables in the effects of an action in a faulty state
        this.constraintTransitionFaultyState();

        // transition of variables in the effects of an action in a conflict state
        this.constraintTransitionConflictState();

        // observation
        this.constraintObservation();
    }

    private void initializeStateVariables() {
        // create csp boolean variables that represent the states
        for (int t = 0; t < this._PLAN_LENGTH+1; t++) {
            for (String predicate: this.relevantGroundedPlanPredicates) {
                this.vmap.put("S:" + t + ":" + predicate, this.model.boolVar("x" + this.xi++));
            }
        }
    }

    private void initializeHealthVariables() {
        for (int t = 0; t < this._COMBINED_PLAN_ACTIONS.size(); t++) {
            for (int a = 0; a < this._COMBINED_PLAN_ACTIONS.get(t).size(); a++) {
                if (!this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("(nop)")) {
                    this.vmap.put("H:" + t + ":" + a + ":h", this.model.boolVar("x" + this.xi++));
                    this.vmap.put("H:" + t + ":" + a + ":f", this.model.boolVar("x" + this.xi++));
                    this.vmap.put("H:" + t + ":" + a + ":c", this.model.boolVar("x" + this.xi++));
                }
            }
        }
    }

    private void constraintHealthStatesMutualExclusive() {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                BoolVar h = this.vmap.getValue("H:" + t + ":" + a + ":h");
                if (h != null) {
                    BoolVar f = this.vmap.getValue("H:" + t + ":" + a + ":f");
                    BoolVar c = this.vmap.getValue("H:" + t + ":" + a + ":c");
                    BoolVar[] vars = {h,f,c};
                    this.model.sum(vars, "=", 1).post();
                }
            }
        }
    }

    private void constraintTransitionNonEffected() {
        for (int t = 1; t < this._TRAJECTORY.size(); t++) {
            for (String p: this.relevantGroundedPlanPredicates) {
                if (this.nonEffectedPredicate(p, t-1)) {
                    BoolVar v = this.vmap.getValue("S:" + t + ":" + p);
                    BoolVar v_prev = this.vmap.getValue("S:" + (t-1) + ":" + p);
                    this.model.ifOnlyIf(this.model.and(v), this.model.and(v_prev));
                }
            }
        }
    }

    private boolean nonEffectedPredicate(String predicate, int jointActionTime) {
        for (Map<String, String> m : this._COMBINED_PLAN_CONDITIONS.get(jointActionTime)) {
            if (m.get("eff").contains(predicate)) {
                return false;
            }
        }
        return true;
    }

    private void constraintTransitionNormalState() {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (!this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("(nop)")) {
                    BoolVar n = this.vmap.getValue("H:" + t + ":" + a + ":h");
                    BoolVar[] n_pre = addValidPre(t, a, n);
                    Constraint normalAndPreValid = this.model.and(n_pre);

                    String[] eff = this._COMBINED_PLAN_CONDITIONS.get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
                    int nextVarT = t+1;
                    Constraint[] effConstraints = Arrays.stream(eff).map(s -> s.contains("(not ") ? this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(5, s.length()-1)))) : this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s))).toArray(Constraint[]::new);

                    Constraint effOccur = this.model.and(effConstraints);
                    this.model.ifThen(normalAndPreValid, effOccur);
                }
            }
        }
    }

    private void constraintTransitionFaultyState() {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (!this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("(nop)")) {
                    BoolVar f = this.vmap.getValue("H:" + t + ":" + a + ":f");
                    BoolVar[] f_pre = addValidPre(t, a, f);
                    Constraint faultyAndPreValid = this.model.and(f_pre);

                    Constraint effNotOccur = effNotOccurCons(t, a);

                    this.model.ifThen(faultyAndPreValid, effNotOccur);
                }
            }
        }
    }

    private void constraintTransitionConflictState() {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (!this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("(nop)")) {
                    Constraint c = this.model.and(this.vmap.getValue("H:" + t + ":" + a + ":c"));
                    String[] pre = this._COMBINED_PLAN_CONDITIONS.get(t).get(a).get("pre").replaceAll("\\(", "S:" + t + ":(").split("(?=\\() |(?<=\\)) ");
                    Constraint[] all_pre = Stream.concat(Stream.of(this.model.trueConstraint()), Arrays.stream(Arrays.stream(pre).map(this.vmap::getValue).map(this.model::and).toArray(Constraint[]::new))).toArray(Constraint[]::new);
                    Constraint notAllPreValid = this.model.not(this.model.and(all_pre));
                    this.model.ifOnlyIf(c, notAllPreValid);

                    Constraint effNotOccur = effNotOccurCons(t, a);

                    this.model.ifThen(c, effNotOccur);
                }
            }
        }
    }

    private Constraint effNotOccurCons(int t, int a) {
        String[] eff = this._COMBINED_PLAN_CONDITIONS.get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
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

    private BoolVar[] addValidPre(int t, int a, BoolVar v) {
        String[] pre = this._COMBINED_PLAN_CONDITIONS.get(t).get(a).get("pre").replaceAll("\\(", "S:" + t + ":(").split("(?=\\() |(?<=\\)) ");
        return Stream.concat(Stream.of(v), Arrays.stream(Arrays.stream(pre).map(this.vmap::getValue).toArray(BoolVar[]::new))).toArray(BoolVar[]::new);
    }

    private void constraintObservation() {
        for (Integer t : this._OBSERVABLE_STATES) {
            for (String gp: this.relevantGroundedPlanPredicates) {
                if (this._TRAJECTORY.get(t).contains(gp)) {
                    this.model.and(this.vmap.getValue("S:" + t + ":" + gp)).post();
                } else {
                    this.model.not(this.model.and(this.vmap.getValue("S:" + t + ":" + gp))).post();
                }
            }
        }
    }

    private void solveProblem() {
        Solver solver = this.model.getSolver();
        Solution s = solver.findSolution();
        while (s != null) {
            Diagnosis d = new Diagnosis(s, this.vmap, this._PLAN_LENGTH, this._AGENTS_NUM);
            if (!this.containsDiagnosis(this.diagnoses, d)) {
                this.diagnoses.add(d);
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

    private void printDiagnoses() {
        for (int d = 0; d < this.diagnoses.size(); d++) {
            print("Diagnosis #" + (d+1) + ":\n" + this.diagnoses.get(d));
        }
    }


}

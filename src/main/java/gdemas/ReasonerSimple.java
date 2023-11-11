package gdemas;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gdemas.Utils.*;

public class ReasonerSimple extends Reasoner {

    private final List<List<Map<String, String>>>   combinedPlanConditions;
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
        this.combinedPlanConditions = this.computeCombinedPlanConditions();
        this.relevantGroundedPlanPredicates = this.computeRelevantGroundedPlanPredicates();
        this.diagnoses = new ArrayList<>();
    }

    private List<List<Map<String, String>>> computeCombinedPlanConditions() {
        List<List<Map<String, String>>> cpc = new ArrayList<>();
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            List<Map<String, String>> tcpc = new ArrayList<>();
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                Map<String, String> atcpc = new HashMap<>();
                atcpc.put("pre", extractActionGroundedConditions(t, a, "preconditions"));
                atcpc.put("eff", extractActionGroundedConditions(t, a, "effects"));
                tcpc.add(atcpc);
            }
            cpc.add(tcpc);
        }
        return cpc;
    }

    private String extractActionGroundedConditions(int t, int a, String conditionsType) {
        if (this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("nop")) {
            return "";
        } else {
            String groundedAction = this._COMBINED_PLAN_ACTIONS.get(t).get(a);
            String[] actionSignature = groundedAction.split(" ");
            String actionName = actionSignature[0];
            String[] actionArguments = Arrays.copyOfRange(actionSignature, 1, actionSignature.length);
            String conditions = this._DOMAIN.actions.get(actionName).get(conditionsType);
            String[] actionParams = this._DOMAIN.actions.get(actionName).get("parameters").replaceAll("\\s+-\\s+\\S+", "").split(" ");
            for (int i = 0; i < actionArguments.length; i++) {
                conditions = conditions.replaceAll("\\?" + actionParams[i].substring(1), actionArguments[i]);
            }
            return conditions;
        }
    }

    /**
     * note, state predicates that are never used, are not relevant
     */
    private List<String> computeRelevantGroundedPlanPredicates() {
        // collect grounded predicates from the plan
        List<String> rgppreds = new ArrayList<>();
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                addPredicatesOfCondition(t, a, "pre", rgppreds);
                addPredicatesOfCondition(t, a, "eff", rgppreds);
            }
        }
        return rgppreds;
    }

    private void addPredicatesOfCondition(int t, int a, String conditionType, List<String> rgppreds) {
        String cnd = this.combinedPlanConditions.get(t).get(a).get(conditionType);
        if (!cnd.isEmpty()) {
            List<String> cndPredicates = Arrays.asList(cnd.split("(?=\\() |(?<=\\)) "));
            for (int i = 0; i < cndPredicates.size(); i++) {
                if (cndPredicates.get(i).contains("(not ")) {
                    cndPredicates.set(i, cndPredicates.get(i).substring(6, cndPredicates.get(i).length()-2));
                } else {
                    cndPredicates.set(i, cndPredicates.get(i).substring(1, cndPredicates.get(i).length()-1));
                }
            }
            rgppreds.addAll(cndPredicates.stream().filter(s -> !rgppreds.contains(s)).collect(Collectors.toList()));
        }
    }

    @Override
    public void diagnoseProblem() {
        this.modelProblem();
        this.solveProblem();
        print(9);
    }

    private void modelProblem() {
        // create the model
        this.model = new Model();

        // set the variable name count number
        this.xi = 1;

        // initialize the variable map
        this.vmap = new BijectiveMap<>();

        // initialize state variables
        this.initializeStateVariables();

        // initialize health variables
        this.initializeHealthVariables();

        // adding constraints
        // health states mutual exclusiveness
        this.constraintHealthStatesMutualExclusive();
        print(9);

        // transition of non-effected variables
        this.constraintTransitionNonEffected();
        print(9);

        // transition of variables in the effects of an action in a normal state
        this.constraintTransitionNormalState();
        print(9);

        // transition of variables in the effects of an action in a faulty state
        this.constraintTransitionFaultyState();
        print(9);

        // transition of variables in the effects of an action in a conflict state
        this.constraintTransitionConflictState();
        print(9);

        // observation
        this.constraintObservation();
        print(999);
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
                if (!this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("nop")) {
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
        for (Map<String, String> m : this.combinedPlanConditions.get(jointActionTime)) {
            if (m.get("eff").contains(predicate)) {
                return false;
            }
        }
        return true;
    }

    private void constraintTransitionNormalState() {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (!this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("nop")) {
                    BoolVar n = this.vmap.getValue("H:" + t + ":" + a + ":h");
                    BoolVar[] n_pre = addValidPre(t, a, n);
                    Constraint normalAndPreValid = this.model.and(n_pre);

                    String[] eff = this.combinedPlanConditions.get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
                    int nextVarT = t+1;
                    Constraint[] effConstraints = Arrays.stream(eff).map(s -> s.contains("not") ? this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(6, s.length()-2)))) : this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(1, s.length()-1)))).toArray(Constraint[]::new);

                    Constraint effOccur = this.model.and(effConstraints);
                    this.model.ifThen(normalAndPreValid, effOccur);
                }
            }
        }
    }

    private void constraintTransitionFaultyState() {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (!this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("nop")) {
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
                if (!this._COMBINED_PLAN_ACTIONS.get(t).get(a).equals("nop")) {
                    Constraint c = this.model.and(this.vmap.getValue("H:" + t + ":" + a + ":c"));
                    String[] pre = this.combinedPlanConditions.get(t).get(a).get("pre").replaceAll("\\(", "S:" + t + ":").replaceAll("\\)$", "").split("\\) ");
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
        String[] eff = this.combinedPlanConditions.get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
        int nextVarT = t + 1;
        Constraint[] effConstraints = Arrays.stream(eff).map(s -> s.contains("not") ?
                this.model.and(
                        this.model.or(this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(6, s.length()-2)))), this.model.and(this.vmap.getValue("S:" + (nextVarT-1) + ":" + s.substring(6, s.length()-2)))),
                        this.model.or(this.model.not(this.model.and(this.vmap.getValue("S:" + (nextVarT-1) + ":" + s.substring(6, s.length()-2)))), this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(6, s.length()-2))))
                ) :
                this.model.and(
                        this.model.or(this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(1, s.length()-1)))), this.model.and(this.vmap.getValue("S:" + (nextVarT-1) + ":" + s.substring(1, s.length()-1)))),
                        this.model.or(this.model.not(this.model.and(this.vmap.getValue("S:" + (nextVarT-1) + ":" + s.substring(1, s.length()-1)))), this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(1, s.length()-1))))
                )
        ).toArray(Constraint[]::new);
        return this.model.and(effConstraints);
    }

    private BoolVar[] addValidPre(int t, int a, BoolVar v) {
        String[] pre = this.combinedPlanConditions.get(t).get(a).get("pre").replaceAll("\\(", "S:" + t + ":").replaceAll("\\)$", "").split("\\) ");
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
            this.diagnoses.add(d);
            print("Diagnosis #" + this.diagnoses.size() + ":\n" + d);
            s = solver.findSolution();
        }
    }


}

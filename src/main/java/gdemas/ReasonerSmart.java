package gdemas;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    public ReasonerSmart(String     benchmarkName,
                         String     domainName,
                         String     problemName,
                         File       domainFile,
                         File       problemFile,
                         File       agentsFile,
                         File       combinedPlanFile,
                         File       faultsFile,
                         File       trajectoryFile,
                         String     observability) {
        super(benchmarkName, domainName, problemName, domainFile, problemFile, agentsFile, combinedPlanFile, faultsFile, trajectoryFile, observability);
        this.agentsPredicates = this.computeAgentsPredicates();
        this.agentsPlanActions = this.computeAgentsPlanActions();
        this.agentsPlanConditions = this.computeAgentsPlanConditions();
        this.agentsDiagnoses = Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
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
                    if (aStepAction.equals("nop")) {
                        stepPlanActions.add("nop");
                    } else if (this.actionIsRelevantToAgentA(A, t, a)) {
                        stepPlanActions.add(aStepAction);
                    } else {
                        stepPlanActions.add("nop");
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
            List<List<Map<String, String>>> planConditions = this.computePlanConditions(this.agentsPlanActions.get(a));
            agentsPlanConditions.add(planConditions);
        }
        return agentsPlanConditions;
    }

    @Override
    public void diagnoseProblem() {
        for (int A = 0; A < this._AGENTS_NUM; A++) {
            this.modelProblem(A);
            this.solveProblem(A);
        }
        print(9);
    }

    private void modelProblem(int A) {
        // create the model
        this.model = new Model();

        // set the variable name count number
        this.xi = 1;

        // initialize the variable map
        this.vmap = new BijectiveMap<>();

        // initialize state variables
        this.initializeStateVariables(A);

        // initialize health variables
        this.initializeHealthVariables(A);

        // adding constraints
        // health states mutual exclusiveness
        this.constraintHealthStatesMutualExclusive(A);
        print(9);

        // transition of non-effected variables
        this.constraintTransitionNonEffected(A);
        print(9);

        // transition of variables in the effects of an action in a normal state
        this.constraintTransitionNormalState(A);
        print(9);

        // transition of variables in the effects of an action in a faulty state
        this.constraintTransitionFaultyState(A);
        print(9);

        // transition of variables in the effects of an action in a conflict state
        this.constraintTransitionConflictState(A);
        print(9);

        // transition of variables in the effects of an action in an innocent state
        this.constraintTransitionInnocentState(A);
        print(9);

        // transition of variables in the effects of an action in a guilty state
        this.constraintTransitionGuiltyState(A);
        print(9);

        // observation
        this.constraintObservation(A);
        print(999);
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
                if (!this.agentsPlanActions.get(A).get(t).get(a).equals("nop")) {
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
            if (!this.agentsPlanActions.get(A).get(t).get(A).equals("nop")) {
                BoolVar n = this.vmap.getValue("H:" + t + ":" + A + ":h");
                BoolVar[] n_pre = addValidPre(t, A, n);
                Constraint normalAndPreValid = this.model.and(n_pre);

                String[] eff = this.agentsPlanConditions.get(A).get(t).get(A).get("eff").split("(?=\\() |(?<=\\)) ");
                int nextVarT = t+1;
                Constraint[] effConstraints = Arrays.stream(eff).map(s -> s.contains("not") ? this.model.not(this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(6, s.length()-2)))) : this.model.and(this.vmap.getValue("S:" + nextVarT + ":" + s.substring(1, s.length()-1)))).toArray(Constraint[]::new);

                Constraint effOccur = this.model.and(effConstraints);
                this.model.ifThen(normalAndPreValid, effOccur);
            }
        }
    }

    private void constraintTransitionFaultyState(int A) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            if (!this.agentsPlanActions.get(A).get(t).get(A).equals("nop")) {
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
            if (!this.agentsPlanActions.get(A).get(t).get(A).equals("nop")) {
                Constraint c = this.model.and(this.vmap.getValue("H:" + t + ":" + A + ":c"));
                String[] pre = this.agentsPlanConditions.get(A).get(t).get(A).get("pre").replaceAll("\\(", "S:" + t + ":").replaceAll("\\)$", "").split("\\) ");
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

    private BoolVar[] addValidPre(int t, int A, BoolVar v) {
        String[] pre = this.agentsPlanConditions.get(A).get(t).get(A).get("pre").replaceAll("\\(", "S:" + t + ":").replaceAll("\\)$", "").split("\\) ");
        return Stream.concat(Stream.of(v), Arrays.stream(Arrays.stream(pre).map(this.vmap::getValue).toArray(BoolVar[]::new))).toArray(BoolVar[]::new);
    }

    private void constraintTransitionInnocentState(int A) {
        for (int t = 0; t < this._PLAN_LENGTH; t++) {
            for (int a = 0; a < this._AGENTS_NUM; a++) {
                if (a != A) {
                    if (!this.agentsPlanActions.get(A).get(t).get(a).equals("nop")) {
                        Constraint i = this.model.and(this.vmap.getValue("H:" + t + ":" + a + ":i"));

                        String[] eff = this.agentsPlanConditions.get(A).get(t).get(a).get("eff").split("(?=\\() |(?<=\\)) ");
                        List<String> relevantEff = computeRelevantEffects(A, eff);
                        Constraint[] effOccur = new Constraint[relevantEff.size()+1];
                        effOccur[0] = this.model.trueConstraint();
                        for (int j = 0; j < relevantEff.size(); j++) {
                            String s = relevantEff.get(j);
                            if (s.contains("(not ")) {
                                effOccur[j+1] = this.model.not(this.model.and(this.vmap.getValue("S:" + (t+1) + ":" + s.substring(6, s.length()-2))));
                            } else {
                                effOccur[j+1] = this.model.and(this.vmap.getValue("S:" + (t+1) + ":" + s.substring(1, s.length()-1)));
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
                    if (!this.agentsPlanActions.get(A).get(t).get(a).equals("nop")) {
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
                                substring = s.substring(6, s.length() - 2);
                            } else {
                                substring = s.substring(1, s.length() - 1);
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
                e = e.substring(6, e.length() - 2);
            } else {
                e = e.substring(1, e.length() - 1);
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
            this.agentsDiagnoses.get(A).add(d);
            print("Agent " + A + " diagnosis #" + this.agentsDiagnoses.get(A).size() + ":\n" + d);
            s = solver.findSolution();
        }
    }
}

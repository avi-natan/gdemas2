package gdemas;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gdemas.Utils.print;

public class ReasonerAmazing extends Reasoner {

    private final List<List<String>>                    agentsPredicates;
    private final List<List<List<String>>>              agentsPlanActions;
    private final List<List<List<Map<String, String>>>> agentsPlanConditions;
    private Model                                       model;
    private long                                        xi;
    private BijectiveMap<String, BoolVar>               vmap;
    private final List<List<Diagnosis>>                 agentsDiagnoses;
    private List<GlobalDiagnosis>                       globalDiagnoses;

    public ReasonerAmazing(String   benchmarkName,
                           String   domainName,
                           String   problemName,
                           File     domainFile,
                           File     problemFile,
                           File     agentsFile,
                           File     combinedPlanFile,
                           File     faultsFile,
                           File     trajectoryFile,
                           String   observability) {
        super(benchmarkName, domainName, problemName, domainFile, problemFile, agentsFile, combinedPlanFile, faultsFile, trajectoryFile, observability);
        this._REASONER_NAME = "amazing";
        this.agentsPredicates = this.computeAgentsPredicates();
        this.agentsPlanActions = this.computeAgentsPlanActions();
        this.agentsPlanConditions = this.computeAgentsPlanConditions();
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
            List<List<Map<String, String>>> planConditions = Parser.computePlanConditions(this.agentsPlanActions.get(a), this._AGENT_NAMES.size(), this._DOMAIN);
            agentsPlanConditions.add(planConditions);
        }
        return agentsPlanConditions;
    }

    @Override
    public void diagnoseProblem() {
        print(9);
    }
}

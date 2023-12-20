package gdemas;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Reasoner {
    // Operational members
    public Domain                           _DOMAIN;
    public Problem                          _PROBLEM;
    public List<String>                     _AGENT_NAMES;
    public List<List<String>>               _COMBINED_PLAN_ACTIONS;
    public List<List<Map<String, String>>>  _COMBINED_PLAN_CONDITIONS;
    public List<String>                     _FAULTS;
    public List<List<String>>               _TRAJECTORY;
    public List<Integer>                    _OBSERVABLE_STATES;
    public long                             _TIMEOUT;

    // output members
    // controlled parameters
    public String                           _BENCHMARK_NAME;
    public String                           _DOMAIN_NAME;
    public String                           _PROBLEM_NAME;
    public int                              _FAULTS_NUM;
    public int                              _REPETITION_NUM;
    public String                           _OBSERVABILITY;
    public String                           _REASONER_NAME;

    // instance non-controlled parameters
    public int                              _AGENTS_NUM;
    public int                              _GOAL_SIZE;
    public double                           _GOAL_AGENT_RATIO;
    public int                              _PLAN_LENGTH;
    public double                           _COEFFICIENT_OF_VARIATION;

    // measurement members
    public String                           _MODELLING_AGENT_NAME;
    public int                              _MODELLING_PREDICATES_NUM;
    public int                              _MODELLING_ACTIONS_NUM;
    public int                              _MODELLING_VARIABLES_NUM;
    public int                              _MODELLING_CONSTRAINTS_NUM;
    public long                             _MODELLING_RUNTIME;
    public String                           _SOLVING_AGENT_NAME;
    public int                              _SOLVING_PREDICATES_NUM;
    public int                              _SOLVING_ACTIONS_NUM;
    public int                              _SOLVING_VARIABLES_NUM;
    public int                              _SOLVING_CONSTRAINTS_NUM;
    public int                              _SOLVING_DIAGNOSES_NUM;
    public long                             _SOLVING_RUNTIME;
    public long                             _COMBINING_RUNTIME;
    public long                             _TOTAL_RUNTIME;
    public int                              _TIMEDOUT;
    public String                           _LOCAL_INTERNAL_ACTIONS_NUMBERS;
    public int                              _LOCAL_INTERNAL_ACTIONS_MIN;
    public int                              _LOCAL_INTERNAL_ACTIONS_MAX;
    public String                           _LOCAL_EXTERNAL_ACTIONS_NUMBERS;
    public int                              _LOCAL_EXTERNAL_ACTIONS_MIN;
    public int                              _LOCAL_EXTERNAL_ACTIONS_MAX;
    public String                           _LOCAL_TOTAL_ACTIONS_NUMBERS;
    public int                              _LOCAL_TOTAL_ACTIONS_MIN;
    public int                              _LOCAL_TOTAL_ACTIONS_MAX;
    public String                           _LOCAL_DIAGNOSES_NUMBERS;
    public int                              _LOCAL_DIAGNOSES_MIN;
    public int                              _LOCAL_DIAGNOSES_MAX;
    public int                              _DIAGNOSES_NUM;

    public Reasoner(String  benchmarkName,
                    String  domainName,
                    String  problemName,
                    File    domainFile,
                    File    problemFile,
                    File    agentsFile,
                    File    combinedPlanFile,
                    File    faultsFile,
                    File    trajectoryFile,
                    String  observability,
                    long    timeout) {
        // Operational members
        this._DOMAIN                    = Parser.parseDomain(domainFile);
        this._PROBLEM                   = Parser.parseProblem(problemFile);
        this._AGENT_NAMES               = Parser.parseAgentNames(agentsFile);
        this._COMBINED_PLAN_ACTIONS     = Parser.parseCombinedPlan(combinedPlanFile);
        this._COMBINED_PLAN_CONDITIONS  = Parser.computePlanConditions(this._COMBINED_PLAN_ACTIONS, this._AGENT_NAMES.size(), this._DOMAIN);
        this._FAULTS                    = Parser.parseFaults(faultsFile);
        this._TRAJECTORY                = Parser.parseTrajectory(trajectoryFile);
        this._OBSERVABLE_STATES         = this.computeObservableStates(observability, this._TRAJECTORY.size());
        this._TIMEOUT                   = timeout;

        // output members
        // controlled parameters
        this._BENCHMARK_NAME            = benchmarkName;
        this._DOMAIN_NAME               = domainName;
        this._PROBLEM_NAME              = problemName;
        this._FAULTS_NUM                = this._FAULTS.size();
        this._REPETITION_NUM            = Parser.parseRepetitionNum(faultsFile);
        this._OBSERVABILITY             = observability;
        this._REASONER_NAME             = "";

        // instance non-controlled parameters
        this._AGENTS_NUM                = this._AGENT_NAMES.size();
        this._GOAL_SIZE                 = this._PROBLEM.goal.size();
        this._GOAL_AGENT_RATIO          = this._GOAL_SIZE * 1.0 / this._AGENTS_NUM;
        this._PLAN_LENGTH               = this._COMBINED_PLAN_ACTIONS.size();
        this._COEFFICIENT_OF_VARIATION  = Parser.calculateCoefficientOfVariation(this._AGENTS_NUM, this._COMBINED_PLAN_ACTIONS);

        // measurement members
        this._MODELLING_AGENT_NAME      = "";
        this._MODELLING_PREDICATES_NUM  = 0;
        this._MODELLING_ACTIONS_NUM     = 0;
        this._MODELLING_VARIABLES_NUM   = 0;
        this._MODELLING_CONSTRAINTS_NUM = 0;
        this._MODELLING_RUNTIME         = 0;
        this._SOLVING_AGENT_NAME        = "";
        this._SOLVING_PREDICATES_NUM    = 0;
        this._SOLVING_ACTIONS_NUM       = 0;
        this._SOLVING_VARIABLES_NUM     = 0;
        this._SOLVING_CONSTRAINTS_NUM   = 0;
        this._SOLVING_DIAGNOSES_NUM     = 0;
        this._SOLVING_RUNTIME           = 0;
        this._COMBINING_RUNTIME         = 0;
        this._TOTAL_RUNTIME             = 0;
        this._TIMEDOUT                  = -1;
        this._LOCAL_INTERNAL_ACTIONS_NUMBERS   = "";
        this._LOCAL_INTERNAL_ACTIONS_MIN       = 0;
        this._LOCAL_INTERNAL_ACTIONS_MAX       = 0;
        this._LOCAL_EXTERNAL_ACTIONS_NUMBERS   = "";
        this._LOCAL_EXTERNAL_ACTIONS_MIN       = 0;
        this._LOCAL_EXTERNAL_ACTIONS_MAX       = 0;
        this._LOCAL_TOTAL_ACTIONS_NUMBERS   = "";
        this._LOCAL_TOTAL_ACTIONS_MIN       = 0;
        this._LOCAL_TOTAL_ACTIONS_MAX       = 0;
        this._LOCAL_DIAGNOSES_NUMBERS   = "";
        this._LOCAL_DIAGNOSES_MIN       = 0;
        this._LOCAL_DIAGNOSES_MAX       = 0;
        this._DIAGNOSES_NUM             = 0;
    }

    public abstract void diagnoseProblem();

    protected int countActionsNumber(List<List<String>> planActions) {
        int count = 0;
        for (List<String> planAction : planActions) {
            for (String s : planAction) {
                if (!s.equals("(nop)")) {
                    count += 1;
                }
            }
        }
        return count;
    }

    protected void addPredicatesOfCondition(int t, int a, String conditionType, List<String> predicatesList) {
        String cnd = this._COMBINED_PLAN_CONDITIONS.get(t).get(a).get(conditionType);
        if (!cnd.isEmpty()) {
            List<String> cndPredicates = Arrays.asList(cnd.split("(?=\\() |(?<=\\)) "));
            for (int i = 0; i < cndPredicates.size(); i++) {
                if (cndPredicates.get(i).contains("(not ")) {
                    cndPredicates.set(i, cndPredicates.get(i).substring(5, cndPredicates.get(i).length()-1));
                }
            }
            predicatesList.addAll(cndPredicates.stream().filter(s -> !predicatesList.contains(s)).collect(Collectors.toList()));
        }
    }
    private List<Integer> computeObservableStates(String observability, int statesNumber) {
        List<Integer> observableStates = new ArrayList<>();
        switch (observability) {
            case "1p":
                observableStates.add(0);
                observableStates.add(statesNumber-1);
                break;
            case "5p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 20 == 0) observableStates.add(i);
                }
                break;
            case "10p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 10 == 0) observableStates.add(i);
                }
                break;
            case "12p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 8 == 0) observableStates.add(i);
                }
                break;
            case "15p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 7 == 0) observableStates.add(i);
                }
                break;
            case "17p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 6 == 0) observableStates.add(i);
                }
                break;
            case "20p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 5 == 0) observableStates.add(i);
                }
                break;
            case "25p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 4 == 0) observableStates.add(i);
                }
                break;
            case "50p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 2 == 0) observableStates.add(i);
                }
                break;
            case "75p":
                for (int i = 0; i < statesNumber; i++) {
                    if (i % 4 != 0) observableStates.add(i);
                }
                break;
            case "99p":
                for (int i = 0; i < statesNumber; i++) {
                    observableStates.add(i);
                }
                break;
            default:
                throw new RuntimeException("percentage not handled");
        }

        if (!observableStates.contains(0)) {
            observableStates.add(0, 0);
        }
        if (!observableStates.contains(statesNumber-1)) {
            observableStates.add(statesNumber-1);
        }
        return observableStates;
    }


    // output members
    // controlled parameters
    public String getBenchmarkName() {
        return this._BENCHMARK_NAME;
    }
    public String getDomainName() {
        return this._DOMAIN_NAME;
    }
    public String getProblemName() {
        return this._PROBLEM_NAME;
    }
    public int getFaultsNum() {
        return this._FAULTS_NUM;
    }
    public int getRepetitionNum() {
        return this._REPETITION_NUM;
    }
    public String getObservability() {
        return this._OBSERVABILITY;
    }
    public String getReasonerName() {
        return this._REASONER_NAME;
    }

    // instance non-controlled parameters
    public int getAgentsNum() {
        return this._AGENTS_NUM;
    }
    public int getGoalSize() {
        return this._GOAL_SIZE;
    }
    public double getGoalAgentsRatio() {
        return this._GOAL_AGENT_RATIO;
    }
    public int getPlanLength() {
        return this._PLAN_LENGTH;
    }
    public double getCoefficientOfVariation() {
        return _COEFFICIENT_OF_VARIATION;
    }

    // measurement members
    public String getModellingAgentName() {
        return _MODELLING_AGENT_NAME;
    }
    public int getModellingPredicatesNum() {
        return _MODELLING_PREDICATES_NUM;
    }
    public int getModellingActionsNum() {
        return _MODELLING_ACTIONS_NUM;
    }
    public int getModellingVariablesNum() {
        return _MODELLING_VARIABLES_NUM;
    }
    public int getModellingConstraintsNum() {
        return _MODELLING_CONSTRAINTS_NUM;
    }
    public long getModellingRuntime() {
        return _MODELLING_RUNTIME;
    }
    public String getSolvingAgentName() {
        return _SOLVING_AGENT_NAME;
    }
    public int getSolvingPredicatesNum() {
        return _SOLVING_PREDICATES_NUM;
    }
    public int getSolvingActionsNum() {
        return _SOLVING_ACTIONS_NUM;
    }
    public int getSolvingVariablesNum() {
        return _SOLVING_VARIABLES_NUM;
    }
    public int getSolvingConstraintsNum() {
        return _SOLVING_CONSTRAINTS_NUM;
    }
    public int getSolvingDiagnosesNum() {
        return this._SOLVING_DIAGNOSES_NUM;
    }
    public long getSolvingRuntime() {
        return _SOLVING_RUNTIME;
    }
    public long getCombiningRuntime() {
        return _COMBINING_RUNTIME;
    }
    public long getTotalRuntime() {
        return _TOTAL_RUNTIME;
    }
    public int getTimedOut() {
        return _TIMEDOUT;
    }
    public String getLocalInternalActionsNumbers() {
        return _LOCAL_INTERNAL_ACTIONS_NUMBERS;
    }
    public int getLocalInternalActionsMin() {
        return _LOCAL_INTERNAL_ACTIONS_MIN;
    }
    public int getLocalInternalActionsMax() {
        return _LOCAL_INTERNAL_ACTIONS_MAX;
    }
    public String getLocalExternalActionsNumbers() {
        return _LOCAL_EXTERNAL_ACTIONS_NUMBERS;
    }
    public int getLocalExternalActionsMin() {
        return _LOCAL_EXTERNAL_ACTIONS_MIN;
    }
    public int getLocalExternalActionsMax() {
        return _LOCAL_EXTERNAL_ACTIONS_MAX;
    }
    public String getLocalTotalActionsNumbers() {
        return _LOCAL_TOTAL_ACTIONS_NUMBERS;
    }
    public int getLocalTotalActionsMin() {
        return _LOCAL_TOTAL_ACTIONS_MIN;
    }
    public int getLocalTotalActionsMax() {
        return _LOCAL_TOTAL_ACTIONS_MAX;
    }
    public String getLocalDiagnosesNumbers () {
        return this._LOCAL_DIAGNOSES_NUMBERS;
    }
    public int getLocalDiagnosesMin () {
        return this._LOCAL_DIAGNOSES_MIN;
    }
    public int getLocalDiagnosesMax () {
        return this._LOCAL_DIAGNOSES_MAX;
    }
    public int getDiagnosesNum() {
        return _DIAGNOSES_NUM;
    }
}

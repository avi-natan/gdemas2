package gdemas;

import org.apache.poi.ss.usermodel.Row;

public class Record {

    public static String[] headers = new String[] {
            "Benchmark",
            "Domain",
            "Problem",
            "# Faults number",
            "# Repetition index",
            "Observability",
            "Reasoner",

            "# Agents number",
            "# Goal size",
            "# Goal/Agent",
            "# Plan length",

            "M Agent name",
            "# M Predicates number",
            "# M Actions number",
            "# M Variables number",
            "# M Constraints number",
            "# M Runtime",
            "S Agent name",
            "# S Predicates number",
            "# S Actions number",
            "# S Variables number",
            "# S Constraints number",
            "# S Runtime",
            "# Combining runtime",
            "# Solv & Comb runtime",
            "# Diagnoses number"
    };

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
    public long                             _SOLVING_RUNTIME;
    public long                             _COMBINING_RUNTIME;
    public long                             _SOLV_AND_COMB_RUNTIME;
    public int                              _DIAGNOSES_NUM;

    public Record(Reasoner r) {
        // output members
        // controlled parameters
        this._BENCHMARK_NAME            = r.getBenchmarkName();
        this._DOMAIN_NAME               = r.getDomainName();
        this._PROBLEM_NAME              = r.getProblemName();
        this._FAULTS_NUM                = r.getFaultsNum();
        this._REPETITION_NUM            = r.getRepetitionNum();
        this._OBSERVABILITY             = r.getObservability();
        this._REASONER_NAME             = r.getReasonerName();

        // instance non-controlled parameters
        this._AGENTS_NUM                = r.getAgentsNum();
        this._GOAL_SIZE                 = r.getGoalSize();
        this._GOAL_AGENT_RATIO          = r.getGoalAgentsRatio();
        this._PLAN_LENGTH               = r.getPlanLength();

        // measurement members
        this._MODELLING_AGENT_NAME      = r.getModellingAgentName();
        this._MODELLING_PREDICATES_NUM  = r.getModellingPredicatesNum();
        this._MODELLING_ACTIONS_NUM     = r.getModellingActionsNum();
        this._MODELLING_VARIABLES_NUM   = r.getModellingVariablesNum();
        this._MODELLING_CONSTRAINTS_NUM = r.getModellingConstraintsNum();
        this._MODELLING_RUNTIME         = r.getModellingRuntime();
        this._SOLVING_AGENT_NAME        = r.getSolvingAgentName();
        this._SOLVING_PREDICATES_NUM    = r.getSolvingPredicatesNum();
        this._SOLVING_ACTIONS_NUM       = r.getSolvingActionsNum();
        this._SOLVING_VARIABLES_NUM     = r.getSolvingVariablesNum();
        this._SOLVING_CONSTRAINTS_NUM   = r.getSolvingConstraintsNum();
        this._SOLVING_RUNTIME           = r.getSolvingRuntime();
        this._COMBINING_RUNTIME         = r.getCombiningRuntime();
        this._SOLV_AND_COMB_RUNTIME     = r.getSolvAndCombRuntime();
        this._DIAGNOSES_NUM             = r.getDiagnosesNum();
    }

    public void recordToRow(Row row) {
        int c = 0;
        row.createCell(c++).setCellValue(this._BENCHMARK_NAME);
        row.createCell(c++).setCellValue(this._DOMAIN_NAME);
        row.createCell(c++).setCellValue(this._PROBLEM_NAME);
        row.createCell(c++).setCellValue(this._FAULTS_NUM);
        row.createCell(c++).setCellValue(this._REPETITION_NUM);
        row.createCell(c++).setCellValue(this._OBSERVABILITY);
        row.createCell(c++).setCellValue(this._REASONER_NAME);

        // instance non-controlled parameters
        row.createCell(c++).setCellValue(this._AGENTS_NUM);
        row.createCell(c++).setCellValue(this._GOAL_SIZE);
        row.createCell(c++).setCellValue(this._GOAL_AGENT_RATIO);
        row.createCell(c++).setCellValue(this._PLAN_LENGTH);

        // measurement members
        row.createCell(c++).setCellValue(this._MODELLING_AGENT_NAME);
        row.createCell(c++).setCellValue(this._MODELLING_PREDICATES_NUM);
        row.createCell(c++).setCellValue(this._MODELLING_ACTIONS_NUM);
        row.createCell(c++).setCellValue(this._MODELLING_VARIABLES_NUM);
        row.createCell(c++).setCellValue(this._MODELLING_CONSTRAINTS_NUM);
        row.createCell(c++).setCellValue(this._MODELLING_RUNTIME);
        row.createCell(c++).setCellValue(this._SOLVING_AGENT_NAME);
        row.createCell(c++).setCellValue(this._SOLVING_PREDICATES_NUM);
        row.createCell(c++).setCellValue(this._SOLVING_ACTIONS_NUM);
        row.createCell(c++).setCellValue(this._SOLVING_VARIABLES_NUM);
        row.createCell(c++).setCellValue(this._SOLVING_CONSTRAINTS_NUM);
        row.createCell(c++).setCellValue(this._SOLVING_RUNTIME);
        row.createCell(c++).setCellValue(this._COMBINING_RUNTIME);
        row.createCell(c++).setCellValue(this._SOLV_AND_COMB_RUNTIME);
        row.createCell(c).setCellValue(this._DIAGNOSES_NUM);
    }
}

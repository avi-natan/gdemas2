package gdemas;

import org.apache.poi.ss.usermodel.Row;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
            "# Coefficient of variation",

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
            "# S Diagnoses number",
            "# S Runtime",
            "# Combining runtime",
            "# Total runtime",
            "# TimedOut",
            "Local internal actions numbers",
            "# Local internal actions min",
            "# Local internal actions max",
            "Local external actions numbers",
            "# Local external actions min",
            "# Local external actions max",
            "Local total actions numbers",
            "# Local total actions min",
            "# Local total actions max",
            "Local diagnoses numbers",
            "# Local diagnoses min",
            "# Local diagnoses max",
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
        this._COEFFICIENT_OF_VARIATION  = r.getCoefficientOfVariation();

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
        this._SOLVING_DIAGNOSES_NUM     = r.getSolvingDiagnosesNum();
        this._SOLVING_RUNTIME           = r.getSolvingRuntime();
        this._COMBINING_RUNTIME         = r.getCombiningRuntime();
        this._TOTAL_RUNTIME             = r.getTotalRuntime();
        this._TIMEDOUT                  = r.getTimedOut();
        this._LOCAL_INTERNAL_ACTIONS_NUMBERS   = r.getLocalInternalActionsNumbers();
        this._LOCAL_INTERNAL_ACTIONS_MIN       = r.getLocalInternalActionsMin();
        this._LOCAL_INTERNAL_ACTIONS_MAX       = r.getLocalInternalActionsMax();
        this._LOCAL_EXTERNAL_ACTIONS_NUMBERS   = r.getLocalExternalActionsNumbers();
        this._LOCAL_EXTERNAL_ACTIONS_MIN       = r.getLocalExternalActionsMin();
        this._LOCAL_EXTERNAL_ACTIONS_MAX       = r.getLocalExternalActionsMax();
        this._LOCAL_TOTAL_ACTIONS_NUMBERS   = r.getLocalTotalActionsNumbers();
        this._LOCAL_TOTAL_ACTIONS_MIN       = r.getLocalTotalActionsMin();
        this._LOCAL_TOTAL_ACTIONS_MAX       = r.getLocalTotalActionsMax();
        this._LOCAL_DIAGNOSES_NUMBERS   = r.getLocalDiagnosesNumbers();
        this._LOCAL_DIAGNOSES_MIN       = r.getLocalDiagnosesMin();
        this._LOCAL_DIAGNOSES_MAX       = r.getLocalDiagnosesMax();
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
        row.createCell(c++).setCellValue(this._COEFFICIENT_OF_VARIATION);

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
        row.createCell(c++).setCellValue(this._SOLVING_DIAGNOSES_NUM);
        row.createCell(c++).setCellValue(this._SOLVING_RUNTIME);
        row.createCell(c++).setCellValue(this._COMBINING_RUNTIME);
        row.createCell(c++).setCellValue(this._TOTAL_RUNTIME);
        row.createCell(c++).setCellValue(this._TIMEDOUT);
        row.createCell(c++).setCellValue(this._LOCAL_INTERNAL_ACTIONS_NUMBERS);
        row.createCell(c++).setCellValue(this._LOCAL_INTERNAL_ACTIONS_MIN);
        row.createCell(c++).setCellValue(this._LOCAL_INTERNAL_ACTIONS_MAX);
        row.createCell(c++).setCellValue(this._LOCAL_EXTERNAL_ACTIONS_NUMBERS);
        row.createCell(c++).setCellValue(this._LOCAL_EXTERNAL_ACTIONS_MIN);
        row.createCell(c++).setCellValue(this._LOCAL_EXTERNAL_ACTIONS_MAX);
        row.createCell(c++).setCellValue(this._LOCAL_TOTAL_ACTIONS_NUMBERS);
        row.createCell(c++).setCellValue(this._LOCAL_TOTAL_ACTIONS_MIN);
        row.createCell(c++).setCellValue(this._LOCAL_TOTAL_ACTIONS_MAX);
        row.createCell(c++).setCellValue(this._LOCAL_DIAGNOSES_NUMBERS);
        row.createCell(c++).setCellValue(this._LOCAL_DIAGNOSES_MIN);
        row.createCell(c++).setCellValue(this._LOCAL_DIAGNOSES_MAX);
        row.createCell(c).setCellValue(this._DIAGNOSES_NUM);
    }

    public void recordToTxtFile(File txtFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            // Create the record string
            String recordString =
                "Benchmark:" + this._BENCHMARK_NAME + "\n" +
                "Domain:" + this._DOMAIN_NAME + "\n" +
                "Problem:" + this._PROBLEM_NAME + "\n" +
                "# Faults number:" + this._FAULTS_NUM + "\n" +
                "# Repetition index:" + this._REPETITION_NUM + "\n" +
                "Observability:" + this._OBSERVABILITY + "\n" +
                "Reasoner:" + this._REASONER_NAME + "\n" +

                "# Agents number:" + this._AGENTS_NUM + "\n" +
                "# Goal size:" + this._GOAL_SIZE + "\n" +
                "# Goal/Agent:" + this._GOAL_AGENT_RATIO + "\n" +
                "# Plan length:" + this._PLAN_LENGTH + "\n" +
                "# Coefficient of variation:" + this._COEFFICIENT_OF_VARIATION + "\n" +

                "M Agent name:" + this._MODELLING_AGENT_NAME + "\n" +
                "# M Predicates number:" + this._MODELLING_PREDICATES_NUM + "\n" +
                "# M Actions number:" + this._MODELLING_ACTIONS_NUM + "\n" +
                "# M Variables number:" + this._MODELLING_VARIABLES_NUM + "\n" +
                "# M Constraints number:" + this._MODELLING_CONSTRAINTS_NUM + "\n" +
                "# M Runtime:" + this._MODELLING_RUNTIME + "\n" +
                "S Agent name:" + this._SOLVING_AGENT_NAME + "\n" +
                "# S Predicates number:" + this._SOLVING_PREDICATES_NUM + "\n" +
                "# S Actions number:" + this._SOLVING_ACTIONS_NUM + "\n" +
                "# S Variables number:" + this._SOLVING_VARIABLES_NUM + "\n" +
                "# S Constraints number:" + this._SOLVING_CONSTRAINTS_NUM + "\n" +
                "# S Diagnoses number:" + this._SOLVING_DIAGNOSES_NUM + "\n" +
                "# S Runtime:" + this._SOLVING_RUNTIME + "\n" +
                "# Combining runtime:" + this._COMBINING_RUNTIME + "\n" +
                "# Total runtime:" + this._TOTAL_RUNTIME + "\n" +
                "# TimedOut:" + this._TIMEDOUT + "\n" +
                "Local internal actions numbers:" + this._LOCAL_INTERNAL_ACTIONS_NUMBERS + "\n" +
                "# Local internal actions min:" + this._LOCAL_INTERNAL_ACTIONS_MIN + "\n" +
                "# Local internal actions max:" + this._LOCAL_INTERNAL_ACTIONS_MAX + "\n" +
                "Local external actions numbers:" + this._LOCAL_EXTERNAL_ACTIONS_NUMBERS + "\n" +
                "# Local external actions min:" + this._LOCAL_EXTERNAL_ACTIONS_MIN + "\n" +
                "# Local external actions max:" + this._LOCAL_EXTERNAL_ACTIONS_MAX + "\n" +
                "Local total actions numbers:" + this._LOCAL_TOTAL_ACTIONS_NUMBERS + "\n" +
                "# Local total actions min:" + this._LOCAL_TOTAL_ACTIONS_MIN + "\n" +
                "# Local total actions max:" + this._LOCAL_TOTAL_ACTIONS_MAX + "\n" +
                "Local diagnoses numbers:" + this._LOCAL_DIAGNOSES_NUMBERS + "\n" +
                "# Local diagnoses min:" + this._LOCAL_DIAGNOSES_MIN + "\n" +
                "# Local diagnoses max:" + this._LOCAL_DIAGNOSES_MAX + "\n" +
                "# Diagnoses number:" + this._DIAGNOSES_NUM;
            writer.write(recordString);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }
}

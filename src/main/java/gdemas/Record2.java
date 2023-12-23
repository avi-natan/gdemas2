package gdemas;

import org.apache.poi.ss.usermodel.Row;

import static gdemas.Utils.print;

public class Record2 {
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
            "# Size max subgroup",
            "# Percent max subgroup",
            "# Diagnoses number",
            "successful",
            "Comparable"
    };

    // output members
    // controlled parameters
    public String                           _BENCHMARK_NAME;                // String
    public String                           _DOMAIN_NAME;                   // String
    public String                           _PROBLEM_NAME;                  // String
    public String                           _FAULTS_NUM;                    // int
    public String                           _REPETITION_NUM;                // int
    public String                           _OBSERVABILITY;                 // String
    public String                           _REASONER_NAME;                 // String

    // instance non-controlled parameters
    public String                           _AGENTS_NUM;                    // int
    public String                           _GOAL_SIZE;                     // int
    public String                           _GOAL_AGENT_RATIO;              // double
    public String                           _PLAN_LENGTH;                   // int
    public String                           _COEFFICIENT_OF_VARIATION;      // double

    // measurement members
    public String                           _MODELLING_AGENT_NAME;          // String
    public String                           _MODELLING_PREDICATES_NUM;      // int
    public String                           _MODELLING_ACTIONS_NUM;         // int
    public String                           _MODELLING_VARIABLES_NUM;       // int
    public String                           _MODELLING_CONSTRAINTS_NUM;     // int
    public String                           _MODELLING_RUNTIME;             // long
    public String                           _SOLVING_AGENT_NAME;            // String
    public String                           _SOLVING_PREDICATES_NUM;        // int
    public String                           _SOLVING_ACTIONS_NUM;           // int
    public String                           _SOLVING_VARIABLES_NUM;         // int
    public String                           _SOLVING_CONSTRAINTS_NUM;       // int
    public String                           _SOLVING_DIAGNOSES_NUM;         // int
    public String                           _SOLVING_RUNTIME;               // long
    public String                           _COMBINING_RUNTIME;             // long
    public String                           _TOTAL_RUNTIME;                 // long
    public String                           _TIMEDOUT;                      // int
    public String                           _LOCAL_INTERNAL_ACTIONS_NUMBERS;// String
    public String                           _LOCAL_INTERNAL_ACTIONS_MIN;    // int
    public String                           _LOCAL_INTERNAL_ACTIONS_MAX;    // int
    public String                           _LOCAL_EXTERNAL_ACTIONS_NUMBERS;// String
    public String                           _LOCAL_EXTERNAL_ACTIONS_MIN;    // int
    public String                           _LOCAL_EXTERNAL_ACTIONS_MAX;    // int
    public String                           _LOCAL_TOTAL_ACTIONS_NUMBERS;   // String
    public String                           _LOCAL_TOTAL_ACTIONS_MIN;       // int
    public String                           _LOCAL_TOTAL_ACTIONS_MAX;       // int
    public String                           _LOCAL_DIAGNOSES_NUMBERS;       // String
    public String                           _LOCAL_DIAGNOSES_MIN;           // int
    public String                           _LOCAL_DIAGNOSES_MAX;           // int
    public String                           _DIAGNOSES_NUM;                 // int
    public String                           _SIZE_MAX_SUBGROUP;             // int
    public String                           _PERCENT_MAX_SUBGROUP;          // double
    public String                           _SUCCESSFUL;                    // String
    public String                           _COMPARABLE;                    // String

    public void recordToRow(Row row) {
        if (this._SUCCESSFUL.equals("yes")) {
            recordSuccessfulToRow(row);
        } else {
            recordFailedToRow(row);
        }
    }

    private void recordFailedToRow(Row row) {
        int c = 0;
        row.createCell(c++).setCellValue(this._BENCHMARK_NAME);
        row.createCell(c++).setCellValue(this._DOMAIN_NAME);
        row.createCell(c++).setCellValue(this._PROBLEM_NAME);
        row.createCell(c++).setCellValue(Integer.parseInt(this._FAULTS_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._REPETITION_NUM));
        row.createCell(c++).setCellValue(this._OBSERVABILITY);
        row.createCell(c++).setCellValue(this._REASONER_NAME);

        // instance non-controlled parameters
        row.createCell(c++).setCellValue(Integer.parseInt(this._AGENTS_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._GOAL_SIZE));
        row.createCell(c++).setCellValue(Double.parseDouble(this._GOAL_AGENT_RATIO));
        row.createCell(c++).setCellValue(Integer.parseInt(this._PLAN_LENGTH));
        row.createCell(c++).setCellValue(Double.parseDouble(this._COEFFICIENT_OF_VARIATION));

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
        row.createCell(c++).setCellValue(this._SIZE_MAX_SUBGROUP);
        row.createCell(c++).setCellValue(this._PERCENT_MAX_SUBGROUP);
        row.createCell(c++).setCellValue(this._DIAGNOSES_NUM);
        row.createCell(c++).setCellValue(this._SUCCESSFUL);
        row.createCell(c).setCellValue(this._COMPARABLE);
    }

    private void recordSuccessfulToRow(Row row) {
        int c = 0;
        row.createCell(c++).setCellValue(this._BENCHMARK_NAME);
        row.createCell(c++).setCellValue(this._DOMAIN_NAME);
        row.createCell(c++).setCellValue(this._PROBLEM_NAME);
        row.createCell(c++).setCellValue(Integer.parseInt(this._FAULTS_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._REPETITION_NUM));
        row.createCell(c++).setCellValue(this._OBSERVABILITY);
        row.createCell(c++).setCellValue(this._REASONER_NAME);

        // instance non-controlled parameters
        row.createCell(c++).setCellValue(Integer.parseInt(this._AGENTS_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._GOAL_SIZE));
        row.createCell(c++).setCellValue(Double.parseDouble(this._GOAL_AGENT_RATIO));
        row.createCell(c++).setCellValue(Integer.parseInt(this._PLAN_LENGTH));
        row.createCell(c++).setCellValue(Double.parseDouble(this._COEFFICIENT_OF_VARIATION));

        // measurement members
        row.createCell(c++).setCellValue(this._MODELLING_AGENT_NAME);
        row.createCell(c++).setCellValue(Integer.parseInt(this._MODELLING_PREDICATES_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._MODELLING_ACTIONS_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._MODELLING_VARIABLES_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._MODELLING_CONSTRAINTS_NUM));
        row.createCell(c++).setCellValue(Long.parseLong(this._MODELLING_RUNTIME));
        row.createCell(c++).setCellValue(this._SOLVING_AGENT_NAME);
        row.createCell(c++).setCellValue(Integer.parseInt(this._SOLVING_PREDICATES_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._SOLVING_ACTIONS_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._SOLVING_VARIABLES_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._SOLVING_CONSTRAINTS_NUM));
        row.createCell(c++).setCellValue(Integer.parseInt(this._SOLVING_DIAGNOSES_NUM));
        row.createCell(c++).setCellValue(Long.parseLong(this._SOLVING_RUNTIME));
        row.createCell(c++).setCellValue(Long.parseLong(this._COMBINING_RUNTIME));
        row.createCell(c++).setCellValue(Long.parseLong(this._TOTAL_RUNTIME));
        row.createCell(c++).setCellValue(Integer.parseInt(this._TIMEDOUT));
        row.createCell(c++).setCellValue(this._LOCAL_INTERNAL_ACTIONS_NUMBERS);
        row.createCell(c++).setCellValue(Integer.parseInt(this._LOCAL_INTERNAL_ACTIONS_MIN));
        row.createCell(c++).setCellValue(Integer.parseInt(this._LOCAL_INTERNAL_ACTIONS_MAX));
        row.createCell(c++).setCellValue(this._LOCAL_EXTERNAL_ACTIONS_NUMBERS);
        row.createCell(c++).setCellValue(Integer.parseInt(this._LOCAL_EXTERNAL_ACTIONS_MIN));
        row.createCell(c++).setCellValue(Integer.parseInt(this._LOCAL_EXTERNAL_ACTIONS_MAX));
        row.createCell(c++).setCellValue(this._LOCAL_TOTAL_ACTIONS_NUMBERS);
        row.createCell(c++).setCellValue(Integer.parseInt(this._LOCAL_TOTAL_ACTIONS_MIN));
        row.createCell(c++).setCellValue(Integer.parseInt(this._LOCAL_TOTAL_ACTIONS_MAX));
        row.createCell(c++).setCellValue(this._LOCAL_DIAGNOSES_NUMBERS);
        row.createCell(c++).setCellValue(Integer.parseInt(this._LOCAL_DIAGNOSES_MIN));
        row.createCell(c++).setCellValue(Integer.parseInt(this._LOCAL_DIAGNOSES_MAX));
        row.createCell(c++).setCellValue(Integer.parseInt(this._SIZE_MAX_SUBGROUP));
        row.createCell(c++).setCellValue(Double.parseDouble(this._PERCENT_MAX_SUBGROUP));
        row.createCell(c++).setCellValue(Integer.parseInt(this._DIAGNOSES_NUM));
        row.createCell(c++).setCellValue(this._SUCCESSFUL);
        row.createCell(c).setCellValue(this._COMPARABLE);
    }
}

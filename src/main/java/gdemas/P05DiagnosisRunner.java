package gdemas;

import java.io.*;
import java.util.Objects;

import static gdemas.Utils.*;
import static gdemas.Utils.listDirectories;

public class P05DiagnosisRunner {

    public static void execute(String executionMode, String[] observabilities) {
        File inputFolder = new File("benchmarks/mastrips/05 - faulty executions");
        File outputFolder = new File("benchmarks/mastrips/06 - results");

        Report report;

        if (executionMode.equals("new")) {
            deleteFolderContents(outputFolder);
            report = new Report();
            report.executionType = "new";
        } else {
            report = new Report();
            report.executionType = executionMode;
            File lastReportFile = new File("benchmarks/mastrips/06 - results/report.txt");
            if (lastReportFile.exists()) {
                report.parseFromLastReport(lastReportFile);
            }
        }

        String[] reasoners = {
                "simple",
                "smart",
                "amazing",
                "amazing2"
        };

        File[] domainFolders = listDirectories(inputFolder);
        for (File domainFolder: domainFolders) {
            File domainFolder06 = new File(outputFolder, domainFolder.getName());
            mkdirIfDoesntExist(domainFolder06);
            File domainFile = new File(domainFolder, domainFolder.getName() + "-domain.pddl");
            File domainFile06 = new File(domainFolder06, domainFolder06.getName() + "-domain.pddl");
            copyFileIfDoesntExist(domainFile, domainFile06);
            print(java.time.LocalTime.now() + ": " + domainFile06.getAbsolutePath());

            File[] problemFolders = listDirectories(domainFolder);
            for (File problemFolder: problemFolders) {
                File problemFolder06 = new File(domainFolder06, problemFolder.getName());
                mkdirIfDoesntExist(problemFolder06);
                File problemFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + ".pddl");
                File problemFile06 = new File(problemFolder06, domainFolder06.getName() + "-" + problemFolder06.getName() + ".pddl");
                copyFileIfDoesntExist(problemFile, problemFile06);
                print(java.time.LocalTime.now() + ": " + problemFile06.getAbsolutePath());
                File agentsFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-agents.txt");
                File agentsFile06 = new File(problemFolder06, domainFolder06.getName() + "-" + problemFolder06.getName() + "-agents.txt");
                copyFileIfDoesntExist(agentsFile, agentsFile06);
                print(java.time.LocalTime.now() + ": " + agentsFile06.getAbsolutePath());
                File combinedPlanFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-combined_plan.solution");
                File combinedPlanFile06 = new File(problemFolder06, domainFolder06.getName() + "-" + problemFolder06.getName() + "-combined_plan.solution");
                copyFileIfDoesntExist(combinedPlanFile, combinedPlanFile06);
                print(java.time.LocalTime.now() + ": " + combinedPlanFile06.getAbsolutePath());
                File planFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-plan.txt");
                File planFile06 = new File(problemFolder06, domainFolder06.getName() + "-" + problemFolder06.getName() + "-plan.txt");
                copyFileIfDoesntExist(planFile, planFile06);
                print(java.time.LocalTime.now() + ": " + planFile06.getAbsolutePath());

                File[] faultFolders = listDirectories(problemFolder);
                for (File faultFolder: faultFolders) {
                    File faultFolder06 = new File(problemFolder06, faultFolder.getName());
                    mkdirIfDoesntExist(faultFolder06);

                    File[] faultFiles = Objects.requireNonNullElseGet(faultFolder.listFiles((dir, name) -> name.endsWith("-faults.txt")), () -> new File[]{});
                    for (File faultFile: faultFiles) {
                        File faultFile06 = new File(faultFolder06, faultFile.getName());
                        copyFileIfDoesntExist(faultFile, faultFile06);
                        print(java.time.LocalTime.now() + ": " + faultFile06.getAbsolutePath());
                        File trajectoryFile = new File(faultFolder, faultFile.getName().substring(0, faultFile.getName().length()-11) + "-combined_trajectory.trajectory");
                        File trajectoryFile06 = new File(faultFolder06, faultFile.getName().substring(0, faultFile.getName().length()-11) + "-combined_trajectory.trajectory");
                        copyFileIfDoesntExist(trajectoryFile, trajectoryFile06);

                        for (String observability : observabilities) {
                            for (String s : reasoners) {
                                File resultsFile06r = new File(faultFolder06, faultFile.getName().substring(0, faultFile.getName().length() - 11) + "-" + observability + "-" + s + "-results.txt");
                                print(java.time.LocalTime.now() + ": " + resultsFile06r.getAbsolutePath());
                                // if the results file exists, continue
                                if (resultsFile06r.exists()) {
                                    print(java.time.LocalTime.now() + ": " + "exists");
                                    report.existed += 1;
                                } else {
                                    File failTxt = new File(faultFolder06, faultFile.getName().substring(0, faultFile.getName().length() - 11) + "-" + observability + "-" + s + "-fail.txt");
                                    if (failTxt.exists()) {
                                        if (executionMode.equals("continueSkipFailed")) {
                                            print(java.time.LocalTime.now() + ": " + "skip");
                                            report.skipped += 1;
                                            report.skippedFiles.add(failTxt.getAbsolutePath());
                                            continue;
                                        }
                                    } else {
                                        try {
                                            failTxt.createNewFile();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    report.attempts += 1;
                                    Record record;
                                    Reasoner reasoner = null;
                                    int attempt = 0;
                                    int maxAttempts = 1;
                                    boolean success = false;
                                    switch (s) {
                                        case "simple":
                                            while (attempt < maxAttempts) {
                                                try {
                                                    reasoner = new ReasonerSimple("mastrips", domainFolder06.getName(), problemFolder06.getName(), domainFile06, problemFile06, agentsFile06, combinedPlanFile06, faultFile06, trajectoryFile06, observability);
                                                    reasoner.diagnoseProblem();
                                                    success = true;
                                                    break;
                                                } catch (OutOfMemoryError | Exception e) {
                                                    e.printStackTrace();
                                                    attempt += 1;
                                                }
                                            }
                                            break;
                                        case "smart":
                                            while (attempt < maxAttempts) {
                                                try {
                                                    reasoner = new ReasonerSmart("mastrips", domainFolder06.getName(), problemFolder06.getName(), domainFile06, problemFile06, agentsFile06, combinedPlanFile06, faultFile06, trajectoryFile06, observability);
                                                    reasoner.diagnoseProblem();
                                                    success = true;
                                                    break;
                                                } catch (OutOfMemoryError | Exception e) {
                                                    e.printStackTrace();
                                                    attempt += 1;
                                                }
                                            }
                                            break;
                                        case "amazing":
                                            while (attempt < maxAttempts) {
                                                try {
                                                    reasoner = new ReasonerAmazing("mastrips", domainFolder06.getName(), problemFolder06.getName(), domainFile06, problemFile06, agentsFile06, combinedPlanFile06, faultFile06, trajectoryFile06, observability);
                                                    reasoner.diagnoseProblem();
                                                    success = true;
                                                    break;
                                                } catch (OutOfMemoryError | Exception e) {
                                                    e.printStackTrace();
                                                    attempt += 1;
                                                }
                                            }
                                            break;
                                        case "amazing2":
                                            while (attempt < maxAttempts) {
                                                try {
                                                    reasoner = new ReasonerAmazing2("mastrips", domainFolder06.getName(), problemFolder06.getName(), domainFile06, problemFile06, agentsFile06, combinedPlanFile06, faultFile06, trajectoryFile06, observability);
                                                    reasoner.diagnoseProblem();
                                                    success = true;
                                                    break;
                                                } catch (OutOfMemoryError | Exception e) {
                                                    e.printStackTrace();
                                                    attempt += 1;
                                                }
                                            }
                                            break;
                                        default:
                                            throw new RuntimeException("invalid reasoner name");
                                    }
                                    if (success) {
                                        failTxt.delete();
                                        record = new Record(reasoner);
                                        saveRecordToTxtFile(record, resultsFile06r);
                                        print(java.time.LocalTime.now() + ": " + "success " + record._TOTAL_RUNTIME);
                                        report.success += 1;
                                        report.successfulFiles.add(resultsFile06r.getAbsolutePath());
                                    } else {
                                        print(java.time.LocalTime.now() + ": " + "fail");
                                        report.fail += 1;
                                        report.failedFiles.add(resultsFile06r.getAbsolutePath());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        report.totalExisting = report.existed + report.success;
        File reportFile = new File("benchmarks/mastrips/06 - results/report.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(report.toString());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
        print(9);
    }

    private static void saveRecordToTxtFile(Record record, File txtFile) {
        record.recordToTxtFile(txtFile);
    }
}

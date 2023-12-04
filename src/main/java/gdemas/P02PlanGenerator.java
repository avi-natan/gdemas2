package gdemas;

import java.io.*;

import static gdemas.Utils.*;
import static gdemas.Utils.listDirectories;

public class P02PlanGenerator {

    public static void execute(String executionMode) {
        File inputFolder = new File("benchmarks/mastrips/02 - agent names");
        File outputFolder = new File("benchmarks/mastrips/03 - plans");

        Report report;

        if (executionMode.equals("new")) {
            deleteFolderContents(outputFolder);
            report = new Report();
            report.executionType = "new";
        } else {
            report = new Report();
            report.executionType = "continue";
            File lastReportFile = new File("benchmarks/mastrips/03 - plans/report.txt");
            if (lastReportFile.exists()) {
                report.parseFromLastReport(lastReportFile);
            }
        }

        File[] domainFolders = listDirectories(inputFolder);
        for (File domainFolder: domainFolders) {
            File domainFolder03 = new File(outputFolder, domainFolder.getName());
            mkdirIfDoesntExist(domainFolder03);
            File domainFile = new File(domainFolder, domainFolder.getName() + "-domain.pddl");
            File domainFile03 = new File(domainFolder03, domainFolder03.getName() + "-domain.pddl");
            copyFileIfDoesntExist(domainFile, domainFile03);
            print(domainFile03.getAbsolutePath());
            File[] problemFolders = listDirectories(domainFolder);

            for (File problemFolder: problemFolders) {
                // prepare working files
                File problemFolder03 = new File(domainFolder03, problemFolder.getName());
                mkdirIfDoesntExist(problemFolder03);
                File problemFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + ".pddl");
                File problemFile03 = new File(problemFolder03, domainFolder03.getName() + "-" + problemFolder03.getName() + ".pddl");
                copyFileIfDoesntExist(problemFile, problemFile03);
//                print(problemFile03.getAbsolutePath());
                File agentsFile = new File(problemFolder, domainFolder.getName() + "-" + problemFolder.getName() + "-agents.txt");
                File agentsFile03 = new File(problemFolder03, domainFolder03.getName() + "-" + problemFolder03.getName() + "-agents.txt");
                copyFileIfDoesntExist(agentsFile, agentsFile03);
//                print(agentsFile03.getAbsolutePath());

                File planFile03 = new File(problemFolder03, domainFolder03.getName() + "-" + problemFolder03.getName() + "-plan.txt");

                if (planFile03.exists()) {
                    print(problemFile03.getAbsolutePath() + ": exists");
                    report.existed += 1;
                } else {
                    File planFileFail03 = new File(problemFolder03, domainFolder03.getName() + "-" + problemFolder03.getName() + "-plan_fail.txt");
                    if (planFileFail03.exists() && executionMode.equals("continueSkipFailed")) {
                        print(problemFile03.getAbsolutePath() + ": skip");
                        report.skipped += 1;
                        report.skippedFiles.add(planFileFail03.getAbsolutePath());
                    } else {
                        if (planFileFail03.exists()) {
                            planFileFail03.delete();
                        }
                        report.attempts += 1;
                        print(problemFile03.getAbsolutePath() + ": fetching");
                        // use the python script to get the plans
                        try {
                            String arg1 = domainFile03.getAbsolutePath();
                            String arg2 = problemFile03.getAbsolutePath();
                            String arg3 = planFile03.getAbsolutePath();

                            // Specify the Python script path
                            String pythonScriptPath = "api_plan_generator.py";

                            // Build the command to run the Python script
                            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, arg1, arg2, arg3);
                            processBuilder.redirectErrorStream(true);

                            // Start the process
                            Process process = processBuilder.start();

                            // Read the output of the Python script and write it on this screen
                            StringBuilder output = new StringBuilder();
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    output.append(line).append(System.lineSeparator());
                                }
                            }
                            print(output.toString());

                            // Wait for the process to complete
                            int exitCode = process.waitFor();

                            // Print exit code
                            System.out.println("Python script exit code: " + exitCode);

                            // remove any plan file created if exit code is not 0
                            if (exitCode != 0) {
                                if (planFile03.exists()) {
                                    planFile03.delete();
                                }
                                print(problemFile03.getAbsolutePath() + ": failed");
                                report.fail += 1;
                                report.failedFiles.add(planFile03.getAbsolutePath());
                                planFileFail03.createNewFile();
                            } else {
                                print(problemFile03.getAbsolutePath() + ": success");
                                report.success += 1;
                                report.successfulFiles.add(planFile03.getAbsolutePath());
                            }
                        } catch (IOException | InterruptedException e) {
                            if (planFile03.exists()) {
                                planFile03.delete();
                            }
                            print(problemFile03.getAbsolutePath() + ": failed");
                            report.fail += 1;
                            report.failedFiles.add(planFile03.getAbsolutePath());
                            try {
                                planFileFail03.createNewFile();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            e.printStackTrace();
                        }

                        print("waiting 1 minute cooldown\n");
                        try {
                            // Sleep for 1 minute (60,000 milliseconds)
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            // Handle the exception if the thread is interrupted while sleeping
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        report.totalExisting = report.totalExistingLast + report.success;
        File reportFile = new File("benchmarks/mastrips/03 - plans/report.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(report.toString());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }
}

package gdemas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static gdemas.Utils.listDirectories;
import static gdemas.Utils.print;

public class Scripts {

    public static void main (String [] args) {

        print("hello");

        scriptFindCasesWithZeroDiagnosesThatDidntTimeout();
//        scriptCheckMultipleOfTheSamePredicatesInAState();

    }

    private static void scriptFindCasesWithZeroDiagnosesThatDidntTimeout() {
        List<String> failedFiles = new ArrayList<>();
        List<String> buggedFiles = new ArrayList<>();
        List<String> goodFiles = new ArrayList<>();

        File workingFolder = new File("benchmarks/mastrips/06 - results");

        String[] domainNames = new String[]{
                "blocksworld",
                "depot",
                "driverlog",
                "logistics00",
                "rovers",
                "satellite",
                "taxi",
                "zenotravel"
        };

        for (String domainName : domainNames) {
            File domainFolder = new File(workingFolder, domainName);

            File[] problemFolders = listDirectories(domainFolder);
            for (File problemFolder : problemFolders) {
                print(problemFolder.getAbsolutePath());
                File[] faultFolders = listDirectories(problemFolder);
                for (File faultFolder : faultFolders) {
//                print(faultFolder.getAbsolutePath());
                    File[] resultFiles = Objects.requireNonNullElseGet(faultFolder.listFiles((dir, name) -> name.endsWith("-results.txt") || name.endsWith("-fail.txt")), () -> new File[]{});
                    for (File resultFile : resultFiles) {
//                    print(resultFile.getAbsolutePath());
                        if (resultFile.getName().endsWith("-fail.txt")) {
                            failedFiles.add(resultFile.getAbsolutePath());
                        } else {
                            String fileString = Parser.readFromFile(resultFile);
                            String[] registers = fileString.split("\r\n");
                            int diagnosesNumber = Integer.parseInt(registers[42].split(":")[1]);
                            int timedOut = Integer.parseInt(registers[27].split(":")[1]);
                            if (diagnosesNumber == 0 && timedOut == 0) {
                                buggedFiles.add(resultFile.getAbsolutePath());
                            } else {
                                goodFiles.add(resultFile.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
        print(9);

        StringBuilder bugString = new StringBuilder();
        bugString.append("Failed files: ").append(failedFiles.size());
        for (String s : failedFiles) {
            print("failed: " + s);
            bugString.append("\n").append(s);
            String duplicatesString = generateDuplicatesString(s);
            bugString.append("\n").append(duplicatesString);
        }
        bugString.append("\n\n\n");
        bugString.append("Bugged files: ").append(buggedFiles.size());
        for (String s : buggedFiles) {
            print("bugged: " + s);
            bugString.append("\n").append(s);
            String duplicatesString = generateDuplicatesString(s);
            bugString.append("\n").append(duplicatesString);
        }
        bugString.append("\n\n\n");
        bugString.append("Good files: ").append(goodFiles.size());
        for (String s : goodFiles) {
            print("good  : " + s);
            bugString.append("\n").append(s);
            String duplicatesString = generateDuplicatesString(s);
            bugString.append("\n").append(duplicatesString);
        }

        File reportFile = new File(workingFolder, "debug report.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(bugString.toString());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }

    private static String generateDuplicatesString(String s) {
        File resultFile = new File(s);
        File parent = resultFile.getParentFile();
        String resultsName = resultFile.getName();
        String[] parts = resultsName.split("-");
        StringBuilder trajectoryName = new StringBuilder();
        for (int i = 0; i < parts.length-3; i++) {
            trajectoryName.append(parts[i]).append("-");
        }
        trajectoryName.append("combined_trajectory.trajectory");
        File trajectoryFile = new File(parent, trajectoryName.toString());
        List<String> duplicates = calculateInstanceDuplicates(trajectoryFile);
        StringBuilder states = new StringBuilder("{");
        for (String k : duplicates) {
            states.append(k).append(", ");
        }
        if (states.length() > 1) {
            states.delete(states.length() - 2, states.length());
        }
        states.append("}");
        return states.toString();
    }

    private static void scriptCheckMultipleOfTheSamePredicatesInAState() {
        // parameters for easier changing
        String benchmarkName = "mastrips";
        String domainName = "rovers";
        String problemName = "p10";
        int faultsNum = 1;
        int repetitionNum = 8;
        String observability = "99p";
        long timeout = 10000;

        // input files based on the parameters
        File trajectoryFile = new File("benchmarks - sandbox/" + benchmarkName + "/" + domainName + "/" + problemName + "/" + faultsNum + "/" + domainName + "-" + problemName + "-f[" + faultsNum + "]-r[" + repetitionNum + "]-combined_trajectory.trajectory");

        List<String> instanceDuplicates = calculateInstanceDuplicates(trajectoryFile);

        print(trajectoryFile.getAbsolutePath() + " duplicates in states: ");
        StringBuilder states = new StringBuilder("{");
        for (String k : instanceDuplicates) {
            states.append(k).append(", ");
        }
        if (states.length() > 1) {
            states.delete(states.length() - 2, states.length());
        }
        states.append("}");
        print(states.toString());

        print(9);

    }

    private static List<String> calculateInstanceDuplicates(File trajectoryFile) {
        List<List<String>> trajectory = Parser.parseTrajectory(trajectoryFile);

        Map<String, List<String>> instanceDuplicates = new HashMap<>();
        for (int t = 0; t < trajectory.size(); t++) {
            List<String> stateT = trajectory.get(t);
            List<String> unique = new ArrayList<>();
            List<String> duplicates = new ArrayList<>();
            for (String p : stateT) {
                if (unique.contains(p)) {
                    duplicates.add(p);
                } else {
                    unique.add(p);
                }
            }
            if (!duplicates.isEmpty()) {
                instanceDuplicates.put("" + t, duplicates);
            }
        }

        List<String> res = new ArrayList<>(instanceDuplicates.keySet());
        return res;
    }
}

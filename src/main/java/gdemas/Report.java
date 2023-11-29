package gdemas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Report {
    public String executionType;

    public int existedLast;
    public int skippedLast;
    public int attemptsLast;
    public int successLast;
    public int failLast;
    public int totalExistingLast;

    public int existed;
    public int skipped;
    public int attempts;
    public int success;
    public int fail;
    public int totalExisting;

    public List<String> skippedFiles = new ArrayList<>();

    public List<String> successfulFiles = new ArrayList<>();

    public List<String> failedFiles = new ArrayList<>();

    public String toString() {

        return
                "Report\n" + "execution type:" + executionType + "\n\n" +

                "Last report stats\n" +
                "existed:" + existedLast + "\n" +
                "skipped:" + skippedLast + "\n" +
                "attempts:" + attemptsLast + "\n" +
                "success:" + successLast + "\n" +
                "fail:" + failLast + "\n" +
                "total existing:" + totalExistingLast + "\n\n" +

                "Current report stats\n" +
                "existed:" + existed + "\n" +
                "skipped:" + skipped + "\n" +
                "attempts:" + attempts + "\n" +
                "success:" + success + "\n" +
                "fail:" + fail + "\n" +
                "total existing:" + totalExisting + "\n\n" +

                "Skipped files list\n" +
                String.join("\r\n", skippedFiles) + "\n\n" +

                "Successful files list\n" +
                String.join("\r\n", successfulFiles) + "\n\n" +

                "Failed files list\n" +
                String.join("\r\n", failedFiles) + "\n";
    }

    public void parseFromLastReport(File file) {
        String content;
        try {
            content = String.join(System.lineSeparator(), Files.readAllLines(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] previousStats = content.split("Current report stats\r\n")[1].split("\r\n\r\nSkipped files list")[0].split("\r\n");
        existedLast = Integer.parseInt(previousStats[0].split(":")[1]);
        skippedLast = Integer.parseInt(previousStats[1].split(":")[1]);
        attemptsLast = Integer.parseInt(previousStats[2].split(":")[1]);
        successLast = Integer.parseInt(previousStats[3].split(":")[1]);
        failLast = Integer.parseInt(previousStats[4].split(":")[1]);
        totalExistingLast = Integer.parseInt(previousStats[5].split(":")[1]);
    }
}

package gdemas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static void print(String string) {
        System.out.println(string);
    }

    public static void print(int string) {
        System.out.println(string);
    }

    public static void print(double string) {
        System.out.println(string);
    }

    public static File[] listDirectories(File parentDirectory) {
        File[] dirs = Objects.requireNonNullElseGet(parentDirectory.listFiles(File::isDirectory), () -> new File[]{});
        Arrays.sort(dirs, new WindowsComparator());
        return dirs;
    }

    public static void deleteFolderContents(File folder) {
        // If it's a directory, list its contents
        File[] contents = folder.listFiles();
        if (contents != null) {
            for (File file : contents) {
                // Recursively delete files and directories within the directory
                recursiveDelete(file);
            }
        }
    }

    public static void recursiveDelete(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            // If it's a directory, list its contents
            File[] contents = fileOrDirectory.listFiles();
            if (contents != null) {
                for (File file : contents) {
                    // Recursively delete files and directories within the directory
                    recursiveDelete(file);
                }
            }
        }

        // Delete the file or directory
        fileOrDirectory.delete();
    }

    public static void mkdirIfDoesntExist(File folder) {
        if (!folder.exists()){
            folder.mkdir();
        }
    }

    public static void copyFileIfDoesntExist(File src, File dst) {
        if (!dst.exists()) {
            try {
                Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int calculateTotalActionsNumber(List<List<String>> combinedPlanActions) {
        int res = 0;
        for (List<String> combinedPlanAction : combinedPlanActions) {
            for (String s : combinedPlanAction) {
                if (!s.equals("nop")) {
                    res += 1;
                }
            }
        }
        return res;
    }

}

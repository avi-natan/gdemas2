package gdemas;

import java.io.File;
import java.util.Objects;

public class Utils {

    public static void print(String string) {
        System.out.println(string);
    }

    public static void print(int string) {
        System.out.println(string);
    }

    public static File[] listDirectories(File parentDirectory) {
        return Objects.requireNonNullElseGet(parentDirectory.listFiles(File::isDirectory), () -> new File[]{});
    }

}

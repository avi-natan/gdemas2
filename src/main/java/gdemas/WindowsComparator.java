package gdemas;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WindowsComparator implements Comparator<File> {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    @Override
    public int compare(File f1, File f2) {
        String s1 = f1.getName();
        String s2 = f2.getName();
        // Extract numerical parts from strings
        Matcher matcher1 = NUMBER_PATTERN.matcher(s1);
        Matcher matcher2 = NUMBER_PATTERN.matcher(s2);

        while (matcher1.find() && matcher2.find()) {
            // Compare numerical parts as integers
            int num1 = Integer.parseInt(matcher1.group());
            int num2 = Integer.parseInt(matcher2.group());

            int result = Integer.compare(num1, num2);

            if (result != 0) {
                return result;
            }

            // Move to the next match if there are more digits
            matcher1.find(matcher1.end());
            matcher2.find(matcher2.end());
        }

        // If numerical parts are the same, compare lexicographically
        return s1.compareToIgnoreCase(s2);
    }
}
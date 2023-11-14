package gdemas;

import java.util.List;
import java.util.StringJoiner;

public class GlobalDiagnosis {
    public List<List<String>> actionHealthStates;
    public List<Integer> constituentDiagnosisIndices;

    public String toString () {
        StringJoiner result = new StringJoiner("\n");
        int i = 0;
        for (List<String> innerList : this.actionHealthStates) {
            StringJoiner innerJoiner = new StringJoiner(" ");
            for (String item : innerList) {
                innerJoiner.add(item);
            }
            result.add(i + ":\t" + innerJoiner);
            i += 1;
        }
        return result.toString();
    }
}

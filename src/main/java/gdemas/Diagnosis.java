package gdemas;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;

import java.util.*;

public class Diagnosis {
    private final List<String> svals;
    private final List<List<String>> actionHealthStates;
    public Diagnosis(Solution s, BijectiveMap<String, BoolVar> vmap, int planLength, int agentsNum) {
        this.svals = new ArrayList<>();
        for (BoolVar b: vmap.values()) {
            this.svals.add("(" + vmap.getKey(b) + "=" + s.getIntVal(b) + ")");
        }
        this.actionHealthStates = new ArrayList<>();
        for (int t = 0; t < planLength; t++) {
            List<String> stepHealthStates = new ArrayList<>();
            for (int a = 0; a < agentsNum; a++) {
                BoolVar b = vmap.getValue("H:" + t + ":" + a + ":h");
                if (b == null) {
                    stepHealthStates.add("x");
                } else if (s.getIntVal(b) == 1) {
                    stepHealthStates.add("h");
                } else {
                    b = vmap.getValue("H:" + t + ":" + a + ":f");
                    if (s.getIntVal(b) == 1) {
                        stepHealthStates.add("f");
                    } else {
                        b = vmap.getValue("H:" + t + ":" + a + ":c");
                        if (s.getIntVal(b) == 1) {
                            stepHealthStates.add("c");
                        } else {
                            stepHealthStates.add("-");
                        }
                    }
                }
            }
            this.actionHealthStates.add(stepHealthStates);
        }
    }

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

    public List<String> getSvals() {
        return svals;
    }

    public List<List<String>> getActionHealthStates() {
        return actionHealthStates;
    }
}

package gdemas;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;

import java.util.*;

public class Diagnosis {
    public final List<String> svals;
    public final List<List<String>> actionHealthStates;
    public final String hash;

    public Diagnosis(Solution s, BijectiveMap<String, BoolVar> vmap, int planLength, int agentsNum) {
        this.svals = new ArrayList<>();
        for (BoolVar b: vmap.values()) {
            this.svals.add("(" + vmap.getKey(b) + "=" + s.getIntVal(b) + ")");
        }
        this.actionHealthStates = new ArrayList<>();
        for (int t = 0; t < planLength; t++) {
            List<String> stepHealthStates = new ArrayList<>();
            for (int a = 0; a < agentsNum; a++) {
                if (vmap.containsKey("H:" + t + ":" + a + ":h") && s.getIntVal(vmap.getValue("H:" + t + ":" + a + ":h")) == 1) {
                    stepHealthStates.add("h");
                } else if (vmap.containsKey("H:" + t + ":" + a + ":f") && s.getIntVal(vmap.getValue("H:" + t + ":" + a + ":f")) == 1) {
                    stepHealthStates.add("f");
                } else if (vmap.containsKey("H:" + t + ":" + a + ":c") && s.getIntVal(vmap.getValue("H:" + t + ":" + a + ":c")) == 1) {
                    stepHealthStates.add("c");
                } else if (vmap.containsKey("H:" + t + ":" + a + ":i") && s.getIntVal(vmap.getValue("H:" + t + ":" + a + ":i")) == 1) {
                    stepHealthStates.add("i");
                } else if (vmap.containsKey("H:" + t + ":" + a + ":g") && s.getIntVal(vmap.getValue("H:" + t + ":" + a + ":g")) == 1) {
                    stepHealthStates.add("g");
                } else {
                    stepHealthStates.add("x");
                }
            }
            this.actionHealthStates.add(stepHealthStates);
        }
        this.hash = this.createHash();
    }

    private String createHash() {
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

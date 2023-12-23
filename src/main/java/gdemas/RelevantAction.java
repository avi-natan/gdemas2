package gdemas;

import java.util.ArrayList;
import java.util.List;

public class RelevantAction {

    private int actionT;
    private int actionA;
    private List<String> possibleValues;
    private List<RelevantAgent> relevantAgents;

    public RelevantAction (int t, int a) {
        this.actionT = t;
        this.actionA = a;
        this.possibleValues = new ArrayList<>(List.of("h", "f", "c"));
        this.relevantAgents = new ArrayList<>();
    }

    public void insertRelevantAgent (RelevantAgent ra) {
        if (!this.relevantAgents.contains(ra)) {
            this.relevantAgents.add(ra);
            ra.insertRelevantAction(this);
        }
    }

    public List<String> getPossibleValues() {
        return this.possibleValues;
    }

    public void setPossibleValues (List<String> values) {
        this.possibleValues = values;
    }

    public int getActionT () {
        return this.actionT;
    }

    public int getActionA () {
        return this.actionA;
    }

    public List<RelevantAgent> getRelevantAgents () {
        return this.relevantAgents;
    }
}

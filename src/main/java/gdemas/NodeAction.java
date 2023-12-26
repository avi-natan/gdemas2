package gdemas;

import java.util.ArrayList;
import java.util.List;

public class NodeAction {

    public final int id;
    public final int T;
    public final int A;
    public final String string;
    public List<NodeAgent> relevantAgents;
    public List<NodePredicate> relevantPredicates;

    public NodeAction (int id, int T, int A, String string) {
        this.id = id;
        this.T = T;
        this.A = A;
        this.string = string;
        this.relevantAgents = new ArrayList<>();
        this.relevantPredicates = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.T + ", " + this.A + ", " + this.string;
    }

}

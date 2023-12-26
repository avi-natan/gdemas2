package gdemas;

import java.util.ArrayList;
import java.util.List;

public class NodePredicate {

    public final int id;
    public final String string;
    public List<NodeAction> relevantActions;

    public NodePredicate (int id, String string) {
        this.id = id;
        this.string = string;
        this.relevantActions = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.string;
    }
}

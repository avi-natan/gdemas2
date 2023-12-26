package gdemas;

import java.util.ArrayList;
import java.util.List;

public class NodeAgent {

    public final int id;
    public final int num;
    public final String name;
    public List<NodeAction> relevantActions;

    public NodeAgent (int id, int num, String name) {
        this.id = id;
        this.num = num;
        this.name = name;
        this.relevantActions = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.num + ", " + this.name;
    }

}

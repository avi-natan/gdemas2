package gdemas;

import java.util.ArrayList;
import java.util.List;

public class Node {

    public final int graphID;
    public final int id;
    public final String string;
    public List<Node> neighbors;

    public Node (int graphID, int id, String string) {
        this.graphID = graphID;
        this.id = id;
        this.string = string;
        this.neighbors = new ArrayList<>();
    }

    @Override
    public String toString () {
        StringBuilder str = new StringBuilder();
        for (Node n : neighbors) {
            str.append(n.id).append(", ");
        }
        str.delete(str.length()-2, str.length());
        return this.id + " " + this.string + "; " + str;
    }

}

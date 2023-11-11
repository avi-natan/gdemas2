package gdemas;

import java.util.List;

public class Problem {
    public String       name;
    public List<String> objects;
    public List<String> init;
    public List<String> goal;

    public Problem(String       name,
                   List<String> objects,
                   List<String> init,
                   List<String> goal) {
        this.name       = name;
        this.objects    = objects;
        this.init       = init;
        this.goal       = goal;
    }
}

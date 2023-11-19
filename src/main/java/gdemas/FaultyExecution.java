package gdemas;

import java.util.List;

public class FaultyExecution {
    public List<String> faults;
    public List<List<String>> trajectory;

    public FaultyExecution(List<String> faults, List<List<String>> trajectory) {
        this.faults = faults;
        this.trajectory = trajectory;
    }
}

package gdemas;

public class Pair {
    private final int agentNum;
    private final int localDiagnosesNum;

    public Pair(int agentNum, int localDiagnosesNum) {
        this.agentNum = agentNum;
        this.localDiagnosesNum = localDiagnosesNum;
    }

    public int getAgentNum() {
        return agentNum;
    }

    public int getLocalDiagnosesNum() {
        return localDiagnosesNum;
    }
}

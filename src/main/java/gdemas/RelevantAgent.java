package gdemas;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static gdemas.Utils.print;

public class RelevantAgent {

    private final int qA;
    private final List<RelevantAction> relevantActions;

    public RelevantAgent (int A) {
        this.qA = A;
        this.relevantActions = new ArrayList<>();
    }

    public void insertRelevantAction(RelevantAction ra) {
        if (!this.relevantActions.contains(ra)) {
            this.relevantActions.add(ra);
            ra.insertRelevantAgent(this);
        }
    }

    public int getQA () {
        return this.qA;
    }

    public BigInteger getPossibleDiagnosesNum() {
        BigInteger possibleDiagnoses = new BigInteger("1");
        for (RelevantAction ra : this.relevantActions) {
            possibleDiagnoses = possibleDiagnoses.multiply(new BigInteger(String.valueOf(ra.getPossibleValues().size())));
        }
        return possibleDiagnoses;
    }

    public List<RelevantAction> getRelevantActions () {
        return this.relevantActions;
    }
}

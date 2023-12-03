package gdemas;

import java.util.ArrayList;
import java.util.List;

public class Action {
    public int actionOriginalStep;
    public String actionString;
    public String preconditions;
    public String effects;

    public List<String> effectedPredicates;

    public List<Action> effectedActions;
    public List<Action> effectingActions;

    public Action(int actionOriginalStep, String actionString, String preconditions, String effects, List<String> effectedPredicates) {
        this.actionOriginalStep = actionOriginalStep;
        this.actionString = actionString;
        this.preconditions = preconditions;
        this.effects = effects;
        this.effectedPredicates = effectedPredicates;

        this.effectedActions = new ArrayList<>();
        this.effectingActions = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.actionString;
    }
}

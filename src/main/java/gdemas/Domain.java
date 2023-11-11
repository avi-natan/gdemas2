package gdemas;

import java.util.List;
import java.util.Map;

public class Domain {
    public String                               name;
    public List<String>                         requirements;
    public List<String>                         types;
    public List<String>                         predicates;
    public Map<String, Map<String, String>>     actions;

    public Domain(String                            name,
                  List<String>                      requirements,
                  List<String>                      types,
                  List<String>                      predicates,
                  Map<String, Map<String, String>>  actions) {
        this.name           = name;
        this.requirements   = requirements;
        this.types          = types;
        this.predicates     = predicates;
        this.actions        = actions;
    }
}
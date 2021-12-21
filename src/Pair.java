import java.util.HashMap;

public class Pair {
    Variable var;
    HashMap<VarState, Boolean> domain;

    public Pair(Variable var, HashMap<VarState, Boolean> domain) {
        this.var = var;
        this.domain = domain;
    }

    @Override
    public String toString() {
        return var.row + ", " + var.col + ": " + var.value + "\t| " + domain;
    }
}

import java.util.HashMap;
import java.util.HashSet;

public class Pair {
    Variable var;
    HashSet<VarState> domain;

    public Pair(Variable var, HashSet<VarState> domain) {
        this.var = var;
        this.domain = domain;
    }

    @Override
    public String toString() {
        return var.row + ", " + var.col + ": " + var.value + "\t| " + domain;
    }
}

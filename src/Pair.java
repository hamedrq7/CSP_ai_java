import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class Pair {
    Variable var;
    TreeSet<VarState> domain;

    public Pair(Variable var, TreeSet<VarState> domain) {
        this.var = var;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object obj) {
        Pair p = (Pair) obj;
        if(p.var.row==this.var.row && p.var.col==this.var.col) return true;
        else return false;
    }

    @Override
    public String toString() {
        return var.row + ", " + var.col + ": " + var.value + "\t| " + domain;
    }
}

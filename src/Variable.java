import java.util.*;

public class Variable {
    VarState value;
    TreeSet<VarState> domain;

    //
    int row, col;

    public Variable(int row, int col, TreeSet<VarState> domain) {
        this.row = row;
        this.col = col;
        this.domain = domain;
        this.value = VarState.notInit;
    }

    public ArrayList<Variable> getNeighbours(Csp csp) {
        ArrayList<Variable> result = new ArrayList<>();
        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                if((x==0 && y==0) || x*y!=0)  continue;
                if(row+x < csp.n && row+x >= 0 && col+y < csp.m && col+y >= 0) {
                    result.add(csp.vars[row+x][col+y]);
                }
            }
        }
        return result;
    }

    public boolean isConsistent(Csp csp) {
        // this function is written based on the structure of problem
        // (based in constraints of the given problem)

        if(this.value == VarState.notInit) {
            System.out.println("Checking consistency for a variable that is not assigned!");
            System.exit(2);
        }

        //----------------------------------------------- pairing constraint
        boolean pairCon = isPairConsistent(csp);

        //----------------------------------------------- همنامی و غیر همنامی
        boolean poleCon = isPoleConsistent(csp);

        //-----------------------------checking col_pos/neg and row_pos/neg
        boolean rowColCon = isRowColConsistent(csp);
        return (pairCon && poleCon && rowColCon);
    }

    public boolean isPairConsistent(Csp csp) {
        Variable pair = csp.getPair(this);
        if(pair.value != VarState.notInit) {
            if (this.value == VarState.empty) {
                if (pair.value != VarState.empty) return false;
            } else if (this.value == VarState.pos) {
                if (pair.value != VarState.neg) return false;
            } else if (this.value == VarState.neg) {
                if (pair.value != VarState.pos) return false;
            }
        }
        else {
            // pair is not initialized

            //------this part is for when you want to assign the pair as well
            /*
            //System.out.println("row, col, n_x, n_y: " + row + ", " + col + ", " + n_x + ", " + n_y);
            if (this.value == VarState.empty) {
                pair.value = VarState.empty;
            } else if (this.value == VarState.pos) {
                pair.value = VarState.neg;
            } else if (this.value == VarState.neg) {
                pair.value = VarState.pos;
            }
            */
        }
        return true;
    }

    public boolean isPoleConsistent(Csp csp) {
        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                if((x==0 && y==0) || x*y!=0)  continue;
                if(row+x < csp.n && row+x >= 0 && col+y < csp.m && col+y >= 0) {
                    VarState neighbourState = csp.vars[row+x][col+y].value;
                    if(this.value == VarState.pos && neighbourState == VarState.pos) return false;
                    if(this.value == VarState.neg && neighbourState == VarState.neg) return false;
                }
            }
        }
        return true;
    }

    // you can make this function to check for stronger inconsistencies
    public boolean isRowColConsistent(Csp csp) {
        // return false scenarios:
        //      if all vars in the col/row are initialized and col/row pos/neg constraints are not satisfied
        //      if current number of pos/neg exceeds the constraints on row/col
        // basically, we only return false if we know there is failure OR there is gonna be failure based on
        // the current assignment of current variable (not based on condition of unassigned variables)

        int col_pos_count = 0;
        int col_neg_count = 0;
        int row_pos_count = 0;
        int row_neg_count = 0;

        boolean col_allInitialized = true;
        boolean row_allInitialized = true;

        // in the col
        for(int i = 0; i < csp.n; i++) {
            //col = this.col
            if(csp.vars[i][this.col].value == VarState.notInit) col_allInitialized = false;
            else if(csp.vars[i][this.col].value == VarState.pos) col_pos_count++;
            else if(csp.vars[i][this.col].value == VarState.neg) col_neg_count++;
        }
        // in the row
        for(int j = 0; j < csp.m; j++) {
            //row = this.row
            if(csp.vars[this.row][j].value == VarState.notInit) row_allInitialized = false;
            else if(csp.vars[this.row][j].value == VarState.pos) row_pos_count++;
            else if(csp.vars[this.row][j].value == VarState.neg) row_neg_count++;
        }

        if(col_allInitialized) {
            if(col_pos_count != csp.col_pos[this.col] || col_neg_count != csp.col_neg[this.col]) return false;
        }
        else {
            if(col_pos_count > csp.col_pos[this.col] || col_neg_count > csp.col_neg[this.col]) return false;
        }

        if(row_allInitialized) {
            if(row_pos_count != csp.row_pos[this.row] || row_neg_count != csp.row_neg[this.row]) return false;
        }
        else {
            if(row_pos_count > csp.row_pos[this.row] || row_neg_count > csp.row_neg[this.row]) return false;
        }

        return true;
    }

    public String domainToString() {

        StringBuilder sb = new StringBuilder();
        if(domain.contains(VarState.empty)) sb.append('0');
        else sb.append(' ');
        if(domain.contains(VarState.pos)) sb.append('+');
        else sb.append(' ');
        if(domain.contains(VarState.neg)) sb.append('-');
        else sb.append(' ');

        if(this.value==VarState.notInit) return sb.toString();
        else return "|" + this.value.toString() + "|";
        //return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        Variable v = (Variable) obj;
        if(this.row == v.row && this.col == v.col) return true;
        else return false;
    }
}

enum VarState {
    notInit {
        @Override
        public String toString() {
            return "X";
        }
    },
    empty {
        @Override
        public String toString() {
            return "0";
        }
    },
    pos{
        @Override
        public String toString() {
            return "+";
        }
    },
    neg {
        @Override
        public String toString() {
            return "-";
        }
    }
}
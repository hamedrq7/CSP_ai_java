import java.util.HashMap;

public class Variable {
    VarState value;
    HashMap<VarState, Boolean> domain;


    int row, col;

    public Variable(int row, int col, HashMap<VarState, Boolean> domain) {
        this.row = row;
        this.col = col;
        this.domain = domain;
        this.value = VarState.notInit;
    }

    public boolean isConsistent(Csp csp) {
        // this function is written based on the structure of problem
        // (based in constraints of the given problem)

        if(this.value == VarState.notInit) {
            System.out.println("Checking consistency for a variable that is not assigned!");
            System.exit(2);
        }

        //----------------------------------------------- pairing constraint
        for(CellPair x : csp.pairs) {
            boolean foundPair = false;
            VarState pairState = null;

            //---
            int n_x = 0, n_y = 0;

            if( (x.x1 == row && x.y1 == col) ) {
                //x2, y2 is neighbour
                n_x = x.x2; n_y = x.y2;
                pairState = csp.vars[x.x2][x.y2].value;
                foundPair = true;
            }
            else if  ( (x.x2 == row && x.y2 == col) ) {
                //x1, y1 is neighbour
                n_x = x.x1; n_y = x.y1;
                pairState = csp.vars[x.x1][x.y1].value;
                foundPair = true;
            }

            if(pairState != VarState.notInit) {
                if (foundPair) {

                    if (csp.vars[row][col].value == VarState.empty) {
                        if (pairState != VarState.empty) return false;
                    } else if (csp.vars[row][col].value == VarState.pos) {
                        if (pairState != VarState.neg) return false;
                    } else if (csp.vars[row][col].value == VarState.neg) {
                        if (pairState != VarState.pos) return false;
                    }

                    break;
                }
            }
            //-------------------------------------------------------------cheating
            else {
                // doesnt work if you uncomment, why ?  ? ? ?? ? ? ? ? ? ? ? ?? ? ? ? ? ? ? ?????
                /*
                if(foundPair) {
                    //System.out.println("row, col, n_x, n_y: " + row + ", " + col + ", " + n_x + ", " + n_y);
                    if (csp.vars[row][col].value == VarState.empty) {
                        csp.vars[n_x][n_y].value = VarState.empty;
                    } else if (csp.vars[row][col].value == VarState.pos) {
                        csp.vars[n_x][n_y].value = VarState.neg;
                    } else if (csp.vars[row][col].value == VarState.neg) {
                        csp.vars[n_x][n_y].value = VarState.pos;
                    }
                }
                break;
                */
            }

        }


        //----------------------------------------------- همنامی و غیر همنامی
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

        //-----------------------------checking col_pos/neg and row_pos/neg

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

}

enum VarState {
    notInit,
    empty,
    pos,
    neg
}
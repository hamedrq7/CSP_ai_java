import java.util.*;

//this class is written based on structure of problem
class CellPair {
    Variable x, y;
    public CellPair(Variable x, Variable y) {
        this.x = x;
        this.y = y;
    }
    int x1, y1;
    int x2, y2;
    public CellPair(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int row = x.row;
        int col = x.col;
        int result = 1;
        result = prime * result + col;
        result = prime * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        CellPair p = (CellPair) obj;
        // Xi, Xj equals Xi, Xj
        if(this.x.equals(p.x) && this.y.equals(p.y)) {
            return true;
        }

        //************
        // in AC-3, every binary constraint becomes 2 arcs, one in each direction
        // so Xi, Xj NOT equals Xj, Xi

        else return false;
    }
}
public class Csp {
    int n, m;

    ArrayList<CellPair> pairs = new ArrayList<>();

    Variable[][] vars;
    int[] col_pos, col_neg, row_pos,row_neg;

    public Csp(int n, int m, int[] col_pos, int[] col_neg, int[] row_pos, int[] row_neg, int[][] board) {
        this.n = n;
        this.m = m;
        this.col_pos = col_pos;
        this.col_neg = col_neg;
        this.row_pos = row_pos;
        this.row_neg = row_neg;

        this.vars = new Variable[n][m];

        //build vars
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                TreeSet<VarState> initialDomain = new TreeSet<VarState>();

                initialDomain.add(VarState.empty);
                initialDomain.add(VarState.neg);
                initialDomain.add(VarState.pos);

                vars[i][j] = new Variable(i, j, initialDomain);
            }
        }

        //build pairs
        for(int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                boolean found = false;
                for (int k = 0; k < n; k++) {
                    for (int l = 0; l < m; l++) {
                        if (i == k && l == j) continue;
                        if (board[i][j] == board[k][l]) {
                            found = true;
                            // produces double the size of actual cell pair array
                            pairs.add(new CellPair(i, j, k, l));
                        }
                    }
                }
                if (!found) {
                    System.out.println("didnt found pair! wrong input");
                    System.exit(3);
                }
            }
        }
    }

    public static Boolean deleteFromDomainAndAddToOldDomain(Csp csp, Variable varToDelete, VarState stateToDelete, ArrayList<Pair> oldDomains) {
        if(varToDelete.domain.contains(stateToDelete)) {
            boolean alreadyAdded = false;
            for(Pair p : oldDomains){
                if(p.var.row==varToDelete.row && p.var.col==varToDelete.col) alreadyAdded = true;
            }
            if(!alreadyAdded)
                oldDomains.add(new Pair(varToDelete, (TreeSet<VarState>) varToDelete.domain.clone()));
            varToDelete.domain.remove(stateToDelete);
            return true;
        } else return false;
    }
    public static void pairForwardChecking(Csp csp, Variable newAssignedVar, ArrayList<Pair> oldDomains) {
        Variable pairVar = csp.getPair(newAssignedVar);
        oldDomains.add(new Pair(pairVar, (TreeSet<VarState>) pairVar.domain.clone()));
        //if it is unassigned:
        if(pairVar.value==VarState.notInit) {
            ArrayList<VarState> listToRemove = new ArrayList<>();
            for(VarState var : pairVar.domain) {
                //assign
                pairVar.value = var;

                //check pairVar con
                //// only check pair consistency, because we are checking binary constraint between 2 vars
                if(!pairVar.isPairConsistent(csp)) {
                    //pairVar.domain.remove(var);
                    listToRemove.add(var);
                }
                //undo assignment
                pairVar.value = VarState.notInit;
            }
            pairVar.domain.removeAll(listToRemove);
        }
    }
    public static void poleForwardChecking(Csp csp, Variable newAssignedVar, ArrayList<Pair> oldDomains) {
        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                if((x==0 && y==0) || x*y!=0)  continue;
                if(newAssignedVar.row+x < csp.n && newAssignedVar.row+x >= 0
                        && newAssignedVar.col+y < csp.m && newAssignedVar.col+y >= 0) {

                    int i = newAssignedVar.row+x;
                    int j = newAssignedVar.col+y;

                    //update domain
                    if(csp.vars[i][j].value==VarState.notInit) {

                        boolean alreadyAdded = false;
                        for(Pair p : oldDomains) if(p.var.row==i && p.var.col==j) alreadyAdded = true;
                        if(!alreadyAdded) oldDomains.add(new Pair(csp.vars[i][j], (TreeSet<VarState>) csp.vars[i][j].domain.clone()));

                        if(newAssignedVar.value==VarState.pos) {
                            //delete pos from i j's domain
                            if(csp.vars[i][j].domain.contains(VarState.pos)) csp.vars[i][j].domain.remove(VarState.pos);
                        }
                        else if(newAssignedVar.value==VarState.neg) {
                            if(csp.vars[i][j].domain.contains(VarState.neg)) csp.vars[i][j].domain.remove(VarState.neg);
                        }
                    }
                }
            }
        }
    }
    public static ArrayList<Pair> abnormalForwardChecking(Csp csp, Variable newAssignedVar) {
        ArrayList<Pair> oldDomains = new ArrayList<>();

        //--> update domain of every unassigned variable that is in constraint with "newAssignedVar"

        //question, which variables does the newAssignedVar effect?
        //      all vars in its up, down, left and right:
        //          newAssignedVar.value != {up, down, left, right}

        //      the pairVar value:
        //          value of pairVar should be equal to newAssignedVar.value (reduces domain size to 1)

        //*******************************************************
        //      all vars in its column
        //      all vars in its row
        //*******************************************************

        //------------------------------------pairVar value consistency:
        pairForwardChecking(csp, newAssignedVar, oldDomains);

        //------------------------------------pole consistency (up, down, right and left)
        poleForwardChecking(csp, newAssignedVar, oldDomains);

        //------------------------------------------------------------row consistency
        // if initialized variables pos/neg already satisfies row pos/neg constraints,
        // delete pos/neg from unassigned variables domain in that row
        // also if number of remaining not init variables equals (POS/NEG row/col constraint - #initialized Pos/Neg)
        // it means that remaining not init variables must not be empty, so delete empty from their domain
        int row_pos_count = 0, row_neg_count = 0, row_not_init_count = 0;
        for(int j = 0; j < csp.m; j++) {
            if(csp.vars[newAssignedVar.row][j].value==VarState.pos) row_pos_count++;
            if(csp.vars[newAssignedVar.row][j].value==VarState.neg) row_neg_count++;
            if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) row_not_init_count++;
        }
        if(row_pos_count == csp.row_pos[newAssignedVar.row]) {
            //delete pos from not-init variables
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) {
                    boolean temp = deleteFromDomainAndAddToOldDomain(csp, csp.vars[newAssignedVar.row][j], VarState.pos, oldDomains);
                }
            }
        }
        else if(row_pos_count > csp.row_pos[newAssignedVar.row]) {
            // should not happen based on implementation of backtrack and FC
            // but implementing for good practice
            //System.out.println("ER 77");
            //System.exit(77);
        }
        else {
            //row_pos_count < csp.row_pos[newAssignedVar.row]
            // if csp.row_pos[newAssignedVar.row] - row_pos_count == #not initialized vars
            //  then, all the not initialized var must be POSITIVE, so we need to delete NEGATIVE and EMPTY from their domain
            if(csp.row_pos[newAssignedVar.row] - row_pos_count == row_not_init_count) {
                for(int j = 0; j < csp.m; j++) {
                    if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) {
                        boolean temp1 = deleteFromDomainAndAddToOldDomain(csp, csp.vars[newAssignedVar.row][j], VarState.neg, oldDomains);
                        boolean temp2 = deleteFromDomainAndAddToOldDomain(csp, csp.vars[newAssignedVar.row][j], VarState.empty, oldDomains);
                    }
                }
            }
        }


        //row neg
        if(row_neg_count == csp.row_neg[newAssignedVar.row]) {
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) {
                    boolean temp = deleteFromDomainAndAddToOldDomain(csp, csp.vars[newAssignedVar.row][j], VarState.neg, oldDomains);
                }
            }
        }
        else if(row_neg_count > csp.row_neg[newAssignedVar.row]) {
            // should not happen based on implementation of backtrack and FC
            // but implementing for good practice
            //System.out.println("ER 77");
            //System.exit(77);
        }
        else {
            //row_neg_count < csp.row_neg[newAssignedVar.row]

            // if csp.row_neg[newAssignedVar.row] - row_neg_count == #not initialized vars
            //  then, all the not initialized var must be neg, so we need to delete POS and EMPTY from their domain
            if(csp.row_neg[newAssignedVar.row] - row_neg_count == row_not_init_count) {
                for(int j = 0; j < csp.m; j++) {
                    if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) {
                        boolean temp1 = deleteFromDomainAndAddToOldDomain(csp, csp.vars[newAssignedVar.row][j], VarState.pos, oldDomains);
                        boolean temp2 = deleteFromDomainAndAddToOldDomain(csp, csp.vars[newAssignedVar.row][j], VarState.empty, oldDomains);
                    }
                }
            }
        }

        //--------col consistency
        // same logic as row consistency, but in the column
        int col_pos_count = 0, col_neg_count = 0, col_not_init_count = 0;
        for(int i = 0; i < csp.n; i++) {
            if(csp.vars[i][newAssignedVar.col].value==VarState.pos) col_pos_count++;
            if(csp.vars[i][newAssignedVar.col].value==VarState.neg) col_neg_count++;
            if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) col_not_init_count++;
        }
        if(col_pos_count == csp.col_pos[newAssignedVar.col]) {
            for(int i = 0; i < csp.n; i++) {
                if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) {
                    boolean temp = deleteFromDomainAndAddToOldDomain(csp, csp.vars[i][newAssignedVar.col], VarState.pos, oldDomains);
                }
            }
        }
        else if(col_pos_count > csp.col_pos[newAssignedVar.col]) {
            // should not happen based on implementation of backtrack and FC
            // but implementing for good practice
            //System.out.println("ER 77");
            //System.exit(77);
        }
        else {
            //col_pos_count < csp.col_pos[newAssignedVar.col]

            // if csp.col_pos[newAssignedVar.col] - col_pos_count == #not initialized vars
            //  then, all the not initialized var must be POSITIVE, so we need to delete NEGATIVE and EMPTY from their domain
            if(csp.col_pos[newAssignedVar.col] - col_pos_count == col_not_init_count) {
                for(int i = 0; i < csp.n; i++) {
                    if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) {
                        boolean temp1 = deleteFromDomainAndAddToOldDomain(csp, csp.vars[i][newAssignedVar.col], VarState.neg, oldDomains);
                        boolean temp2 = deleteFromDomainAndAddToOldDomain(csp, csp.vars[i][newAssignedVar.col], VarState.empty, oldDomains);
                    }
                }
            }
        }

        // col neg
        if(col_neg_count == csp.col_neg[newAssignedVar.col]) {
            for(int i = 0; i < csp.n; i++) {
                if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) {
                    boolean temp = deleteFromDomainAndAddToOldDomain(csp, csp.vars[i][newAssignedVar.col], VarState.neg, oldDomains);
                }
            }
        }
        else if(col_neg_count > csp.col_neg[newAssignedVar.col]) {
            // should not happen based on implementation of backtrack and FC
            // but implementing for good practice
            //System.out.println("ER 77");
            //System.exit(77);
        }
        else {
            //col_neg_count < csp.col_neg[newAssignedVar.col]

            // if csp.col_neg[newAssignedVar.col] - col_neg_count == #not initialized vars
            //  then, all the not initialized var must be NEG, so we need to delete POS and EMPTY from their domain
            if(csp.col_neg[newAssignedVar.col] - col_neg_count == col_not_init_count) {

                for(int i = 0; i < csp.n; i++) {
                    if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) {
                        boolean temp1 = deleteFromDomainAndAddToOldDomain(csp, csp.vars[i][newAssignedVar.col], VarState.pos, oldDomains);
                        boolean temp2 = deleteFromDomainAndAddToOldDomain(csp, csp.vars[i][newAssignedVar.col], VarState.empty, oldDomains);
                    }
                }
            }
        }
        return oldDomains;
    }
    public static ArrayList<Pair> ForwardChecking(Csp csp, Variable newAssignedVar) {
        ArrayList<Pair> oldDomains = new ArrayList<>();
        //------------------------------------pairVar value consistency:
        pairForwardChecking(csp, newAssignedVar, oldDomains);

        //------------------------------------pole consistency (up, down, right and left)
        poleForwardChecking(csp, newAssignedVar, oldDomains);

        //--------row consistency
        int row_pos_count = 0, row_neg_count = 0;
        for(int j = 0; j < csp.m; j++) {
            if(csp.vars[newAssignedVar.row][j].value==VarState.pos) row_pos_count++;
            if(csp.vars[newAssignedVar.row][j].value==VarState.neg) row_neg_count++;
        }
        if(row_pos_count == csp.row_pos[newAssignedVar.row]) {
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) {
                    boolean temp = deleteFromDomainAndAddToOldDomain(csp, csp.vars[newAssignedVar.row][j], VarState.pos, oldDomains);
                }
            }
        }
        if(row_neg_count == csp.row_neg[newAssignedVar.row]) {
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) {
                    boolean temp = deleteFromDomainAndAddToOldDomain(csp, csp.vars[newAssignedVar.row][j], VarState.neg, oldDomains);
                }
            }
        }

        //--------col consistency
        int col_pos_count = 0, col_neg_count = 0;
        for(int i = 0; i < csp.n; i++) {
            if(csp.vars[i][newAssignedVar.col].value==VarState.pos) col_pos_count++;
            if(csp.vars[i][newAssignedVar.col].value==VarState.neg) col_neg_count++;
        }
        if(col_pos_count == csp.col_pos[newAssignedVar.col]) {
            for(int i = 0; i < csp.n; i++) {
                if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) {
                    boolean temp = deleteFromDomainAndAddToOldDomain(csp, csp.vars[i][newAssignedVar.col], VarState.pos, oldDomains);
                }
            }
        }
        if(col_neg_count == csp.col_neg[newAssignedVar.col]) {
            for(int i = 0; i < csp.n; i++) {
                if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) {
                    boolean temp = deleteFromDomainAndAddToOldDomain(csp, csp.vars[i][newAssignedVar.col], VarState.neg, oldDomains);
                }
            }
        }

        return oldDomains;
    }
    public static ArrayList<Pair> BinaryForwardChecking(Csp csp, Variable newAssignedVar) {
        ArrayList<Pair> oldDomains = new ArrayList<>();

        //------------------------------------pairVar value consistency:
        pairForwardChecking(csp, newAssignedVar, oldDomains);

        //------------------------------------pole consistency (up, down, right and left)
        poleForwardChecking(csp, newAssignedVar, oldDomains);

        return oldDomains;
    }


    public boolean isComplete() {
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                if(this.vars[i][j].value==VarState.notInit) {
                    return false;
                }
            }
        }
        return true;
    }

    public Variable getPair(Variable var) {
        for(CellPair x : this.pairs) {

            if( (x.x1 == var.row && x.y1 == var.col) ) {
                //x2, y2 is neighbour
                return this.vars[x.x2][x.y2];
            }
            else if  ( (x.x2 == var.row && x.y2 == var.col) ) {
                //x1, y1 is neighbour
                return this.vars[x.x1][x.y1];
            }
        }

        System.out.println("Did not found pair for Variable " + var.row +", "+ var.col);
        return null;
    }

    public void printBoard(int x, int y) {
        System.out.println("-------------------");
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {

                if(i == x && j == y) System.out.print(" (");
                else {System.out.print("  ");}

                if(this.vars[i][j].value == VarState.notInit) {
                    System.out.print("x");
                }
                else if(this.vars[i][j].value == VarState.empty) {
                    System.out.print("0");

                }
                else if(this.vars[i][j].value == VarState.pos) {
                    System.out.print("+");

                }
                else if(this.vars[i][j].value == VarState.neg) {
                    System.out.print("-");

                }
                else {
                    System.exit(69);
                }

                if(i==x && j ==y) System.out.print(") ");
                else System.out.print("  ");
            }
            System.out.println();
        }
        System.out.println("-------------------");
    }

    public void printDomains(int x, int y) {
        System.out.println("-----Domains-----");
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                if(i== x && j == y) {
                    System.out.print(" ( ");
                    if(this.vars[i][j].value == VarState.empty) {
                        System.out.print(" 0 ");

                    }
                    else if(this.vars[i][j].value == VarState.pos) {
                        System.out.print(" + ");

                    }
                    else if(this.vars[i][j].value == VarState.neg) {
                        System.out.print(" - ");

                    }
                    System.out.print(" ) ");
                }
                else {
                    System.out.print("   ");
                    System.out.print(vars[i][j].domainToString());
                    System.out.print("   ");
                }
            }
            System.out.println();
        }
        System.out.println("-----Domain-----");
    }
}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

//this class is written based on structure of problem
class CellPair {
    int x1, y1;
    int x2, y2;
    public CellPair(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
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
                HashSet<VarState> initialDomain = new HashSet<>();

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
                else System.out.print("  ");

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

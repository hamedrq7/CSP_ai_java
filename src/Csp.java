import java.util.ArrayList;
import java.util.HashMap;

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
                HashMap<VarState, Boolean> initialDomain = new HashMap<>();
                initialDomain.put(VarState.empty, true);
                initialDomain.put(VarState.neg, true);
                initialDomain.put(VarState.pos, true);

                vars[i][j] = new Variable(i, j, initialDomain);
            }
        }

        //build pairs
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                //
                boolean found = false;
                for(int k = 0; k < n; k++) {
                    for(int l = 0; l < m; l++) {
                        if(i==k && l==j) continue;
                        if(board[i][j] == board[k][l]) {
                            found = true;
                            // produces double the size of actual cell pair array
                            pairs.add(new CellPair(i, j, k, l));
                        }
                    }
                }
                if(!found) {
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

}

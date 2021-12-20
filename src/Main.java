import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        int n = 0, m=0;
        int[] col_pos = new int[0], col_neg = new int[0];
        int[] row_pos = new int[0], row_neg = new int[0];
        int[][] board = new int[0][];
        
        try {
            File myObj = new File("tests\\test2.txt");
            //System.out.println(myObj.exists());
            Scanner scanner = new Scanner(myObj);

            n = scanner.nextInt();
            m = scanner.nextInt();

            col_pos = new int[m];
            col_neg = new int[m];

            row_pos = new int[n];
            row_neg = new int[n];

            for(int i = 0; i < n; i++) row_pos[i] =  scanner.nextInt();
            for(int i = 0; i < n; i++) row_neg[i] =  scanner.nextInt();

            for(int i = 0; i < m; i++) col_pos[i] =  scanner.nextInt();
            for(int i = 0; i < m; i++) col_neg[i] =  scanner.nextInt();

            board = new int[n][m];
            for(int i = 0; i < n; i++)
                for(int j = 0; j < m; j++)
                    board[i][j] = scanner.nextInt();


            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        /*for(int i = 0; i < m; i++) {
            System.out.print(col_pos[i] + " ");
        }
        System.out.println();
        for(int i = 0; i < m; i++) {
            System.out.print(col_neg[i] + " ");
        }
        System.out.println();

        for(int i = 0; i < n; i++) {
            System.out.print(row_pos[i] + " ");
        }
        System.out.println();
        for(int i = 0; i < n; i++) {
            System.out.print(row_neg[i] + " ");
        }
        System.out.println();

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                System.out.print(board[i][j] + " ");

            }
            System.out.println();
        }
        System.out.println();*/

        Csp csp = new Csp(n, m, col_pos, col_neg, row_pos, row_neg, board);
        //for (CellPair x : csp.pairs) {
        //    System.out.println(x.x1 + ", " + x.y1 + ", " + x.x2 + ", " + x.y2);
        //}
        backtrack_solve(csp);
        //csp.printBoard();

    }


    public static void backtrack_solve(Csp csp) {
        boolean temp = recursive(csp);
        System.out.println(temp);
    }
    public static boolean recursive(Csp csp) {
        if(csp.isComplete()) {
            System.out.println("assignment is complete");
            csp.printBoard(-1, -1);
            return true;
        }
        
        //ADD -> select a var based on MRV

        //right now selection is based on index
        Variable varToAssign = null;
        for(int i = 0; i < csp.n; i++) {
            boolean stop = false;
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[i][j].value == VarState.notInit) {
                    stop = true;
                    varToAssign = csp.vars[i][j];
                    break;
                }
            }
            if(stop) break;
        }
        
        //ADD -> sort(Choose value) based on LCS


        //right now its index based
        for (Map.Entry<VarState, Boolean> entry : varToAssign.domain.entrySet()) {
            //if it is in the domain
            if(varToAssign.domain.get(entry.getKey())) {

                //assign
                csp.vars[varToAssign.row][varToAssign.col].value = entry.getKey();

                //check if consistent
                // only expand if its consistent
                if(csp.vars[varToAssign.row][varToAssign.col].isConsistent(csp)) {
                    csp.printBoard(varToAssign.row, varToAssign.col);
                    System.out.println("up consistent");
                    boolean result = recursive(csp);
                    if(result) return true;
                }

                //remove from assignment
                //System.out.println("inconsistent");
                csp.vars[varToAssign.row][varToAssign.col].value = VarState.notInit;

            }
        }

        return false;
    }
}

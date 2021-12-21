import com.sun.source.tree.VariableTree;

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
        long startTime = System.nanoTime();

        backtrack_solve_inference(csp);

        long stopTime = System.nanoTime();
        System.out.println(stopTime - startTime);
        //csp.printBoard();

    }


    public static void backtrack_solve_inference(Csp csp) {
        boolean temp = recursive_with_inference(csp);
        System.out.println(temp);
    }
    public static boolean recursive_with_inference(Csp csp) {
        if(csp.isComplete()) {
            System.out.println("assignment is complete");
            csp.printBoard( -1, -1);
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

                //Assign
                csp.vars[varToAssign.row][varToAssign.col].value = entry.getKey();
                // Forward Checking for the new assignment (UPDATE DOMAIN FOR UNASSIGNED VARIABLES)
                ForwardChecking(csp, varToAssign);

                //check if consistent
                // only expand if its consistent
                if(csp.vars[varToAssign.row][varToAssign.col].isConsistent(csp)) {
                    csp.printBoard(varToAssign.row, varToAssign.col);
                    System.out.println("up consistent");
                    boolean result = recursive(csp);
                    if(result) return true;
                }
                else {
                    csp.printBoard(varToAssign.row, varToAssign.col);
                    System.out.println("up in-consistent");
                }

                //UNDO FORWARD CHECKING
                //remove from assignment
                csp.vars[varToAssign.row][varToAssign.col].value = VarState.notInit;
                //-------- if you assign the pair as well, you have to undo pair assignment too
                //csp.vars[csp.getPair(varToAssign).row][csp.getPair(varToAssign).col].value = VarState.notInit;
            }
        }

        return false;
    }

    public static void ForwardChecking(Csp csp, Variable newAssignedVar) {
        // 1 easy way (laziest way) is to iterate over all values in domain of all unassigned variable,
        // check if that value is inconsistent or consistent, if it is inconsistent, delete it from that variable's domain
        /*
        for(int i = 0; i < csp.n; i++) {
            for (int j = 0; j < csp.m; j++) {
                //un assigned
                if(csp.vars[i][j].value==VarState.notInit) {
                    for (Map.Entry<VarState, Boolean> entry : csp.vars[i][j].domain.entrySet()) {
                        if (csp.vars[i][j].domain.get(entry.getKey())==true) {
                            //assign
                            csp.vars[i][j].value = entry.getKey();
                            if (!csp.vars[i][j].isConsistent(csp)) {
                                //update domain
                                csp.vars[i][j].domain.replace(entry.getKey(), true, false);
                            }
                            //undo the assignment
                            csp.vars[i][j].value = VarState.notInit;
                        }
                    }
                }

            }
        }
        csp.printDomains(varToAssign.row, varToAssign.col);
        */

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
        Variable pairVar = csp.getPair(newAssignedVar);
        //if it is unassigned:
        if(pairVar.value==VarState.notInit) {
            for (Map.Entry<VarState, Boolean> entry : pairVar.domain.entrySet()) {
                if(pairVar.domain.get(entry.getKey())==true) {
                    //assign
                    pairVar.value = entry.getKey();
                    //check pairVar con
                    if(!pairVar.isPairConsistent(csp)) {
                        pairVar.domain.replace(entry.getKey(), true, false);
                    }
                    //undo assignment
                    pairVar.value = VarState.notInit;
                }
            }
        }

        //------------------------------------pole consistency (up, down, right and left)
        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                if((x==0 && y==0) || x*y!=0)  continue;
                if(newAssignedVar.row+x < csp.n && newAssignedVar.row+x >= 0
                        && newAssignedVar.col+y < csp.m && newAssignedVar.col+y >= 0) {

                    int i = newAssignedVar.row+x;
                    int j = newAssignedVar.col+y;
                    //update domain
                    if(csp.vars[i][j].value==VarState.notInit) {
                        if(newAssignedVar.value==VarState.pos) {
                            //delete pos from i j's domain
                            if(csp.vars[i][j].domain.get(VarState.pos)==true) csp.vars[i][j].domain.replace(VarState.pos, true, false);
                        }
                        else if(newAssignedVar.value==VarState.neg) {
                            if(csp.vars[i][j].domain.get(VarState.neg)==true) csp.vars[i][j].domain.replace(VarState.neg, true, false);
                        }
                    }
                }
            }
        }

        csp.printDomains(newAssignedVar.row, newAssignedVar.col);
    }

    public static void backtrack_solve(Csp csp) {
        boolean temp = recursive(csp);
        System.out.println(temp);
    }

    public static boolean recursive(Csp csp) {
        //csp.printBoard(-1, -1);
        if(csp.isComplete()) {
            System.out.println("assignment is complete");
            csp.printBoard( -1, -1);
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
                    //csp.printBoard(varToAssign.row, varToAssign.col);
                    //System.out.println("up consistent");
                    boolean result = recursive(csp);
                    if(result) return true;
                }
                else {
                    //csp.printBoard(varToAssign.row, varToAssign.col);
                    //System.out.println("up in-consistent");
                }

                //remove from assignment
                csp.vars[varToAssign.row][varToAssign.col].value = VarState.notInit;
                //-------- if you assign the pair as well, you have to undo pair assignment too
                //csp.vars[csp.getPair(varToAssign).row][csp.getPair(varToAssign).col].value = VarState.notInit;
            }
        }

        return false;
    }
}

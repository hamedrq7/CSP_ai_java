import com.sun.source.tree.VariableTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    static int recursion_count_simple_backtracking = 0;
    static int recursion_count_fc = 0;

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

        Csp csp1 = new Csp(n, m, col_pos, col_neg, row_pos, row_neg, board);
        Csp csp2 = new Csp(n, m, col_pos, col_neg, row_pos, row_neg, board);
        Csp csp3 = new Csp(n, m, col_pos, col_neg, row_pos, row_neg, board);
        //for (CellPair x : csp.pairs) {
        //    System.out.println(x.x1 + ", " + x.y1 + ", " + x.x2 + ", " + x.y2);
        //}

        System.out.println("start");
        long startTime = System.nanoTime();
        backtrack_solve_inference(csp1);
        long stopTime = System.nanoTime();
        System.out.println("FC: " + (stopTime - startTime));

        long startTime2 = System.nanoTime();
        backtrack_solve(csp2);
        long stopTime2 = System.nanoTime();
        System.out.println("non FC: " + (stopTime2 - startTime2));

        System.out.println("#recursions without FC: " + recursion_count_simple_backtracking);
        System.out.println("#recursions with FC: " + recursion_count_fc);
        //csp.printBoard();

    }


    public static void backtrack_solve_inference(Csp csp) {
        boolean temp = recursive_with_inference(csp);
        System.out.println(temp);
    }
    public static boolean recursive_with_inference(Csp csp) {
        recursion_count_fc++;
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
                //      forward checking returns the changes in domains, so we can undo them
                ArrayList<Pair> oldDomains = ForwardChecking(csp, varToAssign);

                //check if consistent
                // only expand if its consistent
                if(csp.vars[varToAssign.row][varToAssign.col].isConsistent(csp)) {
                    //csp.printBoard(varToAssign.row, varToAssign.col);
                    //System.out.println("up consistent");
                    boolean result = recursive_with_inference(csp);
                    if(result) return true;
                }
                else {
                    //csp.printBoard(varToAssign.row, varToAssign.col);
                    //System.out.println("up in-consistent");
                }

                //UNDO FORWARD CHECKING
                //UndoForwardChecking(csp, varToAssign);
                for(Pair p : oldDomains) {
                    csp.vars[p.var.row][p.var.col].domain = p.domain;
                }

                //remove from assignment
                csp.vars[varToAssign.row][varToAssign.col].value = VarState.notInit;
                //-------- if you assign the pair as well, you have to undo pair assignment too
                //csp.vars[csp.getPair(varToAssign).row][csp.getPair(varToAssign).col].value = VarState.notInit;
            }
        }

        return false;
    }

    public static void UndoForwardChecking(Csp csp, Variable newAssignedVar) {
        //UNDO-------------------------------pairVar value consistency:
        Variable pairVar = csp.getPair(newAssignedVar);

        if(newAssignedVar.value==VarState.pos) {
            pairVar.domain.replace(VarState.pos, false, true);
            pairVar.domain.replace(VarState.empty, false, true);
        }
        else if(newAssignedVar.value==VarState.neg) {
            pairVar.domain.replace(VarState.neg, false, true);
            pairVar.domain.replace(VarState.empty, false, true);
        }

        //UNDO-----------------------------pole consistency (up, down, right and left)
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
                            csp.vars[i][j].domain.replace(VarState.pos, false, true);
                        }
                        else if(newAssignedVar.value==VarState.neg) {
                            csp.vars[i][j].domain.replace(VarState.neg, false, true);
                        }
                    }
                }
            }
        }

    }
    public static ArrayList<Pair> ForwardChecking(Csp csp, Variable newAssignedVar) {
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
        Variable pairVar = csp.getPair(newAssignedVar);

        oldDomains.add(new Pair(pairVar, (HashMap<VarState, Boolean>) pairVar.domain.clone()));

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

                        boolean alreadyAdded = false;
                        for(Pair p : oldDomains) if(p.var.row==i && p.var.col==j) alreadyAdded = true;
                        if(!alreadyAdded) oldDomains.add(new Pair(csp.vars[i][j], (HashMap<VarState, Boolean>) csp.vars[i][j].domain.clone()));

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

        //for(Pair p : oldDomains) {
        //    System.out.println(p);
        //}

        //csp.printDomains(newAssignedVar.row, newAssignedVar.col);

        return oldDomains;
    }

    public static void backtrack_solve(Csp csp) {
        boolean temp = recursive(csp);
        System.out.println(temp);
    }

    public static boolean recursive(Csp csp) {
        recursion_count_simple_backtracking++;
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

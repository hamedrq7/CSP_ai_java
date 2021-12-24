import com.sun.jdi.Value;
import com.sun.source.tree.VariableTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    static int recursion_count_simple_backtracking = 0;
    static int recursion_count_fc = 0;
    static int recursion_count_fc_mrv_lcv = 0;

    static int bfc = 0;
    static int fc = 0;

    public static void main(String[] args) {

        /*int nn = 10, mm = 10;
        System.out.println(nn + " " + mm);
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < mm; j++) {
                System.out.print(mm/2 + " ");
            }
            System.out.println();
        }
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < nn; j++) {
                System.out.print(nn/2 + " ");
            }
            System.out.println();
        }
        for(int i = 0; i < nn; i++) {
            for(int j = 0; j < mm; j++) {
                System.out.print(j+1+(mm*(i/2)) + " ");
            }
            System.out.println();
        }*/

        int n = 0, m=0;
        int[] col_pos = new int[0], col_neg = new int[0];
        int[] row_pos = new int[0], row_neg = new int[0];
        int[][] board = new int[0][];

        String testFileName = "tests\\test2.txt";
        try {
            File myObj = new File(testFileName);
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


        long startTime2 = System.nanoTime();
        //backtrack_solve(csp2);
        long stopTime2 = System.nanoTime();
        System.out.println("non FC: " + (stopTime2 - startTime2));

        System.out.println("start");
        long startTime = System.nanoTime();
        //backtrack_FC(csp1);
        long stopTime = System.nanoTime();
        System.out.println("FC: " + (stopTime - startTime));

        long startTime3 = System.nanoTime();
        backtrack_FC_MRV_LCV(csp3);
        long stopTime3 = System.nanoTime();
        System.out.println("FC MRV LCV: " + (stopTime3 - startTime3));

        System.out.println("#recursions without FC: " + recursion_count_simple_backtracking);
        System.out.println("#recursions with FC: " + recursion_count_fc);
        System.out.println("#recursions with FC MRV LCV: " + recursion_count_fc_mrv_lcv);

        System.out.println("fc: "+ fc);
        System.out.println("bfc: " + bfc);
        //csp.printBoard();

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

        oldDomains.add(new Pair(pairVar, (TreeSet<VarState>) pairVar.domain.clone()));

        //if it is unassigned:
        if(pairVar.value==VarState.notInit) {
            ArrayList<VarState> listToRemove = new ArrayList<>();
            for(VarState var : pairVar.domain) {
                //assign
                pairVar.value = var;

                //check pairVar con
                if(!pairVar.isPairConsistent(csp)) {
                    //pairVar.domain.remove(var);
                    listToRemove.add(var);
                }
                //undo assignment
                pairVar.value = VarState.notInit;
            }
            pairVar.domain.removeAll(listToRemove);
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

        //--------row consistency
        // if initialized variables pos/neg already satisfies row pos/neg constraints,
        // delete pos/neg from unassigned variables domain in that row
        boolean rowPosSatisfied = false;
        boolean rowNegSatisfied = false;
        int row_pos_count = 0, row_neg_count = 0;
        for(int j = 0; j < csp.m; j++) {
            if(csp.vars[newAssignedVar.row][j].value==VarState.pos) row_pos_count++;
            if(csp.vars[newAssignedVar.row][j].value==VarState.neg) row_neg_count++;
        }
        if(row_pos_count == csp.row_pos[newAssignedVar.row]) {
            rowPosSatisfied = true;
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) {
                    if(csp.vars[newAssignedVar.row][j].domain.contains(VarState.pos)) {
                        boolean alreadyAdded = false;
                        for(Pair p : oldDomains) if(p.var.row==newAssignedVar.row && p.var.col==j) alreadyAdded = true;
                        if(!alreadyAdded)
                            oldDomains.add(new Pair(csp.vars[newAssignedVar.row][j], (TreeSet<VarState>) csp.vars[newAssignedVar.row][j].domain.clone()));
                        csp.vars[newAssignedVar.row][j].domain.remove(VarState.pos);

                    }
                }
            }
        }
        if(row_neg_count == csp.row_neg[newAssignedVar.row]) {
            rowNegSatisfied = true;
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[newAssignedVar.row][j].value==VarState.notInit) {
                    if(csp.vars[newAssignedVar.row][j].domain.contains(VarState.neg)) {
                        boolean alreadyAdded = false;
                        for(Pair p : oldDomains) if(p.var.row==newAssignedVar.row && p.var.col==j) alreadyAdded = true;
                        if(!alreadyAdded)
                            oldDomains.add(new Pair(csp.vars[newAssignedVar.row][j], (TreeSet<VarState>) csp.vars[newAssignedVar.row][j].domain.clone()));
                        csp.vars[newAssignedVar.row][j].domain.remove(VarState.neg);
                    }
                }
            }
        }

        //--------col consistency
        // same logic as row consistency, but in the column
        boolean colPosSatisfied = false;
        boolean colNegSatisfied = false;
        int col_pos_count = 0, col_neg_count = 0;
        for(int i = 0; i < csp.n; i++) {
            if(csp.vars[i][newAssignedVar.col].value==VarState.pos) col_pos_count++;
            if(csp.vars[i][newAssignedVar.col].value==VarState.neg) col_neg_count++;
        }
        if(col_pos_count == csp.col_pos[newAssignedVar.col]) {
            colPosSatisfied = true;
            for(int i = 0; i < csp.n; i++) {
                if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) {
                    if(csp.vars[i][newAssignedVar.col].domain.contains(VarState.pos)) {
                        boolean alreadyAdded = false;
                        for(Pair p : oldDomains) if(p.var.row==i && p.var.col==newAssignedVar.col) alreadyAdded = true;
                        if(!alreadyAdded)
                            oldDomains.add(new Pair(csp.vars[i][newAssignedVar.col], (TreeSet<VarState>) csp.vars[i][newAssignedVar.col].domain.clone()));

                        csp.vars[i][newAssignedVar.col].domain.remove(VarState.pos);
                    }

                }
            }
        }
        if(col_neg_count == csp.col_neg[newAssignedVar.col]) {
            colNegSatisfied = true;
            for(int i = 0; i < csp.n; i++) {
                if(csp.vars[i][newAssignedVar.col].value==VarState.notInit) {
                    if(csp.vars[i][newAssignedVar.col].domain.contains(VarState.neg)) {
                        boolean alreadyAdded = false;
                        for(Pair p : oldDomains) if(p.var.row==i && p.var.col==newAssignedVar.col) alreadyAdded = true;
                        if(!alreadyAdded)
                            oldDomains.add(new Pair(csp.vars[i][newAssignedVar.col], (TreeSet<VarState>) csp.vars[i][newAssignedVar.col].domain.clone()));
                        csp.vars[i][newAssignedVar.col].domain.remove(VarState.neg);
                    }

                }
            }
        }

        //for(Pair p : oldDomains) {
        //    System.out.println(p);
        //}

        //csp.printDomains(newAssignedVar.row, newAssignedVar.col);


        for(int i = 0; i < csp.n; i++) {
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[i][j].domain.isEmpty()) {
                    //System.out.println("||||||||||||||||||||||||||||||||");
                    //csp.printDomains(newAssignedVar.row, newAssignedVar.col);
                }
            }
        }
        return oldDomains;
    }
    public static ArrayList<Pair> BinaryForwardChecking(Csp csp, Variable newAssignedVar) {
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

        oldDomains.add(new Pair(pairVar, (TreeSet<VarState>) pairVar.domain.clone()));

        //if it is unassigned:
        if(pairVar.value==VarState.notInit) {
            ArrayList<VarState> listToRemove = new ArrayList<>();
            for(VarState var : pairVar.domain) {
                //assign
                pairVar.value = var;

                //check pairVar con
                if(!pairVar.isPairConsistent(csp)) {
                    //pairVar.domain.remove(var);
                    listToRemove.add(var);
                }
                //undo assignment
                pairVar.value = VarState.notInit;
            }
            pairVar.domain.removeAll(listToRemove);
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

        //for(Pair p : oldDomains) {
        //    System.out.println(p);
        //}

        //csp.printDomains(newAssignedVar.row, newAssignedVar.col);


        for(int i = 0; i < csp.n; i++) {
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[i][j].domain.isEmpty()) {
                    //System.out.println("||||||||||||||||||||||||||||||||");
                    //csp.printDomains(newAssignedVar.row, newAssignedVar.col);
                }
            }
        }
        return oldDomains;
    }

    public static void backtrack_arc_consistency(Csp csp) {
        boolean temp = recursive_arc(csp);
    }

    public static boolean recursive_arc(Csp csp) {
        //do GRAPH ARC-CONSISTENCY


        return false;
    }


    // ONLY BINARY CONSTRAINTS (did not transformed general constraints into binary yet)
    public static boolean ac3(Csp csp) {
        Queue<CellPair> arcs = (Queue<CellPair>) new LinkedHashSet<CellPair>();
        //initialize arcs
        for(int i = 0; i < csp.n; i++) {
            for(int j = 0; j < csp.m; j++) {
                ArrayList<Variable> neighbours = csp.vars[i][j].getNeighbours(csp);
                for(Variable y : neighbours) {
                    /// if y is already assigned, then we do not care
                    if(y.value != VarState.notInit) {
                        CellPair newArc = new CellPair(csp.vars[i][j], y);
                        if(!arcs.contains(newArc))
                            arcs.add(newArc);
                    }
                }
            }
        }

        while(!arcs.isEmpty()) {
            CellPair poppedArc = arcs.poll();
            arcs.remove();
            if(revise(csp, poppedArc.x, poppedArc.y)) {
                if(poppedArc.x.domain.isEmpty()) return false;
                for(Variable neighbour : poppedArc.x.getNeighbours(csp)) {
                    if(neighbour.value != VarState.notInit) {
                        if(!neighbour.equals(poppedArc.y)) {
                            ////// as we mentioned before, in AC-3 each
                            // binary constraints becomes 2 arcs, one in
                            // each direction

                            //in this part you have to add Xk, Xi (Xk is neighbour)
                            // why? because revise function returned true, so
                            // domain of Xi has changed , therefore you need to recheck
                            // arc consistency of arc Xk, Xi (NOT Xi and Xk)
                            boolean isNotAlreadyAdded = arcs.add(new CellPair(neighbour, poppedArc.x));
                            if(!isNotAlreadyAdded) System.out.println("+-+");
                        }
                    }
                }
            }

        }

        return true;
    }

    public static boolean revise(Csp csp, Variable x, Variable y) {
        if(y.value != VarState.notInit) return false;
        boolean revised = false;
        for(VarState val : x.domain) {
            boolean foundConsistentValue = false;
            ////// ONLY CHECK CONSTRAINTS BETWEEN X AND Y (not all the neighbours)
            // meaning that :
            // if X, Y are pairs -> check pairConsistency between them
            // if X, Y are neighbour poles -> check poleConsistency between them

            //assign val to x
            x.value = val;
            //iterate over y domain to see of there is any value in y's domain that
            // satisfies the binary constraint between X, Y
            for(VarState y_val : y.domain) {
                //**** we assume that X is binary consistent up until here
                // since we assumed
                // its consistent with respect to other variables (other than y)
                // checking ALL x's binary constraints, just means checking
                // binary constrains between X and Y

                ///////// if that assumption changes (you changed the code)
                // you need to make this part so that it only checks bConstraints
                // between X and Y

                //assign y to y_val
                y.value = y_val;

                //**** NOTE THAT WE DONT check consistency of Y's new value across
                // other variables
                if(x.)
            }
        }
        return revised;
    }
    public static void backtrack_FC_MRV_LCV (Csp csp){
        System.out.println(recursive_FC_MRV_LCV(csp));
    }

    // returns list of legal values sorted by LCV ( ONLY CONSISTENT ONES )
    public static ArrayList<VarState> LCV(Csp csp, Variable assignedVar) {
        HashMap<VarState, Integer> flexMap = new HashMap<>();
        ArrayList<VarState> sortedDomain = new ArrayList<>();

        for(VarState val : assignedVar.domain) {
            //assign
            int domainSum = 0;
            csp.vars[assignedVar.row][assignedVar.col].value = val;

            ArrayList<Pair> oldDomains = ForwardChecking(csp, assignedVar);
            for(int i = 0; i < csp.n; i++) {
                for(int j = 0; j < csp.m; j++) {
                    if(csp.vars[i][j].value == VarState.notInit) {
                        domainSum += csp.vars[i][j].domain.size();
                    }
                }
            }
            //UNDO FORWARD CHECKING
            //UndoForwardChecking(csp, varToAssign);
            for(Pair p : oldDomains) {
                csp.vars[p.var.row][p.var.col].domain = p.domain;
            }

            flexMap.put(val, domainSum);
            sortedDomain.add(val);
        }


        sortedDomain.sort(new Comparator<VarState>() {
            @Override
            public int compare(VarState o1, VarState o2) {
                if(flexMap.get(o1) > flexMap.get(o2)) return -1;
                else if(flexMap.get(o1) < flexMap.get(o2)) return 1;
                else return 0;
            }
        });

        if(sortedDomain.size()==3 &&
                (flexMap.get(VarState.neg).intValue() != flexMap.get(VarState.pos).intValue())
                &&
                (flexMap.get(VarState.neg).intValue() != (flexMap.get(VarState.empty).intValue()))
                &&
                (flexMap.get(VarState.empty).intValue() != (flexMap.get(VarState.pos)).intValue())
                ){
            //System.out.println(flexMap);
            //System.out.println(sortedDomain);

        }
        return sortedDomain;
    }
    public static boolean recursive_FC_MRV_LCV(Csp csp) {
        recursion_count_fc_mrv_lcv++;
        if(csp.isComplete()) {
            System.out.println("assignment is complete");
            csp.printBoard( -1, -1);
            return true;
        }

        //ADD -> select a var based on MRV
        Variable varToAssign = csp.vars[0][0];
        boolean firstNotInitializedFound = false;
        for(int i = 0; i < csp.n; i++) {
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[i][j].value == VarState.notInit) {
                    if(!firstNotInitializedFound) {
                        varToAssign = csp.vars[i][j];
                        firstNotInitializedFound = true;
                    }
                    else {
                        if(csp.vars[i][j].domain.size() < varToAssign.domain.size()) {
                            varToAssign = csp.vars[i][j];
                        }
                    }
                }
            }
        }
        //System.out.println("Selected variable based on MRV: " + varToAssign.row + ", "+ varToAssign.col);

        //ADD -> sort(Choose value) based on LCS
        ArrayList<VarState> sortedLCV = LCV(csp, varToAssign);
        for(VarState val : sortedLCV) {
            //Assign
            csp.vars[varToAssign.row][varToAssign.col].value = val;

            // Forward Checking for the new assignment (UPDATE DOMAIN FOR UNASSIGNED VARIABLES)
            //      forward checking returns the changes in domains, so we can undo them
            //ArrayList<Pair> oldDomains = ForwardChecking(csp, varToAssign);
            ArrayList<Pair> oldDomains = BinaryForwardChecking(csp, varToAssign);

            //check if consistent
            // only expand if its consistent
            if(csp.vars[varToAssign.row][varToAssign.col].isConsistent(csp)) {
                //csp.printBoard(varToAssign.row, varToAssign.col);
                //System.out.println("up consistent");

                // if assignment is consistent, and forward checking detected an
                // empty domain for an unassigned variable, we need to skip this value in for loop
                // (we cant return false, because returning false means there is no ANY value for this
                // variable that can be consistent. but we did forward checking after assigning a value
                // to this variable, it only means that this VALUE of this variable is inconsistent, so
                // we have to skip this value, not this variable
                // for skipping this value, just do not expand the search any longer (dont call recursive)
                boolean fc_detected_failure = false;
                for(int i = 0; i < csp.n; i++) {
                    for(int j = 0; j < csp.m; j++) {
                        if(csp.vars[i][j].value == VarState.notInit) {
                            if(csp.vars[i][j].domain.isEmpty()) {
                                //System.out.println("STOPPED BY FC ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                                //csp.printDomains(varToAssign.row, varToAssign.col);
                                fc_detected_failure = true;
                            }
                        }
                    }
                }
                //csp.printDomains(varToAssign.row, varToAssign.col);
                if (!fc_detected_failure) {
                    boolean result = recursive_FC_MRV_LCV(csp);
                    if(result) return true;
                }
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


        return false;
    }

    public static void backtrack_FC(Csp csp) {
        boolean temp = recursive_FC(csp);
        System.out.println(temp);
    }
    public static boolean recursive_FC(Csp csp) {
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
        for(VarState val : varToAssign.domain) {
            //Assign
            csp.vars[varToAssign.row][varToAssign.col].value = val;

            // Forward Checking for the new assignment (UPDATE DOMAIN FOR UNASSIGNED VARIABLES)
            //      forward checking returns the changes in domains, so we can undo them
            ArrayList<Pair> oldDomains = BinaryForwardChecking(csp, varToAssign);
            //ArrayList<Pair> oldDomains = ForwardChecking(csp, varToAssign);


            //check if consistent
            // only expand if its consistent
            if(csp.vars[varToAssign.row][varToAssign.col].isConsistent(csp)) {
                //csp.printBoard(varToAssign.row, varToAssign.col);
                //System.out.println("up consistent");

                // if assignment is consistent, and forward checking detected an
                // empty domain for an unassigned variable, we need to skip this value in for loop
                // (we cant return false, because returning false means there is no ANY value for this
                // variable that can be consistent. but we did forward checking after assigning a value
                // to this variable, it only means that this VALUE of this variable is inconsistent, so
                // we have to skip this value, not this variable
                // for skipping this value, just do not expand the search any longer (dont call recursive)
                boolean fc_detected_failure = false;
                for(int i = 0; i < csp.n; i++) {
                    for(int j = 0; j < csp.m; j++) {
                        if(csp.vars[i][j].value == VarState.notInit) {
                            if(csp.vars[i][j].domain.isEmpty()) {
                                //System.out.println("STOPPED BY FC ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                                //csp.printDomains(varToAssign.row, varToAssign.col);
                                fc_detected_failure = true;
                                bfc++;
                                fc++;
                            }
                        }
                    }
                }

                if (!fc_detected_failure) {
                    boolean result = recursive_FC(csp);
                    if(result) return true;
                }
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


        return false;
    }

    /*
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
    */

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

        for(VarState val : varToAssign.domain) {
            //assign
            csp.vars[varToAssign.row][varToAssign.col].value = val;

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

        return false;
    }
}

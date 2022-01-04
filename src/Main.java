import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    static int recursion_count_simple_backtracking = 0;
    static int recursion_count_fc = 0;
    static int recursion_count_fc_mrv_lcv = 0;
    static int recursion_count_arc = 0;

    static int bfc = 0;
    static int fc = 0;

    public static void main(String[] args) {
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

        Csp csp1 = new Csp(n, m, col_pos, col_neg, row_pos, row_neg, board);
        Csp csp2 = new Csp(n, m, col_pos, col_neg, row_pos, row_neg, board);
        Csp csp3 = new Csp(n, m, col_pos, col_neg, row_pos, row_neg, board);
        Csp csp4 = new Csp(n, m, col_pos, col_neg, row_pos, row_neg, board);

        long startTime1 = System.nanoTime();
        backtrack_solve(csp1);
        long stopTime1 = System.nanoTime();
        System.out.println("dry backtrack, TIME: " + (stopTime1 - startTime1)/1000000);

        long startTime2 = System.nanoTime();
        backtrack_FC(csp2);
        long stopTime2 = System.nanoTime();
        System.out.println("backTrack, only forward checking, TIME: " + (stopTime2 - startTime2)/1000000);

        long startTime3 = System.nanoTime();
        backtrack_FC_MRV_LCV(csp3);
        long stopTime3 = System.nanoTime();
        System.out.println("forward checking+MRV+LCV, TIME: " + (stopTime3 - startTime3)/1000000);

        long startTime4 = System.nanoTime();
        backtrack_arc_consistency(csp4);
        long stopTime4 = System.nanoTime();
        System.out.println("with arc consistency, TIME: " + (stopTime4 - startTime4)/1000000);

        System.out.println("#recursions simple backtrack: " + recursion_count_simple_backtracking);
        System.out.println("#recursions with FC: " + recursion_count_fc);
        System.out.println("#recursions in forward checking+MRV+LCV: " + recursion_count_fc_mrv_lcv);
        System.out.println("#recursions with arc con: " + recursion_count_arc);

        System.out.println("fc: "+ fc);
        System.out.println("bfc: " + bfc);
        //csp.printBoard();

    }


    public static void backtrack_arc_consistency(Csp csp) {
        boolean temp = recursive_arc(csp);
    }

    public static boolean recursive_arc(Csp csp) {
        recursion_count_arc++;
        ArrayList<Pair> oldDomains = ac3(csp);

        // un comment to see how ac3 is doing

        /*
        System.out.println("current board:");
        csp.printBoard(-1, -1);
        System.out.println("domain after ac3: ");
        csp.printDomains(-1, -1);
        */

        if(csp.isComplete()) {
            System.out.println("assignment is complete");
            csp.printBoard( -1, -1);
            return true;
        }

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
            //Assign
            csp.vars[varToAssign.row][varToAssign.col].value = val;

            // Forward Checking for the new assignment (UPDATE DOMAIN FOR UNASSIGNED VARIABLES)
            //      forward checking returns the changes in domains, so we can undo them
            //ArrayList<Pair> oldDomainsFromFC = BinaryForwardChecking(csp, varToAssign);
            //ArrayList<Pair> oldDomainsFromFC = ForwardChecking(csp, varToAssign);
            ArrayList<Pair> oldDomainsFromFC = Csp.abnormalForwardChecking(csp, varToAssign);

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
                    boolean result = recursive_arc(csp);
                    if(result) return true;
                }
            }
            else {
                //csp.printBoard(varToAssign.row, varToAssign.col);
                //System.out.println("up in-consistent");
            }

            //UNDO FORWARD CHECKING
            for(Pair p : oldDomainsFromFC) {
                csp.vars[p.var.row][p.var.col].domain = p.domain;
            }

            //remove from assignment
            csp.vars[varToAssign.row][varToAssign.col].value = VarState.notInit;
            //-------- if you assign the pair as well, you have to undo pair assignment too
            //csp.vars[csp.getPair(varToAssign).row][csp.getPair(varToAssign).col].value = VarState.notInit;
        }

        for(Pair p : oldDomains) {
            csp.vars[p.var.row][p.var.col].domain = p.domain;
        }

        return false;
    }

    // ONLY BINARY CONSTRAINTS (did not transformed general constraints into binary yet)
    public static ArrayList<Pair> ac3(Csp csp) {

        ArrayList<Pair> oldDomains = new ArrayList<>();
        Queue<CellPair> arcs =  new LinkedList<>();
        //initialize arcs
        for(int i = 0; i < csp.n; i++) {
            for(int j = 0; j < csp.m; j++) {
                if(csp.vars[i][j].value==VarState.notInit) {
                    ArrayList<Variable> neighbours = csp.vars[i][j].getNeighbours(csp);
                    for(Variable y : neighbours) {
                        /// if y is already assigned, then we do not care
                        //if(y.value == VarState.notInit) {
                        CellPair newArc = new CellPair(csp.vars[i][j], y);
                        if(!arcs.contains(newArc)) {
                            arcs.add(newArc);
                            //System.out.println("arc <("+i+", "+j+"), ("+y.row+", "+y.col+")> added.");
                        }
                        //}
                    }
                }
            }
        }

        while(!arcs.isEmpty()) {
            CellPair poppedArc = arcs.poll();

            //domain of poppedArc.x gets updated in revise() function
            ArrayList<Pair> oldDomainsFromRevise = revise(csp, poppedArc.x, poppedArc.y);

            //System.out.println("returned from revise (Arc: <("+ poppedArc.x.row+","+poppedArc.x.col+"), "+poppedArc.y.row+","+poppedArc.y.col+")>, domain:");
            //csp.printDomains(-1, -1);

            for(Pair p : oldDomainsFromRevise) {
                if(!oldDomains.contains(p)) oldDomains.add(p);
            }
            if(!oldDomainsFromRevise.isEmpty()) { // equivalent to "if(revise())"
                if(poppedArc.x.domain.isEmpty()) {
                    //System.out.println("returned before end of ac3");
                    return oldDomains;
                }
                for(Variable neighbour : poppedArc.x.getNeighbours(csp)) {
                    if(neighbour.value == VarState.notInit) {
                        if(!neighbour.equals(poppedArc.y)) {
                            ////// as we mentioned before, in AC-3 each
                            // binary constraints becomes 2 arcs, one in
                            // each direction

                            // in this part you have to add Xk, Xi (Xk is neighbour)
                            // why? because revise function returned true, so
                            // domain of Xi has changed , therefore you need to recheck
                            // arc consistency of arc <Xk, Xi> (NOT <Xi, Xk>)
                            CellPair newArc = new CellPair(neighbour, poppedArc.x);
                            ArrayList<CellPair> temp = new ArrayList<>();

                            /*boolean isAdded = false;
                            while (!arcs.isEmpty()) {
                                CellPair p = arcs.poll();
                                if(p.equals(newArc)) isAdded = true;
                            }
                            for(CellPair p : temp) arcs.add(p);*/

                            if(!arcs.contains(newArc)) arcs.add(newArc);
                        }
                    }
                }
            }
        }
        return oldDomains;
    }

    //returns true if domain has changed
    public static ArrayList<Pair> revise(Csp csp, Variable x, Variable y) {
        //System.out.println("x, y in revise : " + x.row+", "+x.col+" | "+y.row+", "+y.col);
        ArrayList<Pair> oldDomains = new ArrayList<>();
        if(x.value != VarState.notInit) return oldDomains;

        boolean revised = false;
        ArrayList<VarState> listToRemove = new ArrayList<>();
        for(VarState x_val : x.domain) {
            boolean tempBool = false;
            boolean foundConsistentValue = false;
            ////// ONLY CHECK CONSTRAINTS BETWEEN X AND Y (not all the neighbours)
            // meaning that :
            // if X, Y are pairs -> check pairConsistency between them
            // if X, Y are neighbour poles -> check poleConsistency between them

            //assign val to x
            x.value = x_val;
            //iterate over y domain to see of there is any value in there that
            // satisfies the binary constraint between X, Y

            //System.out.println("checking x value: "+x_val);

            if(y.value!=VarState.notInit) {
                if (csp.getPair(x).equals(y)) {
                    if(x.isPairConsistent(csp)) foundConsistentValue = true;
                    else foundConsistentValue = false;

                    if(!x.isPoleConsistent(csp)) {
                        foundConsistentValue = false;
                    }
                }
                else {
                    if(x.isPoleConsistent(csp)) {
                        foundConsistentValue = true;
                    } else {
                        foundConsistentValue = false;
                    }
                }
            }
            else {
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
                    if (csp.getPair(x).equals(y)) {
                        if(x.isPairConsistent(csp)) foundConsistentValue = true;
                        else foundConsistentValue = false;

                        if(!x.isPoleConsistent(csp)) {
                            foundConsistentValue = false;
                        }
                    }
                    else {
                        if(x.isPoleConsistent(csp)) {
                            foundConsistentValue = true;
                        } else {
                            foundConsistentValue = false;
                        }
                    }

                    //un assign y
                    y.value = VarState.notInit;

                    if(foundConsistentValue) break;
                }
            }

            if (!foundConsistentValue) {
                revised = true;
                tempBool = true;


                boolean alreadyAdded = false;
                for(Pair p : oldDomains) if(p.var.row==x.row && p.var.col==x.col) alreadyAdded = true;
                if(!alreadyAdded) oldDomains.add(new Pair(x, (TreeSet<VarState>) x.domain.clone()));

                listToRemove.add(x_val);
            }
            //un assign x
            x.value = VarState.notInit;
        }

        x.domain.removeAll(listToRemove);

        return oldDomains;
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

            ArrayList<Pair> oldDomains = Csp.abnormalForwardChecking(csp, assignedVar);
            for(int i = 0; i < csp.n; i++) {
                for(int j = 0; j < csp.m; j++) {
                    if(csp.vars[i][j].value == VarState.notInit) {
                        domainSum += csp.vars[i][j].domain.size();
                    }
                }
            }
            //UNDO FORWARD CHECKING
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
            //ArrayList<Pair> oldDomains = Csp.ForwardChecking(csp, varToAssign);
            //ArrayList<Pair> oldDomains = Csp.BinaryForwardChecking(csp, varToAssign);
            ArrayList<Pair> oldDomains = Csp.abnormalForwardChecking(csp, varToAssign);

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
                                fc_detected_failure = true;
                                fc++;
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
            for(Pair p : oldDomains) {
                csp.vars[p.var.row][p.var.col].domain = p.domain;
            }

            //remove from assignment
            csp.vars[varToAssign.row][varToAssign.col].value = VarState.notInit;
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
        //selection is based on index
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
        for(VarState val : varToAssign.domain) {
            //Assign
            csp.vars[varToAssign.row][varToAssign.col].value = val;

            //ArrayList<Pair> oldDomains = Csp.BinaryForwardChecking(csp, varToAssign);
            //ArrayList<Pair> oldDomains = Csp.ForwardChecking(csp, varToAssign);
            ArrayList<Pair> oldDomains = Csp.abnormalForwardChecking(csp, varToAssign);

            if(csp.vars[varToAssign.row][varToAssign.col].isConsistent(csp)) {
                boolean fc_detected_failure = false;
                for(int i = 0; i < csp.n; i++) {
                    for(int j = 0; j < csp.m; j++) {
                        if(csp.vars[i][j].value == VarState.notInit) {
                            if(csp.vars[i][j].domain.isEmpty()) {
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
            //UNDO FORWARD CHECKING
            for(Pair p : oldDomains) {
                csp.vars[p.var.row][p.var.col].domain = p.domain;
            }
            //remove from assignment
            csp.vars[varToAssign.row][varToAssign.col].value = VarState.notInit;
        }
        return false;
    }

    public static void backtrack_solve(Csp csp) {
        boolean temp = recursive(csp);
        System.out.println(temp);
    }
    public static boolean recursive(Csp csp) {
        recursion_count_simple_backtracking++;
        if(csp.isComplete()) {
            System.out.println("assignment is complete");
            csp.printBoard( -1, -1);
            return true;
        }
        //selection is based on index
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
        for(VarState val : varToAssign.domain) {
            //assign
            csp.vars[varToAssign.row][varToAssign.col].value = val;
            if(csp.vars[varToAssign.row][varToAssign.col].isConsistent(csp)) {
                boolean result = recursive(csp);
                if(result) return true;
            }
            //remove from assignment
            csp.vars[varToAssign.row][varToAssign.col].value = VarState.notInit;
        }
        return false;
    }
}

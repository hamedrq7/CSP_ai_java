import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int n = 0, m=0;
        int[] col_pos = new int[0], col_neg = new int[0];
        int[] row_pos = new int[0], row_neg = new int[0];
        int[][] board = new int[0][];
        
        try {
            File myObj = new File("tests\\test1.txt");
            //System.out.println(myObj.exists());
            Scanner scanner = new Scanner(myObj);

            n = scanner.nextInt();
            m = scanner.nextInt();

            col_pos = new int[n];
            col_neg = new int[n];

            row_pos = new int[m];
            row_neg = new int[m];
            
            for(int i = 0; i < n; i++) col_pos[i] =  scanner.nextInt();
            for(int i = 0; i < n; i++) col_neg[i] =  scanner.nextInt();

            for(int i = 0; i < m; i++) row_pos[i] =  scanner.nextInt();
            for(int i = 0; i < m; i++) row_neg[i] =  scanner.nextInt();

            board = new int[m][n];
            for(int i = 0; i < m; i++)
                for(int j = 0; j < n; j++)
                    board[i][j] = scanner.nextInt();


            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        for(int i = 0; i < n; i++) {
            System.out.print(col_pos[i] + " ");
        }
        System.out.println();
        for(int i = 0; i < n; i++) {
            System.out.print(col_neg[i] + " ");
        }
        System.out.println();

        for(int i = 0; i < m; i++) {
            System.out.print(row_pos[i] + " ");
        }
        System.out.println();
        for(int i = 0; i < m; i++) {
            System.out.print(row_neg[i] + " ");
        }
        System.out.println();

        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n; j++) {
                System.out.print(board[i][j] + " ");

            }
            System.out.println();
        }
        System.out.println();

    }


}

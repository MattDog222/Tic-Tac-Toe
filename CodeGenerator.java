import java.util.StringJoiner;

public class CodeGenerator {

    public static int checkPositions(int a, int b, int c, int playerNumber) {
        // Change 1 based index to 0 based, and 2 bits per spot;
        a = (a - 1) * 2;
        b = (b - 1) * 2;
        c = (c - 1) * 2;
        return ((playerNumber << a) | (playerNumber << b) | (playerNumber << c));
    }

    public static String checkColumns(int playerNumber) {
        int p1 = checkPositions(1, 4, 7, playerNumber);
        int p2 = checkPositions(2, 5, 8, playerNumber);
        int p3 = checkPositions(3, 6, 9, playerNumber);
        return "(game&%d)==%d || (game&%d)==%d || (game&%d)==%d".formatted(p1,p1,p2,p2,p3,p3);
    }

    public static String checkRows(int playerNumber) {
        int p1 = checkPositions(1, 2, 3, playerNumber);
        int p2 = checkPositions(4, 5, 6, playerNumber);
        int p3 = checkPositions(7, 8, 9, playerNumber);
        return "(game&%d)==%d || (game&%d)==%d || (game&%d)==%d".formatted(p1,p1,p2,p2,p3,p3);
    }

    public static String checkDiagonals(int playerNumber) {
        int p1 = checkPositions(1, 5, 9, playerNumber);
        int p2 = checkPositions(3, 5, 7, playerNumber);
        return "(game&%d)==%d || (game&%d)==%d".formatted(p1, p1, p2, p2);
    }

    public static String checkAllCellsUsed() {
        StringJoiner sj = new StringJoiner(" && ");
        for (int i = 0 ; i < 9; ++i ) {
            int positionMask = (3 << (i * 2));
            sj.add(String.format("(game&%d)!=0", positionMask));
        }
        return sj.toString();
    }

    public static void main(String[] args) {
        for (int player = 1; player <= 2; ++player) {
            StringJoiner sj = new StringJoiner(" || ");
            sj.add(checkRows(player));
            sj.add(checkColumns(player));
            sj.add(checkDiagonals(player));
            System.out.println("Player " + player + " check:\n" + sj + "\n");
        }
        System.out.println("Tie game check");
        System.out.println(checkAllCellsUsed());

        System.out.println("\nGrid formats");
        StringJoiner sj = new StringJoiner(", ");
        sj.add("((game)&3)==0?' ':(((game)&1)==1?'x':'o')");
        for (int i = 1; i < 9; ++i) {
            int shiftAmount = 2*i;
            sj.add(String.format("((game>>%d)&3)==0?' ':(((game>>%d)&1)==1?'x':'o')", shiftAmount, shiftAmount));
        }
        System.out.println(sj);
    }
}

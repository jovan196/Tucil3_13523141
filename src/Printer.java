import java.util.*;

public class Printer {
    private static final String RESET = "\u001B[0m";
    private static final String RED   = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE  = "\u001B[34m";

    public static void printPath(List<State> path) {
        if (path.isEmpty()) {
            System.out.println("No solution found.");
            return;
        }
        System.out.println("Papan Awal\n" + pretty(path.get(0).board, '\0'));
        for (int i = 1; i < path.size(); i++) {
            State s = path.get(i);
            System.out.println("Gerakan " + i + ": " + s.move);
            System.out.println(pretty(s.board, s.move.piece));
        }
    }

    public static String pretty(Board b, char moved) {
        StringBuilder sb = new StringBuilder();

        // Baris ekstra di ATAS (exitRow == -1)
        if (b.exitRow == -1) {
            for (int c = 0; c < b.cols; c++)
                sb.append(c == b.exitCol ? GREEN + 'K' + RESET : ' ');
            sb.append('\n');
        }

        // Baris-baris grid
        for (int r = 0; r < b.rows; r++) {
            // K di kiri?
            if (b.exitCol == -1 && r == b.exitRow)
                sb.append(GREEN).append('K').append(RESET);

            for (int c = 0; c < b.cols; c++) {
                char ch = b.grid[r][c];
                if (ch == 'P')
                    sb.append(RED).append(ch).append(RESET);
                else if (ch == moved && moved != '\0')
                    sb.append(BLUE).append(ch).append(RESET);
                else
                    sb.append(ch);
            }

            // K di kanan?
            if (b.exitCol == b.cols && r == b.exitRow)
                sb.append(GREEN).append('K').append(RESET);

            sb.append('\n');
        }

        // Baris ekstra di BAWAH (exitRow == rows)
        if (b.exitRow == b.rows) {
            for (int c = 0; c < b.cols; c++)
                sb.append(c == b.exitCol ? GREEN + 'K' + RESET : ' ');
            sb.append('\n');
        }

        return sb.toString();
        }
}
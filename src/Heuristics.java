public class Heuristics {
    private Heuristics() {}

    /*
     * H1 – "blocking cars + distance"  (admissible)
     *   f(n) = (#empty cells between primary tail & exit)
     *         + 2 × (#blocking pieces along that line)
     */
    public static final Heuristic H1 = b -> {
        Piece p = b.primary;
        if (p.orient == Orientation.HORIZONTAL) {
            int dist = b.exitCol - p.tailCol() - 1;  // empty cells before exit
            int blockers = 0;
            for (int c = p.tailCol() + 1; c < b.exitCol; c++) if (b.grid[p.row][c] != '.') blockers++;
            return dist + blockers * 2;
        } else {
            int dist = b.exitRow - p.tailRow() - 1;
            int blockers = 0;
            for (int r = p.tailRow() + 1; r < b.exitRow; r++) if (b.grid[r][p.col] != '.') blockers++;
            return dist + blockers * 2;
        }
    };

    public static Heuristic byId(int id) {
        return switch (id) { case 1 -> H1; default -> H1; };
    }
}

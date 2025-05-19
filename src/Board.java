import java.util.*;

public class Board {
    // immutable public fields for convenience (small program)
    public final int rows, cols;
    public final char[][] grid;               // '.' for empty
    public final Map<Character, Piece> pieces; // id ➜ Piece (excl. exit)
    public final Piece primary;               // the red piece 'P'
    public final int exitRow, exitCol;        // coordinates of 'K'

    public Board(int rows, int cols, char[][] grid,
                 Map<Character, Piece> pieces, Piece primary,
                 int exitRow, int exitCol) {
        this.rows = rows; this.cols = cols;
        this.grid = grid; this.pieces = pieces;
        this.primary = primary; this.exitRow = exitRow; this.exitCol = exitCol;
    }

    /* -------------------------  Parsing  ------------------------- */
    public static Board parse(List<String> lines) {
        if (lines.size() < 3) throw new IllegalArgumentException("Input too short");

        String[] dim = lines.get(0).trim().split("\\s+");
        int R = Integer.parseInt(dim[0]);
        int C = Integer.parseInt(dim[1]);
        int idx = 1;
        // second line optional N – we simply skip it (not strictly required)
        try { Integer.parseInt(lines.get(idx).trim()); idx++; } catch (NumberFormatException ignored) {}

        if (lines.size() - idx < R)
            throw new IllegalArgumentException("Not enough board rows – expected " + R);

        char[][] g = new char[R][C];
        for (char[] row : g) Arrays.fill(row, '.');

        Map<Character, List<int[]>> locs = new HashMap<>();
        int exR = -1, exC = -1;

        for (int r = 0; r < R; r++) {
            String raw = lines.get(idx + r).trim();           // baris mentah
            // ────────────────── deteksi K di dinding ──────────────────
            if (raw.length() == C + 1) {                      // ada 1 sel ekstra
                if (raw.charAt(0) == 'K') {                   // K di kiri papan
                    exR = r; exC = -1;                // use local vars
                    raw = raw.substring(1);                   // buang 'K'
                } else if (raw.charAt(raw.length() - 1) == 'K') {  // K di kanan
                    exR = r; exC = C;                 // use local vars
                    raw = raw.substring(0, raw.length() - 1); // buang 'K'
                } else {
                    throw new IllegalArgumentException("Extra char bukan 'K' di row " + r);
                }
            } else if (raw.length() != C) {
                throw new IllegalArgumentException(
                    "Row " + r + " length " + raw.length() + " ≠ " + C);
            }
            // -----------------------------------------------------------

            // sekarang 'raw' pasti panjangnya persis C
            for (int c = 0; c < C; c++) {
                char ch = raw.charAt(c);
                if (ch != '.') {
                    g[r][c] = ch;
                    locs.computeIfAbsent(ch, k -> new ArrayList<>()).add(new int[] { r, c });
                }
            }
        }

        if (exR == -1) throw new IllegalArgumentException("Exit 'K' not found");
        Map<Character, Piece> pcs = new HashMap<>();
        Piece prim = null;
        for (var e : locs.entrySet()) {
            char id = e.getKey();
            List<int[]> cells = e.getValue();
            int len = cells.size();
            int r0 = cells.get(0)[0], c0 = cells.get(0)[1];
            boolean horizontal = cells.stream().allMatch(p -> p[0] == r0);
            Orientation o = horizontal ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            Piece p = new Piece(id, r0, c0, len, o);
            pcs.put(id, p);
            if (id == 'P') prim = p;
        }
        if (prim == null) throw new IllegalArgumentException("Primary piece 'P' not found");
        return new Board(R, C, g, pcs, prim, exR, exC);
    }

    /* -----------------------  Neighbours  ------------------------ */
    public List<State> expand(State parent) {
        List<State> next = new ArrayList<>();
        for (Piece p : pieces.values()) {
            if (p.orient == Orientation.HORIZONTAL) {
                slideHorizontal(parent, next, p);
            } else {
                slideVertical(parent, next, p);
            }
        }
        return next;
    }

    private void slideHorizontal(State parent, List<State> out, Piece p) {
        // left
        int c = p.col - 1;
        while (c >= 0 && grid[p.row][c] == '.') { out.add(createChild(parent, p, c - p.col)); c--; }
        // right
        c = p.tailCol() + 1;
        while (c < cols && grid[p.row][c] == '.') { out.add(createChild(parent, p, c - p.tailCol())); c++; }
        // special: exit
        if (p.id == 'P' && p.row == exitRow && p.tailCol() < exitCol) {
            boolean clear = true;
            for (int cc = p.tailCol() + 1; cc < exitCol; cc++) if (grid[p.row][cc] != '.') { clear = false; break; }
            if (clear) {
                State goal = createChild(parent, p, exitCol - p.tailCol());
                goal.isGoal = true;
                out.add(goal);
            }
        }
    }

    private void slideVertical(State parent, List<State> out, Piece p) {
        // up
        int r = p.row - 1;
        while (r >= 0 && grid[r][p.col] == '.') { out.add(createChild(parent, p, r - p.row)); r--; }
        // down
        r = p.tailRow() + 1;
        while (r < rows && grid[r][p.col] == '.') { out.add(createChild(parent, p, r - p.tailRow())); r++; }
        // (rare) vertical exit
        if (p.id == 'P' && p.col == exitCol && p.tailRow() < exitRow) {
            boolean clear = true;
            for (int rr = p.tailRow() + 1; rr < exitRow; rr++) if (grid[rr][p.col] != '.') { clear = false; break; }
            if (clear) {
                State goal = createChild(parent, p, exitRow - p.tailRow());
                goal.isGoal = true;
                out.add(goal);
            }
        }
    }

    private State createChild(State parent, Piece moving, int delta) {
        Map<Character, Piece> np = new HashMap<>(pieces);
        Piece mv = moving.moved(delta);
        np.put(mv.id, mv);

        // rebuild grid (cheap for 6×6 boards)
        char[][] ng = new char[rows][cols];
        for (char[] row : ng) Arrays.fill(row, '.');
        for (Piece pc : np.values()) {
            for (int i = 0; i < pc.length; i++) {
                int r = pc.orient == Orientation.HORIZONTAL ? pc.row : pc.row + i;
                int c = pc.orient == Orientation.HORIZONTAL ? pc.col + i : pc.col;
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    ng[r][c] = pc.id;
                }
            }
        }
        int newG = parent == null ? Math.abs(delta) : parent.g + Math.abs(delta);
        return new State(new Board(rows, cols, ng, np, np.get('P'), exitRow, exitCol),
                         parent, new Move(moving.id, delta), newG);
    }

    /* -------------------------  Utils  -------------------------- */
    @Override public boolean equals(Object o) { return o instanceof Board b && Arrays.deepEquals(grid, b.grid); }
    @Override public int hashCode()          { return Arrays.deepHashCode(grid); }
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) { sb.append(row); sb.append('\n'); }
        return sb.toString();
    }
}
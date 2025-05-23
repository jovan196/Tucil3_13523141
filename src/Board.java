import java.util.*;

public class Board {
    // atribut public final untuk memudahkan akses
    public final int rows, cols;
    public final char[][] grid;               // '.' untuk kosong, 'X' untuk halangan
    public final Map<Character, Piece> pieces; // id -> Piece (tidak termasuk exit)
    public final Piece primary;               // primary piece 'P'
    public final int exitRow, exitCol;        // koordinat 'K'

    public record Pos(int r,int c){}
    public final Set<Pos> obstacles;

    public Board(int rows, int cols, char[][] grid,
                 Map<Character, Piece> pieces, Piece primary,
                 int exitRow, int exitCol, Set<Pos> obstacles) {
        this.rows = rows; this.cols = cols;
        // Deep copy grid
        this.grid = new char[grid.length][];
        for (int i = 0; i < grid.length; i++) {
            this.grid[i] = Arrays.copyOf(grid[i], grid[i].length);
        }
        this.pieces = pieces;
        this.primary = primary; this.exitRow = exitRow; this.exitCol = exitCol;
        this.obstacles = obstacles;
    }

    // Parse input dari file
    public static Board parse(List<String> lines) {
        if (lines.size() < 3) throw new IllegalArgumentException("Input too short");

        String[] dim = lines.get(0).trim().split("\\s+");
        int R = Integer.parseInt(dim[0]);
        int C = Integer.parseInt(dim[1]);
        int idx = 1;
        // Baris kedua (banyaknya piece) bisa diabaikan
        // karena dihitung dari banyaknya karakter unik di baris-baris berikutnya
        try { Integer.valueOf(lines.get(idx).trim()); idx++; } catch (NumberFormatException ignored) {}

        if (lines.size() - idx < R)
            throw new IllegalArgumentException("Not enough board rows – expected " + R);

        char[][] g = new char[R][C];
        for (char[] row : g) Arrays.fill(row, '.');

        Map<Character, List<int[]>> locs = new HashMap<>();
        int exR = -2, exC = -2;

        List<String> rawRows = lines.subList(idx, lines.size());
        List<String> boardLines = new ArrayList<>();
        for (int i = 0; i < rawRows.size(); i++) {
            String rawLine = rawRows.get(i);
            String trim = rawLine.trim();
            // baris hanya 'K'? berarti pintu atas (jika boardLines kosong) 
            // atau pintu bawah (jika boardLines sudah lengkap)
            if (trim.equals("K")) {
                int c = rawLine.indexOf('K');
                if (boardLines.isEmpty()) {
                    exR = -1;    // exit di atas baris-0
                    exC = c;
                } else if (boardLines.size() == R) {
                    exR = R;     // exit di bawah baris-(R-1)
                    exC = c;
                } else {
                    throw new IllegalArgumentException("Baris 'K' muncul di tengah input");
                }
                continue;  // jangan masukkan ke boardLines
            }
            // normal row -> harus jadi bagian board
            boardLines.add(rawLine);
        }
        if (boardLines.size() < R)
            throw new IllegalArgumentException("Tidak cukup baris board; dibutuhkan " + R);

        // 2) Sekarang isi grid dari boardLines[0..R-1], deteksi juga K di kiri/kanan:
        Set<Pos> obstacles = new HashSet<>();
        for (int r = 0; r < R; r++) {
            // preserve original spacing to handle side-exit blanks
            String rawLine = boardLines.get(r);
            String raw = rawLine;
             // deteksi K di kiri/kanan seperti sebelumnya:
            if (raw.length() == C + 1) {
                if (raw.charAt(0) == 'K') {
                    exR = r; exC = -1;
                    raw = raw.substring(1);
                } else if (raw.charAt(raw.length()-1) == 'K') {
                    exR = r; exC = C;
                    raw = raw.substring(0, raw.length()-1);
                } else if (raw.charAt(0) == ' ') {
                    // ignore leading blank column for left-side exit
                    raw = raw.substring(1);
                } else if (raw.charAt(raw.length()-1) == ' ') {
                    // ignore trailing blank column for right-side exit
                    raw = raw.substring(0, raw.length()-1);
                } else {
                    throw new IllegalArgumentException("Extra char bukan 'K' di row " + r);
                }
            }
            // after stripping, ensure proper length
            raw = raw.trim();
            if (raw.length() != C) {
                throw new IllegalArgumentException("Row " + r + " length " + raw.length() + " != " + C);
            }
             // isi cell dan kumpulkan lokasi piece
             for (int c = 0; c < C; c++) {
                 char ch = raw.charAt(c);
                 if (ch == 'X') {
                     g[r][c] = 'X';
                     obstacles.add(new Pos(r,c));
                 }
                 else if (ch != '.') {
                     g[r][c] = ch;
                     locs.computeIfAbsent(ch, k -> new ArrayList<>()).add(new int[]{r,c});
                 }
             }
        }

        if (exR == -2)
            throw new IllegalArgumentException("Exit 'K' tidak ditemukan di sisi mana pun");
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
        return new Board(R, C, g, pcs, prim, exR, exC, Collections.unmodifiableSet(obstacles));
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
        // special: horizontal exit for primary piece
        if (p.id == 'P' && p.row == exitRow) {
            // exit on right side
            if (exitCol == cols) {
                boolean clear = true;
                for (int cc = p.tailCol() + 1; cc < cols; cc++) {
                    if (grid[p.row][cc] != '.') { clear = false; break; }
                }
                if (clear) {
                    State goal = createChild(parent, p, cols - p.tailCol());
                    goal.isGoal = true; out.add(goal);
                }
            }
            // exit on left side
            else if (exitCol == -1) {
                boolean clear = true;
                for (int cc = p.col - 1; cc >= 0; cc--) {
                    if (grid[p.row][cc] != '.') { clear = false; break; }
                }
                if (clear) {
                    int delta = -(p.col + 1);
                    State goal = createChild(parent, p, delta);
                    goal.isGoal = true; out.add(goal);
                }
            }
        }
    }

    private void slideVertical(State parent, List<State> out, Piece p) {
        // geser ke atas
        int r = p.row - 1;
        while (r >= 0 && grid[r][p.col] == '.') {
            out.add(createChild(parent, p, r - p.row));
            r--;
        }
        // geser ke bawah
        r = p.tailRow() + 1;
        while (r < rows && grid[r][p.col] == '.') {
            out.add(createChild(parent, p, r - p.tailRow()));
            r++;
        }
        // special: exit vertikal (atas/bawah)
        if (p.id == 'P' && p.col == exitCol) {
            // exit di bawah (exitRow == rows)
            if (exitRow == rows) {
                boolean clear = true;
                for (int rr = p.tailRow() + 1; rr < rows; rr++) {
                    if (grid[rr][p.col] != '.') { clear = false; break; }
                }
                if (clear) {
                    // geser P sampai keluar bawah
                    State goal = createChild(parent, p, rows - p.tailRow());
                    goal.isGoal = true;
                    out.add(goal);
                }
            }
            // exit di atas (exitRow == -1)
            else if (exitRow == -1) {
                boolean clear = true;
                for (int rr = p.row - 1; rr >= 0; rr--) {
                    if (grid[rr][p.col] != '.') { clear = false; break; }
                }
                if (clear) {
                    // geser P sampai keluar atas
                    int delta = -(p.row + 1);
                    State goal = createChild(parent, p, delta);
                    goal.isGoal = true;
                    out.add(goal);
                }
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
        for (Pos o : obstacles) ng[o.r()][o.c()] = 'X';
        int newG = parent == null ? Math.abs(delta) : parent.g + Math.abs(delta);
        return new State(new Board(rows, cols, ng, np, np.get('P'), exitRow, exitCol,obstacles),
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
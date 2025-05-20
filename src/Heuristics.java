public class Heuristics {
    private Heuristics() {}

    /*
     * H1 – "mobil penghalang + jarak" (admissible)
     *   f(n) = (#sel kosong antara ekor primary & exit)
     *         + 2 × (#potongan penghalang di sepanjang jalur tersebut)
     */
    public static final Heuristic H1 = b -> {
        Piece p = b.primary;
        if (p.orient == Orientation.HORIZONTAL) {
            int dist = b.exitCol - p.tailCol() - 1;  // cell kosong
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


    /*------------------------------------------------------------------
     * H2 – distance + 1×blocking  (lebih longgar, tetap admissible)
     *    Alasan: setiap kendaraan penghalang butuh ≥1 gerakan.
     *-----------------------------------------------------------------*/
    public static final Heuristic H2 = b -> {
        Piece p = b.primary;
        if (p.orient == Orientation.HORIZONTAL) {
            int dist = b.exitCol - p.tailCol() - 1;
            int block = 0;
            for (int c = p.tailCol() + 1; c < b.exitCol; c++)
                if (b.grid[p.row][c] != '.') block++;
            return dist + block;           // koefisien 1 -> selalu ≤ biaya real
        } else {
            int dist = b.exitRow - p.tailRow() - 1;
            int block = 0;
            for (int r = p.tailRow() + 1; r < b.exitRow; r++)
                if (b.grid[r][p.col] != '.') block++;
            return dist + block;
        }
    };

    /*------------------------------------------------------------------
     * H3 – distance + 2 × blocking + “secondary blocker” penalty
     *    0 Hitung blocker langsung di jalur P -> K  (seperti H1)
     *    - Tambah +1 lagi per kendaraan yang menghalangi blocker
     *      pertama bila ia hendak bergeser satu sel (estimasi kasar).
     *    - Jika sel rintangan 'X' tepat di jalur -> kembalikan Integer.MAX_VALUE
     *      agar A* / GBFS  praktis menghindari state tak‐solvable.
     *    - Heuristik ini bias ke arah besar  ->  bisa *inadmissible*,
     *      namun sering memangkas eksplorasi.
     *-----------------------------------------------------------------*/
    public static final Heuristic H3 = b -> {
        Piece p = b.primary;

        // fungsi bantu: true bila koordinat dalam grid & bukan '.'
        java.util.function.BiPredicate<Integer,Integer> occupied = (r,c) ->
            r>=0 && r<b.rows && c>=0 && c<b.cols && b.grid[r][c] != '.';

        int dist, block = 0, secBlock = 0;

        if (p.orient == Orientation.HORIZONTAL) {
            dist = b.exitCol - p.tailCol() - 1;
            for (int c = p.tailCol()+1; c < b.exitCol; c++) {
                char id = b.grid[p.row][c];
                if (id == '.') continue;
                if (id == 'X') return Integer.MAX_VALUE; // dinding permanen
                block++;
                // cek satu sel di atas & bawah blocker, apakah juga terisi?
                if (occupied.test(p.row-1, c) || occupied.test(p.row+1, c))
                    secBlock++;
            }
        } else {
            dist = b.exitRow - p.tailRow() - 1;
            for (int r = p.tailRow()+1; r < b.exitRow; r++) {
                char id = b.grid[r][p.col];
                if (id == '.') continue;
                if (id == 'X') return Integer.MAX_VALUE;
                block++;
                if (occupied.test(r, p.col-1) || occupied.test(r, p.col+1))
                    secBlock++;
            }
        }
        return dist + block * 2 + secBlock;   // lebih agresif
    };

    /*----------------------  Selector by ID  -------------------------*/
    public static Heuristic byId(int id) {
        return switch (id) {
            case 1 -> H1;
            case 2 -> H2;
            case 3 -> H3;
            default -> H1;
        };
    }
}

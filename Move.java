public class Move {
    public final char piece; public final int delta;
    Move(char piece, int delta) { this.piece = piece; this.delta = delta; }
    @Override public String toString() {
        if (delta == 0) return piece + "-0";
        String dir = (delta < 0) ? "L" : "R";
        if (dir.equals("L") && Math.abs(delta) > 1) dir += Math.abs(delta);
        if (dir.equals("R") && delta       > 1) dir += delta;
        return piece + '-' + dir;
    }
}
public class Piece {
    public final char id; public final int row, col, length; public final Orientation orient;
    Piece(char id, int row, int col, int length, Orientation orient) {
        this.id=id; this.row=row; this.col=col; this.length=length; this.orient=orient;
    }
    int tailRow() { return orient == Orientation.VERTICAL   ? row + length - 1 : row; }
    int tailCol() { return orient == Orientation.HORIZONTAL ? col + length - 1 : col; }
    Piece moved(int d) {
        return orient == Orientation.HORIZONTAL ? new Piece(id, row, col + d, length, orient)
                                                : new Piece(id, row + d, col, length, orient);
    }
}
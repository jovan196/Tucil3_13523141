public class State implements Comparable<State> {
    final Board board; final State parent; final Move move; final int g; int h; boolean isGoal = false;
    State(Board b, State p, Move m, int g) { board = b; parent = p; move = m; this.g = g; }
    int f() { return g + h; }
    @Override public int compareTo(State o) { return Integer.compare(f(), o.f()); }
}

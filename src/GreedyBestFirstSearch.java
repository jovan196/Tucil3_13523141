import java.util.*;

public class GreedyBestFirstSearch implements PathFinder {
    @Override
    public SearchResult search(Board start, Heuristic h) {
        PriorityQueue<State> open = new PriorityQueue<>(Comparator.comparingInt(s -> s.h));
        Set<Board> closed = new HashSet<>();
        State root = new State(start, null, new Move('-', 0), 0);
        root.h = h.estimate(start); open.add(root);
        long visited = 0;
        while (!open.isEmpty()) {
            State cur = open.poll(); visited++;
            if (cur.isGoal) return new SearchResult(cur, visited);
            closed.add(cur.board);
            for (State nxt : cur.board.expand(cur)) {
                nxt.h = h.estimate(nxt.board);
                if (closed.contains(nxt.board)) continue;
                open.add(nxt);
            }
        }
        return new SearchResult(null, visited);
    }
}

import java.util.*;

public class AStarSearch implements PathFinder {
    @Override
    public SearchResult search(Board start, Heuristic h) {
        PriorityQueue<State> open = new PriorityQueue<>();
        Map<Board, Integer> best = new HashMap<>();
        State root = new State(start, null, new Move('-', 0), 0);
        root.h = h.estimate(start); open.add(root); best.put(start, 0);
        long visited = 0;
        while (!open.isEmpty()) {
            State cur = open.poll(); visited++;
            if (cur.isGoal) return new SearchResult(cur, visited);
            for (State nxt : cur.board.expand(cur)) {
                nxt.h = h.estimate(nxt.board);
                if (best.getOrDefault(nxt.board, Integer.MAX_VALUE) > nxt.g) {
                    best.put(nxt.board, nxt.g);
                    open.add(nxt);
                }
            }
        }
        return new SearchResult(null, visited);
    }
}
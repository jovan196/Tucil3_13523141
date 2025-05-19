import java.util.*;

public class UniformCostSearch implements PathFinder {
    @Override
    public SearchResult search(Board start, Heuristic h) {
        PriorityQueue<State> open = new PriorityQueue<>(Comparator.comparingInt(s -> s.g));
        Map<Board, Integer> best = new HashMap<>();
        State root = new State(start, null, new Move('-', 0), 0);
        root.h = 0; open.add(root); best.put(start, 0);
        long visited = 0;
        while (!open.isEmpty()) {
            State cur = open.poll(); visited++;
            if (cur.isGoal) return new SearchResult(cur, visited);
            for (State nxt : cur.board.expand(cur)) {
                nxt.h = 0; // UCS ignores heuristic
                if (best.getOrDefault(nxt.board, Integer.MAX_VALUE) > nxt.g) {
                    best.put(nxt.board, nxt.g);
                    open.add(nxt);
                }
            }
        }
        return new SearchResult(null, visited);
    }
}
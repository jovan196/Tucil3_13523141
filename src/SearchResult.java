import java.util.*;


public class SearchResult {
    private final State goal; private final long visitedCount;
    public SearchResult(State goal, long visited) { this.goal = goal; this.visitedCount = visited; }
    long visitedCount() { return visitedCount; }
    List<State> path() {
        if (goal == null) return List.of();
        List<State> rev = new ArrayList<>();
        for (State s = goal; s != null; s = s.parent) rev.add(s);
        Collections.reverse(rev); return rev;
    }
}
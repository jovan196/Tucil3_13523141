public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java Main <inputFile> <algorithm> [heuristicId]");
            System.out.println("  <algorithm>  : ucs | gbfs | astar");
            return;
        }
        String file = args[0];
        String alg  = args[1].toLowerCase();
        int    hid  = args.length >= 3 ? Integer.parseInt(args[2]) : 1;

        Board start = Parser.load(file);
        PathFinder pf = switch (alg) {
            case "ucs"   -> new UniformCostSearch();
            case "gbfs"  -> new GreedyBestFirstSearch();
            case "astar" -> new AStarSearch();
            default       -> throw new IllegalArgumentException("Unknown algorithm: " + alg);
        };
        Heuristic h = Heuristics.byId(hid);

        long t0 = System.currentTimeMillis();
        SearchResult res = pf.search(start, h);
        long exec = System.currentTimeMillis() - t0;

        System.out.println("Visited nodes : " + res.visitedCount());
        System.out.println("Time (ms)     : " + exec);
        Printer.printPath(res.path());
    }
}
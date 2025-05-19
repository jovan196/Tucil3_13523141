public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Cara Pakai: java Main <inputFile> <algoritma> [ID heuristik]");
            System.out.println("  <inputFile> : file input (contoh: test.txt)");
            System.out.println("  [ID heuristik]: ID heuristik (default: 1)");
            System.out.println("  <algoritma>  : ucs | gbfs | astar");
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
            default       -> throw new IllegalArgumentException("Algoritma tidak dikenal: " + alg);
        };
        Heuristic h = Heuristics.byId(hid);

        long t0 = System.currentTimeMillis();
        SearchResult res = pf.search(start, h);
        long exec = System.currentTimeMillis() - t0;

        System.out.println("Simpul dikunjungi : " + res.visitedCount());
        System.out.println("Waktu (ms)        : " + exec);
        Printer.printPath(res.path());
    }
}
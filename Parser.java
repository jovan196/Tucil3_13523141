import java.nio.file.*;
import java.util.*;

public class Parser {
    public static Board load(String path) throws Exception {
        List<String> lines = Files.readAllLines(Path.of(path));
        return Board.parse(lines);
    }
}
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day21 {

    String filePath = "input/AOC2023Day21.input";

    static char STARTING_POSITION = 'S';
    static char GARDEN_PLOT = '.';
    static char ROCK = '#';
    static char REACHED = 'O';

    static char[][] copyErasingReached(char[][] prev) {
        char[][] empty = new char[prev.length][prev[0].length];
        for (int i = 0; i < prev.length; i++) {
            for (int j = 0; j < prev[0].length; j++) {
                empty[i][j] = prev[i][j] == REACHED ? GARDEN_PLOT : prev[i][j];
            }
        }
        return empty;
    }

    // L, R, D, T
    static int[][] dxdyArr = new int[][] { { 0, -1 }, { 0, +1 }, { 1, 0 }, { -1, 0 } };

    static boolean valid(int i, int j, char[][] map) {
        return 0 <= i && i < map.length && 0 <= j && j < map[0].length;
    }

    static void setReachedAroundFrom(int i, int j, char[][] map) {
        for (var dxdy: dxdyArr) {
            int ii = i + dxdy[0];
            int jj = j + dxdy[1];
            if (valid(ii, jj, map) && map[ii][jj] == GARDEN_PLOT) map[ii][jj] = REACHED;
        }
    }

    // makes 1 step and returns new map with all new positions (without positions for previous steps)
    static char[][] doStep(char[][] prev) {
        char[][] cur = copyErasingReached(prev);
        for (int i = 0; i < prev.length; i++) {
            for (int j = 0; j < prev[0].length; j++) {
                if (prev[i][j] == REACHED) setReachedAroundFrom(i, j, cur);
            }
        }
        return cur;
    }

    static char[][] doSteps(char[][] initial, int stepsCount) {
        char[][] prev;
        char[][] cur = initial;
        for (int i = 0; i < stepsCount; i++) {
            prev = cur;
            cur = doStep(prev);
        }
        return cur;
    }

    static long countPositions(char[][] map) {
        long sum = 0;
        for (var row: map) for (var c: row) if (c == REACHED) sum++;
        return sum;
    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        char[][] map = new char[lines.size()][lines.get(0).length()];
        for (int i = 0; i < lines.size(); i++) map[i] = lines.get(i).toCharArray();

        // replace 'S' to 'O'
        for (var row: map) for (int i = 0; i < row.length; i++) if (row[i] == STARTING_POSITION) row[i] = REACHED;

        char[][] resultMap = doSteps(map, 64);
        return countPositions(resultMap);
    }

}

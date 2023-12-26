import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AOC2023Day21Part2 {

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

    static void print(char[][] map) {
        System.out.println("----------------------------------------------------------------------");
        for (var row: map) System.out.println(new String(row));
        System.out.println("----------------------------------------------------------------------");
    }

    static char[][] doSteps(char[][] initial, int stepsCount) {
        char[][] prev;
        char[][] cur = initial;
        long p = 0;
        long c = countPositions(cur);
        for (int i = 1; i <= stepsCount; i++) {
            prev = cur;
            cur = doStep(prev);
            if ((i - 65) % 131 == 0) {
                p = c;
                c = countPositions(cur);
                System.out.println("c = " + c + " delta = " + (c - p));
            }
        }
        return cur;
    }

    static long countPositions(char[][] map) {
        long sum = 0;
        for (var row: map) for (var c: row) if (c == REACHED) sum++;
        return sum;
    }

    static int[] extractStartPosition(char[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == STARTING_POSITION) {
                    map[i][j] = GARDEN_PLOT;
                    return new int[] { i, j };
                }
            }
        }
        throw new RuntimeException("cant find STARTING_POSITION");
    }

    static char[][] replicate(int times, char[][] map) {
        int[] start = extractStartPosition(map);
        int n = times * 2 + 1;
        int rows = map.length;
        int columns = map[0].length;
        char[][] newMap = new char[rows * n][columns * n];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                for (int k = 0; k < n; k++) {
                    for (int l = 0; l < n; l++) {
                        newMap[k * rows + i][l * columns + j] = map[i][j];
                    }
                }
            }
        }
        newMap[times * rows + start[0]][times * columns + start[1]] = STARTING_POSITION;
        return newMap;
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        char[][] map = new char[lines.size()][lines.get(0).length()];
        for (int i = 0; i < lines.size(); i++) map[i] = lines.get(i).toCharArray();
        map = replicate(10, map);

        // replace 'S' to 'O'
        for (var row: map) for (int i = 0; i < row.length; i++) if (row[i] == STARTING_POSITION) row[i] = REACHED;

        char[][] resultMap = doSteps(map, 65 + 131 * 10);
        return countPositions(resultMap);
    }

}

// The output is the following for n1=(65 + 131*1), n2=(65 + 131*2), n3=(65 + 131*3)...
//c = 3755 delta = 3754
//c = 33494 delta = 29739
//c = 92811 delta = 59317
//c = 181706 delta = 88895
//c = 300179 delta = 118473
//c = 448230 delta = 148051
//c = 625859 delta = 177629
//c = 833066 delta = 207207
//c = 1069851 delta = 236785
//c = 1336214 delta = 266363
//c = 1632155 delta = 295941


// take counts:
// 33494 92811 181706 300179 448230 625859 833066 1069851 1336214 1632155
// and put it to wolfram alpha.
// It suggested the approx function for these numbers: 3755 + 14950 n + 14789 n^2
// Calculate function for n=202300. The answer is 605247138198755

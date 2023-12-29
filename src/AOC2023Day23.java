import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day23 {

    String filePath = "input/AOC2023Day23.input";

    static char FOREST = '#';
    static char PATH = '.';
    static char SLOPE_LR = '>';
    static char SLOPE_TD = 'v';

    static Set<Character> possibleChars = new HashSet<>(List.of(FOREST, PATH, SLOPE_LR, SLOPE_TD));

    record Pos(int x, int y) {
        Pos stepLeft() { return new Pos(x, y - 1); }
        Pos stepRight() { return new Pos(x, y + 1); }
        Pos stepTop() { return new Pos(x - 1, y); }
        Pos stepDown() { return new Pos(x + 1, y); }
    }

    record Field(char[][] map) {

        boolean inField(Pos p) {
            return 0 <= p.x && p.x < map.length && 0 <= p.y && p.y < map[0].length;
        }

        void checkAssumptions() {
            for (var row: map) for (char c: row) if (!possibleChars.contains(c)) throw new RuntimeException("unexpected char " + c);
        }

        static int path = -1;
        static int slopeLR = -2;
        static int slopeTD = -3;
        static int forest = -4;

        static int convert(char c) {
            return switch (c) {
                case 'v' -> slopeTD;
                case '>' -> slopeLR;
                case '.' -> path;
                case '#' -> forest;
                default -> throw new RuntimeException("unexpected symbol " + c);
            };
        }

        void populatePath(int[][] g, Pos start, int id) {
            Queue<Pos> queue = new LinkedList<>();
            queue.offer(start);
            while (!queue.isEmpty()) {
                Pos pos = queue.poll();
                if (g[pos.x][pos.y] == id) continue;
                g[pos.x][pos.y] = id;
                for (var nextPos: List.of(pos.stepLeft(), pos.stepRight(), pos.stepTop(), pos.stepDown())) {
                    if (inField(nextPos) && g[nextPos.x][nextPos.y] == path) queue.offer(nextPos);
                }
            }

        }

        int reduceGraphAndCount() {
            // create int field. Just copy
            int[][] components = new int[map.length][map[0].length];
            for (int i = 0; i < components.length; i++) {
                for (int j = 0; j < components[0].length; j++) {
                    components[i][j] = convert(map[i][j]);
                }
            }
            // mark components with different ids
            int idsCount = 0;
            for (int i = 0; i < components.length; i++) {
                for (int j = 0; j < components[0].length; j++) {
                    if (components[i][j] != path) continue;
                    populatePath(components, new Pos(i, j), idsCount++);
                }
            }
            int idStart = components[0][1];
            int idEnd = components[components.length - 1][components[0].length - 2];
            System.out.println("idStart = %d and idEnd = %d".formatted(idStart, idEnd));
            // init component size. How many path cells in each component
            int[] componentSize = new int[idsCount];
            Arrays.fill(componentSize, 0);
            for (var row: components) for (var cell: row) if (0 <= cell && cell < idsCount) componentSize[cell]++;
            // init edges. For each node keep the list of next nodes
            List<Integer>[] edges = new List[idsCount];
            for (int i = 0; i < edges.length; i++) edges[i] = new LinkedList<>();
            for (int i = 0; i < components.length; i++) {
                for (int j = 0; j < components[0].length; j++) {
                    if (components[i][j] == slopeLR) {
                        int idFrom = components[i][j - 1];
                        int idTo = components[i][j + 1];
                        edges[idFrom].add(idTo);
                    } else if (components[i][j] == slopeTD) {
                        int idFrom = components[i - 1][j];
                        int idTo = components[i + 1][j];
                        edges[idFrom].add(idTo);
                    }
                }
            }

            // print map with components
            for (var row: components) System.out.println(Arrays.toString(row));
            System.out.println();
            for (int i = 0; i < idsCount; i++) {
                System.out.println("%d (size=%d) has next nodes: ".formatted(i, componentSize[i]) + edges[i]);
            }
            System.out.println();
            // print graph for mermaid chart
            for (int i = 0; i < edges.length; i++) {
                for (var nextNode: edges[i]) System.out.println("%d ---> %d".formatted(i, nextNode));
            }

            int[] dist = new int[idsCount];
            Arrays.fill(dist, Integer.MIN_VALUE);
            dist[0] = componentSize[0] - 1; // Start node shouldn't be counted
            // k times: relax every edge. Bellman-Ford algorithm
            for (int k = 0; k < dist.length; k++) {
                for (int u = 0; u < edges.length; u++) {
                    for (var v: edges[u]) {
                        int newLength = dist[u] + componentSize[v] + 1;
                        if (dist[v] < newLength) dist[v] = newLength;
                    }
                }
            }
            System.out.println();
            System.out.println("max length from start node %d".formatted(idStart) + " to all other nodes:");
            for (int i = 0; i < dist.length; i++) System.out.println(i + ": " + dist[i]);

            return dist[idEnd];
        }

    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        char[][] map = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) map[i] = lines.get(i).toCharArray();
        Field field = new Field(map);
        field.checkAssumptions();

        return field.reduceGraphAndCount();
    }

}

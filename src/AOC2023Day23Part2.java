import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day23Part2 {

    String filePath = "input/AOC2023Day23.input";

    static char FOREST = '#';
    static char PATH = '.';
    static char SLOPE_LR = '>';
    static char SLOPE_TD = 'v';
    static char FORK = '+';

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

        void eraseSlopes() {
            for (var row: map) {
                for (int i = 0; i < row.length; i++) {
                    if (row[i] == SLOPE_LR || row[i] == SLOPE_TD) row[i] = PATH;
                }
            }
        }

        boolean atLeast3DirectionsAround(Pos pos) {
            int pathAround = 0;
            for (var nextPos: List.of(pos.stepLeft(), pos.stepRight(), pos.stepTop(), pos.stepDown())) {
                if (inField(nextPos) && map[nextPos.x][nextPos.y] == PATH) pathAround++;
            }
            return pathAround >= 3;
        }

        void markForks() {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    Pos pos = new Pos(i, j);
                    if (map[i][j] == PATH && atLeast3DirectionsAround(pos)) map[i][j] = FORK;
                }
            }
        }

        void markForkAsPath(int[][] components) {
            for (int i = 0; i < components.length; i++) {
                for (int j = 0; j < components[0].length; j++) {
                    if (components[i][j] == fork) components[i][j] = path;
                }
            }
        }

        static int path = -1;
        static int fork = -2;
        static int forest = -3;

        static int convert(char c) {
            return switch (c) {
                case '+' -> fork;
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
            markForkAsPath(components);
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
            Set<Integer>[] edges = new Set[idsCount];
            for (int i = 0; i < edges.length; i++) edges[i] = new HashSet<>();
            for (int i = 0; i < components.length; i++) {
                for (int j = 0; j < components[0].length; j++) {
                    if (map[i][j] == FORK) {
                        Pos pos = new Pos(i, j);
                        for (var nextPos: List.of(pos.stepLeft(), pos.stepRight(), pos.stepTop(), pos.stepDown())) {
                            if (inField(nextPos) && map[nextPos.x][nextPos.y] == PATH) {
                                int curId = components[i][j];
                                int nextId = components[nextPos.x][nextPos.y];
                                edges[curId].add(nextId);
                                edges[nextId].add(curId);
                            }
                        }
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
                for (var nextNode: edges[i]) {
                    System.out.println("%d ---> %d".formatted(i, nextNode));
                }
            }

            Set<Integer> visited = new HashSet<>();
            int startNodeSize = componentSize[idStart] - 1; // S position shouldn't be counted
            return startNodeSize + maxLengthToTarget(idStart, idEnd, visited, edges, componentSize);
        }

        // brute force
        // This would work for Part 1 as well...
        int maxLengthToTarget(int node, int target, Set<Integer> visited, Set<Integer>[] edges, int[] nodeSize) {
            if (node == target) return 0;
            visited.add(node);
            int max = -1;
            for (var nextNode: edges[node]) {
                if (!visited.contains(nextNode)) {
                    int maxNext = maxLengthToTarget(nextNode, target, visited, edges, nodeSize);
                    if (maxNext < 0) continue;
                    max = Math.max(max, maxNext + nodeSize[nextNode]);
                }
            }
            visited.remove(node);
            return max;
        }

    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        char[][] map = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) map[i] = lines.get(i).toCharArray();
        Field field = new Field(map);
        field.checkAssumptions();
        field.eraseSlopes();
        field.markForks();

        return field.reduceGraphAndCount();
    }

}

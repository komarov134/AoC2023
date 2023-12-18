import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day17 {

    String filePath = "input/AOC2023Day17.input";


    enum D {
        WEST, NORTH, EAST, SOUTH;
    }

    record Pos(int i, int j) {
        Pos next(D d) {
            return switch (d) {
                case WEST -> new Pos(i, j - 1);
                case NORTH -> new Pos(i - 1, j);
                case EAST -> new Pos(i, j + 1);
                case SOUTH -> new Pos(i + 1, j);
            };
        }
    }


    record Key(Pos pos, D direction, int steps) {}

    record QueueData(Key key, int cost) {}

    // It could be a binary heap
    static class MinQueue {

        Map<Key, Integer> map = new HashMap<>();

        QueueData extractMin() {
            if (map.isEmpty()) throw new RuntimeException("empty");
            QueueData min = new QueueData(null, Integer.MAX_VALUE);
            for (var e: map.entrySet()) {
                if (e.getValue() < min.cost) min = new QueueData(e.getKey(), e.getValue());
            }
            map.remove(min.key);
            return min;
        }

        void putMin(Key key, int cost) {
            var prevOrNull = map.remove(key);
            var newCost = (prevOrNull == null) ? cost : Math.min(prevOrNull, cost);
            map.put(key, newCost);
        }

        boolean isEmpty() {
            return map.isEmpty();
        }
    }

    static List<D> possibleDirections(D d) {
        return switch (d) {
            case WEST -> List.of(D.WEST, D.NORTH, D.SOUTH);
            case NORTH -> List.of(D.NORTH, D.WEST, D.EAST);
            case EAST -> List.of(D.EAST, D.NORTH, D.SOUTH);
            case SOUTH -> List.of(D.SOUTH, D.WEST, D.EAST);
        };
    }

    record Field(int[][] map, Set<Key> visited) {

        void markVisited(Key key) {
            visited.add(key);
        }

        boolean isVisited(Key key) {
            return visited.contains(key);
        }

        boolean valid(Pos p) {
            return 0 <= p.i && p.i < map.length && 0 <= p.j && p.j < map[0].length;
        }

        int weight(Pos pos) {
            return map[pos.i][pos.j];
        }

        long leastHeatLoss() {
            MinQueue q = new MinQueue();
            Pos initialPos = new Pos(0, 0);
            q.putMin(new Key(initialPos, D.EAST, 0), 0);
            while (!q.isEmpty()) {
                var data = q.extractMin();
                var dk = data.key;
                markVisited(dk);
                if (dk.pos.i == map.length - 1 && dk.pos.j == map[0].length - 1) {
                    return data.cost;
                }
                for (D nextDirection: possibleDirections(dk.direction)) {
                    int steps = (dk.direction == nextDirection) ? dk.steps + 1 : 0;
                    Pos newPos = dk.pos.next(nextDirection);
                    Key newKey = new Key(newPos, nextDirection, steps);
                    if (steps <= 2 && valid(newPos) && !isVisited(newKey)) {
                        q.putMin(newKey, data.cost + weight(newPos));
                    }
                }
            }

            throw new RuntimeException("Can't find");
        }
    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        int rows = lines.size();
        int columns = lines.get(0).length();
        int[][] map = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                map[i][j] = Integer.parseInt("" + lines.get(i).charAt(j));
            }
        }
        Field field = new Field(map, new HashSet<>());

        return field.leastHeatLoss();
    }

}

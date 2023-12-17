import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day16 {

    String filePath = "input/AOC2023Day16.input";

    enum TileType {
        EMPTY('.'), PIPE('|'), DASH('-'), SLASH('/'), BACKSLASH('\\');

        public final char c;

        TileType(char c) {
            this.c = c;
        }
    }

    static TileType buildTileType(char c) {
        return Arrays.stream(TileType.values()).filter(t -> t.c == c).findFirst().get();
    }

    enum D {
        WEST, NORTH, EAST, SOUTH
    }


    record Pos(int i, int j) {}

    record Routing(D comesFrom, List<D> goesTo) {}

    record TileRouting(TileType type, List<Routing> routingList) {}

    static final List<TileRouting> routes = buildRouting();

    static List<TileRouting> buildRouting() {
        return List.of(
                new TileRouting(
                        TileType.EMPTY, List.of(
                                new Routing(D.WEST, List.of(D.EAST)),
                                new Routing(D.NORTH, List.of(D.SOUTH)),
                                new Routing(D.EAST, List.of(D.WEST)),
                                new Routing(D.SOUTH, List.of(D.NORTH))
                        )
                ),
                new TileRouting(
                        TileType.PIPE, List.of(
                            new Routing(D.WEST, List.of(D.NORTH, D.SOUTH)),
                            new Routing(D.NORTH, List.of(D.SOUTH)),
                            new Routing(D.EAST, List.of(D.NORTH, D.SOUTH)),
                            new Routing(D.SOUTH, List.of(D.NORTH))
                    )
                ),
                new TileRouting(
                        TileType.DASH, List.of(
                            new Routing(D.WEST, List.of(D.EAST)),
                            new Routing(D.NORTH, List.of(D.WEST, D.EAST)),
                            new Routing(D.EAST, List.of(D.WEST)),
                            new Routing(D.SOUTH, List.of(D.WEST, D.EAST))
                    )
                ),
                new TileRouting(
                        TileType.SLASH, List.of(
                            new Routing(D.WEST, List.of(D.NORTH)),
                            new Routing(D.NORTH, List.of(D.WEST)),
                            new Routing(D.EAST, List.of(D.SOUTH)),
                            new Routing(D.SOUTH, List.of(D.EAST))
                    )
                ),
                new TileRouting(
                        TileType.BACKSLASH, List.of(
                            new Routing(D.WEST, List.of(D.SOUTH)),
                            new Routing(D.NORTH, List.of(D.EAST)),
                            new Routing(D.EAST, List.of(D.NORTH)),
                            new Routing(D.SOUTH, List.of(D.WEST))
                    )
                )
        );
    }


    // nextTiles[DIR] contains list of tiles where the beam goes if it comes from DIR
    record Tile(TileType type) {

        // if the beam 'comesFrom' then it 'goesTo' (It's possible to go to many directions at the same time)
        List<D> goesTo(D comesFrom) {
            return routes.stream().filter(t -> t.type == type)
                    .flatMap(t -> t.routingList.stream().filter(r -> r.comesFrom == comesFrom))
                    .findFirst().get().goesTo;
        }

    }

    static Pos step(D direction, Pos p) {
        return switch (direction) {
            case WEST -> new Pos(p.i, p.j - 1);
            case NORTH -> new Pos(p.i - 1, p.j);
            case EAST -> new Pos(p.i, p.j + 1);
            case SOUTH -> new Pos(p.i + 1, p.j);
        };
    }

    record Step(Pos previous, Pos current) {

        D comeFrom() {
            for (D d: D.values()) {
                var next = step(d, current);
                if (previous.equals(next)) return d;
            }
            throw new RuntimeException("wrong prev and cur pairs");
        }

    }

    record VisitedFrom(boolean[] visitedFrom) {
        void markVisited(D from) {
            visitedFrom[from.ordinal()] = true;
        }

        boolean isVisited(D from) {
            return visitedFrom[from.ordinal()];
        }

        boolean isVisited() {
            for (var v: visitedFrom) if (v) return true;
            return false;
        }
    }

    static VisitedFrom buildVisitedFrom() {
        return new VisitedFrom(new boolean[D.values().length]);
    }

    record Field(Tile[][] map, VisitedFrom[][] visited) {

        // from any side
        boolean isVisited(Pos p) {
            return visited[p.i][p.j].isVisited();
        }

        boolean isVisited(Step step) {
            var p = step.current;
            return visited[p.i][p.j].isVisited(step.comeFrom());
        }

        void markVisited(Step step) {
            var p = step.current;
            visited[p.i][p.j].markVisited(step.comeFrom());
        }

        boolean fieldContains(Pos p) {
            return 0 <= p.i && p.i < map.length && 0 <= p.j && p.j < map[0].length;
        }

        List<Pos> next(Step step) {
            List<Pos> nextPositions = new LinkedList<>();
            var p = step.current;
            for (D d: map[p.i][p.j].goesTo(step.comeFrom())) {
                var nextPos = step(d, p);
                if (fieldContains(nextPos)) nextPositions.add(nextPos);
            }
            return nextPositions;
        }


        void propagateBeam() {

            Queue<Step> q = new LinkedList<>();
            q.offer(new Step(new Pos(0, -1), new Pos(0, 0)));
            while (!q.isEmpty()) {
                var step = q.poll();
                markVisited(step);
                for (var nextPos: next(step)) {
                    var nextStep = new Step(step.current, nextPos);
                    if (!isVisited(nextStep)) q.offer(nextStep);
                }
            }

        }

        long countVisited() {
            long sum = 0;
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    if (isVisited(new Pos(i, j))) sum++;
                }
            }
            return sum;
        }

    }

    static Field buildField(List<String> lines) {
        int rows = lines.size();
        int columns = lines.get(0).length();
        Tile[][] map = new Tile[rows][columns];
        VisitedFrom[][] visited = new VisitedFrom[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                map[i][j] = new Tile(buildTileType(lines.get(i).charAt(j)));
                visited[i][j] = buildVisitedFrom();
            }
        }
        return new Field(map, visited);
    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        Field field = buildField(lines);
        field.propagateBeam();
        return field.countVisited();
    }

}

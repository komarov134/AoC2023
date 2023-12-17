import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day10 {

    String filePath = "input/AOC2023Day10.input";

    // | is a vertical pipe connecting north and south.
    // - is a horizontal pipe connecting east and west.
    // L is a 90-degree bend connecting north and east.
    // J is a 90-degree bend connecting north and west.
    // 7 is a 90-degree bend connecting south and west.
    // F is a 90-degree bend connecting south and east.
    // . is ground; there is no pipe in this tile.
    // S is the starting position of the animal; there is a pipe on this tile, but your sketch doesn't show what shape the pipe has.

    record Position(int i, int j) {
        Position move(Direction d) {
            return new Position(i + d.dx, j + d.dy);
        }
    }

    enum Direction {
        LEFT(0, -1), RIGHT(0, +1), UP(-1, 0), DOWN(+1, 0);

        final int dx;
        final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

    }

    enum Tile {
        START('S'),
        GROUND('.'),
        UD('|'),
        LR('-'),
        UR('L'),
        UL('J'),
        BL('7'),
        BR('F');

        final char symbol;

        Tile(char symbol) {
            this.symbol = symbol;
        }

        List<Position> connections(Position pos, Position excluding) {
            return connections(pos).stream().filter(p -> !p.equals(excluding)).toList();
        }

        List<Position> connections(Position pos) {
            return switch (this) {
                case START -> throw new RuntimeException("Asked connection on START tile");
                case GROUND -> List.of();
                case UD -> List.of(pos.move(Direction.UP), pos.move(Direction.DOWN));
                case LR -> List.of(pos.move(Direction.LEFT), pos.move(Direction.RIGHT));
                case UR -> List.of(pos.move(Direction.UP), pos.move(Direction.RIGHT));
                case UL -> List.of(pos.move(Direction.UP), pos.move(Direction.LEFT));
                case BL -> List.of(pos.move(Direction.DOWN), pos.move(Direction.LEFT));
                case BR -> List.of(pos.move(Direction.DOWN), pos.move(Direction.RIGHT));
            };
        }


    }

    record Field(Tile[][] map) {

        Tile get(Position p) {
            return map[p.i][p.j];
        }

        Position findStartPosition() {
            int rows = map.length;
            int columns = map[0].length;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    if (map[i][j] == Tile.START) return new Position(i, j);
                }
            }
            throw new RuntimeException("Couldn't find start position");
        }

    }

    long farthestPosition(Field field) {
        Position startPosition = field.findStartPosition();
        System.out.println("startPosition: " + startPosition);

        // I checked it in input data MANUALLY
        Tile startTile = Tile.UR; // long input
//        Tile startTile = Tile.BR;   // short test input

        // it's enough to consider the one
        Position startNext = startTile.connections(startPosition).get(0);
        System.out.println("startNext: " + startNext);

        Position currentPos = startNext;
        Position prevPos = startPosition;

        long steps = 1;
        while (field.get(currentPos) != Tile.START) {
            List<Position> nextPositions = field.get(currentPos).connections(currentPos, prevPos);
            // all pipes have 2 sides
            assert nextPositions.size() == 1 : "nextPositions: " + nextPositions;
            Position nextPos = nextPositions.get(0);
            // it must contain connections unless it's START tile
            if (field.get(nextPos) != Tile.START) {
                assert field.get(nextPos).connections(nextPos).contains(currentPos);
            }
            prevPos = currentPos;
            currentPos = nextPos;
            steps++;

        }
        System.out.println("steps count: " + steps);

        return steps / 2;
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        int rows = lines.size();
        int columns = lines.get(0).length();

        Tile[][] map = new Tile[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                char c = lines.get(i).charAt(j);
                Tile tile = Arrays.stream(Tile.values()).filter(t -> t.symbol == c).findFirst().get();
                map[i][j] = tile;
            }
        }

        return farthestPosition(new Field(map));
    }

}

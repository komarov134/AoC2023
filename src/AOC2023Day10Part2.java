import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day10Part2 {

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
        LEFT(0, -1), RIGHT(0, +1), UP(-1, 0), DOWN(+1, 0),
        LEFT_UP(-1, -1), RIGHT_UP(-1, +1), LEFT_DOWN(+1, -1), RIGHT_DOWN(+1, +1);

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

        List<String> expended() {
            return switch (this) {
                case START ->
                        List.of(
                                "   ",
                                " S ",
                                "   "
                        );
                case GROUND ->
                        List.of(
                                "   ",
                                " . ",
                                "   "
                        );
                case UD ->
                        List.of(
                                " * ",
                                " * ",
                                " * "
                        );
                case LR ->
                        List.of(
                                "   ",
                                "***",
                                "   "
                        );
                case UR ->
                        List.of(
                                " * ",
                                " **",
                                "   "
                        );
                case UL ->
                        List.of(
                                " * ",
                                "** ",
                                "   "
                        );
                case BL ->
                        List.of(
                                "   ",
                                "** ",
                                " * "
                        );
                case BR ->
                        List.of(
                                "   ",
                                " **",
                                " * "
                        );
            };
        }


    }

    record Field(char[][] map) {

        boolean isVisited(Position pos) {
            return map[pos.i][pos.j] == '0';
        }

        boolean isFilled(Position pos) {
            return map[pos.i][pos.j] == '@';
        }

        boolean isPipe(Position pos) {
            return map[pos.i][pos.j] == '*';
        }

        boolean spaceOrGround(Position pos) {
            return map[pos.i][pos.j] == ' ' || map[pos.i][pos.j] == '.' || isPipe(pos);
        }

        boolean contains(Position pos) {
            return pos.i >= 0 && pos.i < map.length && pos.j >= 0 && pos.j < map[0].length;
        }

        void markVisited(Position pos) {
            map[pos.i][pos.j] = '0';
        }

        void markFilled(Position pos) {
            map[pos.i][pos.j] = '@';
        }

        void print() {
            for (char[] row: map) {
                System.out.println(new String(row));
            }
        }

        long countGroundAndPipes() {
            long sum = 0;
            for (char[] row: map) {
                for (char c: row) {
                    if (c == '.') sum += 3;
                    if (c == '*') sum += 1;
                }
            }
            return sum / 3;
        }

        // fill pipe with symbol '@'
        void fillPipe(Position start) {
            Queue<Position> q = new LinkedList<>();
            q.add(start);
            while (!q.isEmpty()) {
                Position pos = q.poll();
                if (isFilled(pos)) continue;
                markFilled(pos);
                for (Direction d: Direction.values()) {
                    Position nextPos = pos.move(d);
                    if (contains(nextPos) && isPipe(nextPos)) q.offer(nextPos);
                }
            }
        }


        // @ - filled pipe
        // * - pipe outside main loop
        // . - ground
        // ' ' - space
        void eatGroundAndPipeOutside() {
            Queue<Position> q = new LinkedList<>();
            q.add(new Position(0, 0));  // after expanding it's a whitespace ' ' symbol, we are sure
            while (!q.isEmpty()) {
                Position pos = q.poll();
                if (isVisited(pos)) continue;
                markVisited(pos);
                for (Direction d: Direction.values()) {
                    Position nextPos = pos.move(d);
                    if (contains(nextPos) && spaceOrGround(nextPos)) {
                        q.offer(nextPos);
                    }
                }
            }
        }

    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        int rows = lines.size();
        int columns = lines.get(0).length();

        char[][] map = new char[rows * 3][columns * 3];
        Position startPosition = null;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                char c = lines.get(i).charAt(j);
                Tile tile = Arrays.stream(Tile.values()).filter(t -> t.symbol == c).findFirst().get();
                if (tile == Tile.START) {
                    startPosition = new Position(3 * i + 1, 3 * j + 1);
                    tile = Tile.UR;   // replace S MANUALLY. long input
//                    tile = Tile.BL;   // replace S MANUALLY. test short input
                }
                List<String> exp = tile.expended();
                for (int di = 0; di < 3; di++) {
                    for (int dj = 0; dj < 3; dj++) {
                        map[3 * i + di][3 * j + dj] = exp.get(di).charAt(dj);
                    }
                }
            }
        }

        Field field = new Field(map);

        field.print();
        System.out.println("------------------------------------------------------------------------------------------");
        field.fillPipe(startPosition);
        field.print();
        System.out.println("------------------------------------------------------------------------------------------");
        field.eatGroundAndPipeOutside();
        field.print();
        System.out.println("------------------------------------------------------------------------------------------");

        return field.countGroundAndPipes();
    }

}

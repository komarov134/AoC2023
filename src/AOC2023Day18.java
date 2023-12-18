import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day18 {

    String filePath = "input/AOC2023Day18.input";


    enum D {
        R, D, L, U;
    }

    record Pos(int i, int j) {
        Pos next(D d) {
            return switch (d) {
                case L -> new Pos(i, j - 1);
                case U -> new Pos(i - 1, j);
                case R -> new Pos(i, j + 1);
                case D -> new Pos(i + 1, j);
            };
        }
    }

    static char GROUND = '.';
    static char DIG = '#';
    static char FILLING = '@';

    record Command(D direction, long steps) {}

    Command parseCommand(String s) {
        var arr = s.split(" ");
        return new Command(D.valueOf(arr[0]), Integer.parseInt(arr[1]));
    }

    record Field(char[][] map) {

        boolean contains(Pos p) {
            return 0 <= p.i && p.i < map.length && 0 <= p.j && p.j < map[0].length;
        }

        void dig(Pos p) {
            map[p.i][p.j] = DIG;
        }

        void fill(Pos p) {
            map[p.i][p.j] = FILLING;
        }

        boolean isFilling(Pos p) {
            return map[p.i][p.j] == FILLING;
        }

        boolean isGround(Pos p) {
            return map[p.i][p.j] == GROUND;
        }

        void initGround() {
            for (var row: map) Arrays.fill(row, GROUND);
        }

        void runCommands(List<Command> commands, Pos start) {
            Pos current = start;
            for (var c: commands) {
                current = dig(c, current);
            }
        }

        Pos dig(Command c, Pos p) {
            dig(p);
            Pos current = p;
            for (int i = 0; i < c.steps; i++) {
                current = current.next(c.direction);
                dig(current);
            }
            return current;
        }

        void fillOutside() {
            Pos start = new Pos(0, 0);
            Queue<Pos> q = new LinkedList<>();
            q.offer(start);
            while (!q.isEmpty()) {
                Pos p = q.poll();
                if (isFilling(p)) continue;
                fill(p);
                for (D d: D.values()) {
                    var nextPos = p.next(d);
                    if (contains(nextPos) && isGround(nextPos)) q.offer(nextPos);
                }
            }
        }

        long countDig() {
            long sum = 0;
            for (var row: map) for (var c: row) if (c != FILLING) sum++;
            return sum;
        }

        void print() {
            for (var row: map) System.out.println(new String(row));
            System.out.println(String.join("", Collections.nCopies(100, "-")));
        }
    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        var commands = lines.stream().map(s -> parseCommand(s)).toList();

        char[][] map = new char[500][500];
        Field field = new Field(map);
        field.initGround();
        field.runCommands(commands, new Pos(250, 250));
        field.print();
        field.fillOutside();
        field.print();

        return field.countDig();
    }

}

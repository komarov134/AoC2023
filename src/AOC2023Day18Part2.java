import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day18Part2 {

    String filePath = "input/AOC2023Day18.input";

    enum D {
        R, D, L, U;
    }

    record Pos(long i, long j) {
        Pos next(D d, long steps) {
            return switch (d) {
                case L -> new Pos(i, j - steps);
                case U -> new Pos(i - steps, j);
                case R -> new Pos(i, j + steps);
                case D -> new Pos(i + steps, j);
            };
        }
    }

    record Command(D direction, long steps) {}

    static List<Pos> makeCoords(List<Command> commands, Pos start) {
        List<Pos> coords = new ArrayList<>();
        coords.add(start);
        Pos current = start;
        for (var c: commands) {
            current = current.next(c.direction, c.steps);
            coords.add(current);
        }
        assert coords.getLast().equals(coords.getFirst());
        return coords;
    }

    // trapezoid method
    static long square(List<Pos> coords) {
        long sum = 0;
        for (int i = 0; i < coords.size() - 1; i++) {
            Pos p1 = coords.get(i);
            Pos p2 = coords.get(i + 1);
            sum += (p2.i - p1.i) * (p1.j + p2.j);
        }
        return sum / 2;
    }

    static long perimeter(List<Pos> coords) {
        long sum = 0;
        for (int i = 0; i < coords.size() - 1; i++) {
            Pos p1 = coords.get(i);
            Pos p2 = coords.get(i + 1);
            assert p1.i == p2.i || p1.j == p2.j;
            sum += Math.abs(p2.i - p1.i) + Math.abs(p2.j - p1.j);
        }
        return sum;
    }

    // R 6 (#70c710)
    // 70c710 is steps=hex(70c71) and direction=0
    Command parseCommand(String s) {
        var arr = s.split(" ");
        String hex = arr[2].substring(2, arr[2].length() - 1);   // (#70c710) -> 70c710
        int lastIndex = hex.length() - 1;
        long steps = Long.parseLong(hex.substring(0, lastIndex), 16);
        D direction = D.values()[Integer.parseInt(hex.substring(lastIndex))];

        return new Command(direction, steps);
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        var commands = lines.stream().map(s -> parseCommand(s)).toList();
        var coords = makeCoords(commands, new Pos(0, 0));

        // As the line goes through the block center we have to adjust square.
        // We need to take the half more for each block (use perimeter).
        // And for 4 corner blocks we need to take 0.75 more. We already took 0.5 for each in perimeter.
        // And we need to take 4 * 0.25 more
        return square(coords) + perimeter(coords) / 2 + 1;
    }

}

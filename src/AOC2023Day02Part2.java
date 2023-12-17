import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AOC2023Day02Part2 {

    String filePath = "input/AOC2023Day02.input";

    record Color(String name) {}

    record Handful(Map<Color, Integer> counts) {
        Handful maximizeCounts(Handful other) {
            Map<Color, Integer> c = new HashMap<>();
            for (Map.Entry<Color, Integer> e: other.counts.entrySet()) {
                int maxCnt = Math.max(e.getValue(), this.counts.getOrDefault(e.getKey(), 0));
                c.put(e.getKey(), maxCnt);
            }
            return new Handful(c);
        }

        int power() {
            return counts.values().stream().reduce(1, (a, b) -> a * b);
        }

    }

    record Game(int id, List<Handful> handfuls) {
        Handful minPossible() {
            // hardcode is not good here...
            Map<Color, Integer> maxMap = new HashMap<>();
            maxMap.put(new Color("red"), 0);
            maxMap.put(new Color("green"), 0);
            maxMap.put(new Color("blue"), 0);
            Handful min = new Handful(maxMap);

            return handfuls.stream().reduce(min, (a, b) -> b.maximizeCounts(a));
        }
    }


    Handful parseHandful(String s) {
        String[] colorsStr = s.split(",");
        Map<Color, Integer> colors = new HashMap<>();
        for (String colorStr: colorsStr) {
            // replace all non-word letters
            String name = colorStr.replaceAll("\\W", "").replaceAll("\\d", "");
            // replace all non digits
            int count = Integer.parseInt(colorStr.replaceAll("\\D", ""));
            colors.put(new Color(name), count);
        }
        return new Handful(colors);
    }

    // Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
    Game parse(String line) {
        String[] parts = line.split(":");
        // replace all non-digits
        int gameId = Integer.parseInt(parts[0].replaceAll("\\D", ""));
        String[] handfulsStr = parts[1].split(";");
        List<Handful> handfuls = Arrays.stream(handfulsStr)
                .map(this::parseHandful)
                .toList();

        Game g = new Game(gameId, handfuls);
        return g;
    }

    public int solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        int sum = 0;



        for (String line: lines) {
            Game game = parse(line);
            Handful h = game.minPossible();
            sum += h.power();
        }

        return sum;
    }

}

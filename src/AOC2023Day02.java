import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AOC2023Day02 {

    String filePath = "input/AOC2023Day02.input";

    record Color(String name) {}

    record Handful(Map<Color, Integer> counts) {
        boolean isCoveredBy(Handful other) {
            if (!other.counts.keySet().containsAll(counts.keySet())) return false;
            for (Map.Entry<Color, Integer> e: counts.entrySet()) {
                int maxCount = other.counts.getOrDefault(e.getKey(), 0);
                if (e.getValue() > maxCount) return false;
            }
            return true;
        }
    }
    record Game(int id, List<Handful> handfuls) {
        boolean canBe(Handful maxHandful) {
            return handfuls.stream().allMatch(h -> h.isCoveredBy(maxHandful));
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

        Map<Color, Integer> maxMap = new HashMap<>();
        maxMap.put(new Color("red"), 12);
        maxMap.put(new Color("green"), 13);
        maxMap.put(new Color("blue"), 14);
        Handful maxHandful = new Handful(maxMap);

        for (String line: lines) {
            Game game = parse(line);
            int points = game.canBe(maxHandful) ? game.id : 0;
            sum += points;
        }

        return sum;
    }

}

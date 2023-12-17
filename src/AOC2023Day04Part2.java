import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day04Part2 {

    String filePath = "input/AOC2023Day04.input";


    record Card(int id, List<Integer> winningNumbers, List<Integer> numbers) {
        long points() {
            return numbers.stream().filter(winningNumbers::contains).count();
        }
    }

    Card parse(String line) {
        String[] parts = line.split(":");
        int id = Integer.parseInt(parts[0].replaceAll("\\D+", ""));
        String[] parts2 = parts[1].split("\\|");
        List<Integer> winningNumbers = Arrays.stream(parts2[0].trim().split("\\D+")).map(Integer::parseInt).toList();
        List<Integer> numbers = Arrays.stream(parts2[1].trim().split("\\D+")).map(Integer::parseInt).toList();

        return new Card(id, winningNumbers, numbers);
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        long[] copies = new long[lines.size()];
        Arrays.fill(copies, 1L);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Card card = parse(line);
            long points = card.points();
            for (int p = i + 1; p < i + 1 + points; p++) copies[p] += copies[i];
        }

        return Arrays.stream(copies).reduce(0L, Long::sum);
    }

}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day04 {

    String filePath = "input/AOC2023Day04.input";


    record Card(int id, List<Integer> winningNumbers, List<Integer> numbers) {
        long points() {
            long matchedNumbersCount = numbers.stream().filter(winningNumbers::contains).count();
            return matchedNumbersCount == 0 ? 0 : 1L << (matchedNumbersCount - 1);
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

        long sum = 0;
        for (String line: lines) {
            Card card = parse(line);
            sum += card.points();
        }
        return sum;
    }

}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AOC2023Day01 {

    String filePath = "input/AOC2023Day01.input";

    public int solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        int sum = 0;
        for (String line: lines) {
            char firstDigit = '?';
            char secondDigit = '?';
            for (char c: line.toCharArray()) {
                if (Character.isDigit(c)) {
                    if (firstDigit == '?') firstDigit = c;
                    secondDigit = c;
                }
            }
            int number = Integer.parseInt("" + firstDigit + secondDigit);
            sum += number;
        }

        return sum;
    }

}

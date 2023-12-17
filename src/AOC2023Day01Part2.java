import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AOC2023Day01Part2 {

    String filePath = "input/AOC2023Day01Part2.input";

    public record DigitWord(String word, char digit) {}

    private DigitWord[] words = new DigitWord[] {
            new DigitWord("one", '1'),
            new DigitWord("two", '2'),
            new DigitWord("three", '3'),
            new DigitWord("four", '4'),
            new DigitWord("five", '5'),
            new DigitWord("six", '6'),
            new DigitWord("seven", '7'),
            new DigitWord("eight", '8'),
            new DigitWord("nine", '9'),
    };

    // matches digits words to the line starting from leftIndex
    // returns '?' if no match found
    private char findDigitWord(int leftIndex, String line) {
        for (DigitWord word: words) {
            int rightIndexExcl = leftIndex + word.word.length();
            if (rightIndexExcl <= line.length() && line.substring(leftIndex, rightIndexExcl).equals(word.word)) {
                return word.digit;
            }
        }
        return '?';
    }

    public int solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        int sum = 0;
        for (String line: lines) {
            char firstDigit = '?';
            char secondDigit = '?';
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                char foundDigit = Character.isDigit(c) ? c : findDigitWord(i, line);
                if (Character.isDigit(foundDigit)) {
                    if (firstDigit == '?') firstDigit = foundDigit;
                    secondDigit = foundDigit;
                }
            }

            int number = Integer.parseInt("" + firstDigit + secondDigit);
            sum += number;
        }

        return sum;
    }

}

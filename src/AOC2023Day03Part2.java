import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day03Part2 {

    String filePath = "input/AOC2023Day03.input";


    // start/end indexes (inclusive) of the number in the row
    record NumberCoords(int row, int start, int end) {

        long toLong(char[][] m) {
            String numberStr = new String(Arrays.copyOfRange(m[row], start, end + 1));
            return Long.parseLong(numberStr);
        }

    }

    record Field(char[][] m) {

        NumberCoords detectNumber(int i, int j) {
            if (!Character.isDigit(m[i][j])) throw new RuntimeException("not a digit");
            int start = j;
            while (start - 1 >= 0 && Character.isDigit(m[i][start - 1])) start--;
            int end = start;
            while (end + 1 < m[i].length && Character.isDigit(m[i][end + 1])) end++;
            return new NumberCoords(i, start, end);
        }

        // all numbers around the gear on the ij position
        List<Long> getAllNumbersAround(int i, int j) {
            List<Long> numbers = new ArrayList<>();

            // handle m[i][j-1] and m[i][j+1]
            for (int dy: new int[] { -1, +1 }) {
                if (Character.isDigit(m[i][j + dy])) {
                    NumberCoords nc = detectNumber(i, j + dy);
                    numbers.add(nc.toLong(m));
                }
            }
            // handle up and down rows
            // up row:   m[i-1][j-1], m[i-1][j], m[i-1][j+1]
            // down row: m[i+1][j-1], m[i+1][j], m[i+1][j+1]
            for (int dx: new int[] { -1, +1 }) {
                for (int c = j - 1; c <= j + 1; c++) {
                    if (Character.isDigit(m[i + dx][c])) {
                        NumberCoords nc = detectNumber(i + dx, c);
                        numbers.add(nc.toLong(m));
                        c = nc.end;
                    }
                }
            }

            return numbers;
        }

        long sumAllGears() {
            long sum = 0;
            for (int i = 0; i < m.length; i++) {
                int columnsNumber = m[i].length;
                for (int j = 0; j < columnsNumber; j++) {
                    if (m[i][j] == '*') {
                        List<Long> numbers = getAllNumbersAround(i, j);
                        if (numbers.size() == 2) sum += numbers.stream().reduce(1L, (a, b) -> a * b);
                    }
                }
            }
            return sum;

        }
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        int firstLineLength = lines.get(0).length();
        boolean allLinesHaveTheSameLength = lines.stream().map(String::length).allMatch(l -> l == firstLineLength);
        if (!allLinesHaveTheSameLength) throw new IllegalArgumentException("Expected all lines the same size");

        char[][] matrix = lines.stream().map(String::toCharArray).toArray(char[][]::new);
        Field field = new Field(matrix);
        return field.sumAllGears();
    }

}

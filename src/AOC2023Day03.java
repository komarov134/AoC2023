import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day03 {

    String filePath = "input/AOC2023Day03.input";


    // start/end indexes (inclusive) of the number in the row
    record NumberCoords(int row, int start, int end) {}

    record Field(char[][] m) {

        // everything out of the field is a dot
        boolean isDotOrDigit(int i, int j) {
            // out of the field
            if (i < 0 || i >= m.length || j < 0 || j >= m[i].length) return true;
            return m[i][j] == '.' || Character.isDigit(m[i][j]);
        }

        NumberCoords detectNumber(int i, int j) {
            if (!Character.isDigit(m[i][j])) throw new RuntimeException("not a digit");
            int start = j;
            while (start - 1 >= 0 && Character.isDigit(m[i][start - 1])) start--;
            int end = start;
            while (end + 1 < m[i].length && Character.isDigit(m[i][end + 1])) end++;
            return new NumberCoords(i, start, end);
        }

        boolean digitIsSurroundedByDots(int i, int j) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (!isDotOrDigit(i + dx, j + dy)) return false;
                }
            }
            return true;
        }

        boolean isSurroundedByDots(NumberCoords nc) {
            for (int c = nc.start; c <= nc.end; c++) {
                if (!digitIsSurroundedByDots(nc.row, c)) return false;
            }
            return true;
        }

        // replace with dots
        void eraseNumber(NumberCoords nc) {
            for (int c = nc.start; c <= nc.end; c++) {
                m[nc.row][c] = '.';
            }
        }

        long sumAllNumbers() {
            long sum = 0;
            for (int i = 0; i < m.length; i++) {
                int columnsNumber = m[i].length;
                for (int j = 0; j < columnsNumber; j++) {
                    if (Character.isDigit(m[i][j])) {
                        NumberCoords nc = detectNumber(i, j);
                        if (!isSurroundedByDots(nc)) {
                            String numberStr = new String(Arrays.copyOfRange(m[nc.row], nc.start, nc.end + 1));
                            sum += Long.parseLong(numberStr);
                            eraseNumber(nc);
                        }
//                        j = nc.end; // skip all other digits (Actually it's not necessary)
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
        return field.sumAllNumbers();
    }

}

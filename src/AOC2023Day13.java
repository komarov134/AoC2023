import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day13 {

    String filePath = "input/AOC2023Day13.input";

    static char ASH = '.';
    static char ROCK = '#';

    static long HORIZONTAL_MULTIPLIER = 100;


    static String asString(char[] arr) {
        return new String(arr);
    }

    static char[][] transpose(char[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        char[][] transposed = new char[cols][rows];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                transposed[i][j] = matrix[j][i];
            }
        }

        return transposed;
    }

    static boolean hasMirror(int leftLength, String[] arr) {
        int leftIndex = leftLength - 1;
        int rightIndex = leftIndex + 1;
        int rightLength = arr.length - leftLength;
        int length = Math.min(leftLength, rightLength);
        for (int d = 0; d < length; d++) {
            if (!arr[leftIndex - d].equals(arr[rightIndex + d])) return false;
        }
//        System.out.println("hasMirror true for leftLength=" + leftLength + " and " + Arrays.toString(arr));
        return true;
    }

    // at most 1 mirror exists
    static long leftNumbers(String[] arr) {
        for (int l = 1; l <= arr.length - 1; l++) {
            if (hasMirror(l, arr)) return l;
        }
        return 0;
    }

    static String[] rowsAsStrings(char[][] map) {
        return Arrays.stream(map).map(arr -> asString(arr)).toArray(String[]::new);
    }

    record Pattern(char[][] map) {
        int rows() {
            return map.length;
        }
        int columns() {
            return map[0].length;
        }


        long horizontalReflectionNumber() {
            var n = rowsAsStrings(map);
//            System.out.println("horizontalReflectionNumber: " + Arrays.toString(n));
            return HORIZONTAL_MULTIPLIER * leftNumbers(n);
        }
        long verticalReflectionNumber() {
            var n = rowsAsStrings(transpose(map));
//            System.out.println("verticalReflectionNumber: " + Arrays.toString(n));
            return leftNumbers(n);
        }

        long reflectionNumber() {
            long h = horizontalReflectionNumber();
            long v = verticalReflectionNumber();
            System.out.println("h = " + h + ", v = " + v);
            assert h == 0 ^ v == 0;
            return h + v;
        }

    }

    Pattern build(List<String> patternLines) {
        char[][] map = new char[patternLines.size()][patternLines.get(0).length()];
        int i = 0;
        for (String line: patternLines) map[i++] = line.toCharArray();
        return new Pattern(map);
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        List<Pattern> patterns = new ArrayList<>();
        List<String> patternLines = new ArrayList<>();
        for (String line: lines) {
            if (line.isEmpty()) {
                patterns.add(build(patternLines));
                patternLines = new ArrayList<>();
            } else {
                patternLines.add(line);
            }
        }
        patterns.add(build(patternLines));

        int maxRows = patterns.stream().map(Pattern::rows).reduce(0, Math::max);
        int maxColumns = patterns.stream().map(Pattern::columns).reduce(0, Math::max);
        System.out.println("max rows = " + maxRows + " and max columns = " + maxColumns);

        long sum = patterns.stream().map(Pattern::reflectionNumber).reduce(0L, Long::sum);

        return sum;
    }

}

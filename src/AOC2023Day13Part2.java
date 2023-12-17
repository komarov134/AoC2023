import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day13Part2 {

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
        return true;
    }

    // The mirror '|' placed in 5th position is [0, 1, 2, 3, 4 | 5, 6, 7]
    // this is also a number of elements left to the mirror (leftLength)
    static List<Integer> positions(String[] arr) {
        List<Integer> positions = new ArrayList<>();
        for (int l = 1; l <= arr.length - 1; l++) {
            if (hasMirror(l, arr)) positions.add(l);
        }
        return positions;
    }

    static String[] rowsAsStrings(char[][] map) {
        return Arrays.stream(map).map(arr -> asString(arr)).toArray(String[]::new);
    }

    record Mirror(boolean horizontal, int position) {}

    record Pattern(char[][] map) {
        int rows() {
            return map.length;
        }
        int columns() {
            return map[0].length;
        }


        // null if not found
        List<Mirror> mirrors() {
            List<Integer> horizontalPositions = positions(rowsAsStrings(map));
            List<Integer> verticalPositions = positions(rowsAsStrings(transpose(map)));
            List<Mirror> mirrors = new ArrayList<>();
            for (var h: horizontalPositions) {
                mirrors.add(new Mirror(true, h));
            }
            for (var v: verticalPositions) {
                mirrors.add(new Mirror(false, v));
            }
            return mirrors;
        }

        void flipCell(int i, int j) {
            char flipped = map[i][j] == ASH ? ROCK : ASH;
            map[i][j] = flipped;
        }

        long fixSmudgeAndReturnReflectionNumber() {
            List<Mirror> mirrorWithSmudge = mirrors();
            assert mirrorWithSmudge.size() == 1;    // the only one exists in part 1

            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < columns(); j++) {
                    flipCell(i, j);
                    List<Mirror> mirrors = mirrors().stream().filter(m -> !m.equals(mirrorWithSmudge.get(0))).toList();
                    assert mirrors.size() <= 1 : "got " + mirrors.size();   // at most one exists in part 2
                    for (var m: mirrors) {
                        long multiplier = m.horizontal ? HORIZONTAL_MULTIPLIER : 1;
                        return multiplier * m.position;
                    }
                    flipCell(i, j);
                }
            }
            return 0;
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

        long sum = patterns.stream().map(Pattern::fixSmudgeAndReturnReflectionNumber).reduce(0L, Long::sum);

        return sum;
    }

}

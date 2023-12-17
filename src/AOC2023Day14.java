import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AOC2023Day14 {

    String filePath = "input/AOC2023Day14.input";

    static char EMPTY = '.';
    static char CUBE = '#';
    static char ROUND = 'O';

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


    record Field(char[][] map) {

        void moveLeft(char[] arr, int i) {
            assert arr[i] == ROUND;
            while(i > 0 && arr[i - 1] == EMPTY) {
                arr[i - 1] = ROUND;
                arr[i] = EMPTY;
                i--;
            }
        }

        long totalLoad() {
            var d = transpose(map); // now we need to move all as left as possible

            // move all
            for (char[] arr: d) {
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == ROUND) moveLeft(arr, i);
                }
            }

            var x = transpose(d);
            for (char[] arr: x) System.out.println(new String(arr));

            long load = 0;
            for (int i = 0; i < d.length; i++) {
                for (int j = 0; j < d[0].length; j++) {
                    if (d[i][j] == ROUND) load += d[0].length - j;
                }
            }
            return load;
        }

    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        var map = lines.stream().map(String::toCharArray).toArray(char[][]::new);
        var field = new Field(map);

        return field.totalLoad();
    }

}

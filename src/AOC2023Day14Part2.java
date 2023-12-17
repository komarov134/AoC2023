import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day14Part2 {

    String filePath = "input/AOC2023Day14.input";

    static char EMPTY = '.';
    static char CUBE = '#';
    static char ROUND = 'O';

    static int CYCLES_COUNT = 1000000000;

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

    static void transposeInPlace(char[][] matrix) {
        int n = matrix.length;
        assert n == matrix[0].length;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                char temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }
    }

    // 1 2 3      3 2 1
    // 4 5 6  ->  6 5 4
    // 7 8 9      9 8 7
    static void reflectInPlace(char[][] m) {
        int n = m.length;
        for (char[] arr: m) {
            for (int i = 0; i < n / 2; i++) {
                char t = arr[i];
                arr[i] = arr[n - 1 - i];
                arr[n - 1 - i] = t;
            }
        }
    }

    static void moveLeft(char[] arr, int i) {
        assert arr[i] == ROUND;
        while (i > 0 && arr[i - 1] == EMPTY) {
            arr[i - 1] = ROUND;
            arr[i] = EMPTY;
            i--;
        }
    }

    static void moveAllLeft(char[][] m) {
        for (char[] arr: m) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == ROUND) moveLeft(arr, i);
            }
        }
    }

    static void moveAllRight(char[][] m) {
        reflectInPlace(m);
        moveAllLeft(m);
        reflectInPlace(m);
    }

    static void moveAllUp2(char[][] m) {
        for (int j = 0; j < m[0].length; j++) {
            for (int i = 0; i < m.length; i++) {
                if (m[i][j] == ROUND) {
                    int k = i;
                    while (k > 0 && m[k - 1][j] == EMPTY) {
                        m[k - 1][j] = ROUND;
                        m[k][j] = EMPTY;
                        k--;
                    }
                }
            }
        }
    }

    static void moveAllDown2(char[][] m) {
        for (int j = 0; j < m[0].length; j++) {
            for (int i = m.length - 1; i >= 0; i--) {
                if (m[i][j] == ROUND) {
                    int k = i;
                    while (k + 1 < m.length && m[k + 1][j] == EMPTY) {
                        m[k + 1][j] = ROUND;
                        m[k][j] = EMPTY;
                        k++;
                    }
                }
            }
        }
    }

    static void moveAllUp(char[][] m) {
        transposeInPlace(m);
        moveAllLeft(m);
        transposeInPlace(m);
    }

    static void moveAllDown(char[][] m) {
        transposeInPlace(m);
        reflectInPlace(m);
        moveAllLeft(m);
        reflectInPlace(m);
        transposeInPlace(m);
    }



    static void cycle(char[][] m) {
        moveAllUp2(m);
        moveAllLeft(m);
        moveAllDown2(m);
        moveAllRight(m);
    }

    static void print(char[][] m) {
        for (char[] arr: m) System.out.println(new String(arr));
    }

    static int countLeftLoad(char[][] m) {
        int load = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                if (m[i][j] == ROUND) load += m[0].length - j;
            }
        }
        return load;
    }

    static int countUpLoad(char[][] m) {
        transposeInPlace(m);
        var load = countLeftLoad(m);
        transposeInPlace(m);
        return load;
    }

    // Function to find the period in the integer array
    public static int findPeriod(int[] array) {
        for (int period = 1; period <= array.length / 3; period++) {
            if (hasPeriod(array, period)) return period;
        }
        return -1; // No repeating pattern found
    }

    public static boolean hasPeriod(int[] a, int period) {
        for (int i = 0; i + period < a.length; i++) {
            if (a[i] != a[i + period]) return false;
        }
        return true;
    }

    record Field(char[][] map) {

        long totalLoad() {
            // warming up
            int warmingUpCycles = 10000;
            for (int i = 0; i < warmingUpCycles; i++) {
                cycle(map);
            }
            int[] numbers = new int[10000];
            for (int i = 0; i < numbers.length; i++) {
                cycle(map);
                numbers[i] = countUpLoad(map);
            }
            System.out.println(Arrays.toString(numbers));
            int p = findPeriod(numbers);
            System.out.println("period = " + p);

            var rest = (CYCLES_COUNT - warmingUpCycles) % p;
            var index = rest == 0 ? p - 1 : rest - 1;
            return numbers[index];
        }

    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        var map = lines.stream().map(String::toCharArray).toArray(char[][]::new);
        var field = new Field(map);

        return field.totalLoad();
    }

}

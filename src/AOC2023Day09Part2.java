import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day09Part2 {

    String filePath = "input/AOC2023Day09.input";

    long predictFirstValue(long[] values) {
        long[][] m = new long[values.length][values.length];
        m[0] = values;
        for (int i = 1; i < values.length; i++) {
            for (int j = values.length - 2; j >= 0; j--) {
                m[i][j] = m[i - 1][j + 1] - m[i - 1][j];
            }
        }
        long result = 0;
        for (int i = m.length - 1; i >= 0; i--) result = m[i][0] - result;
        return result;
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        long sum = 0;
        for (String line: lines) {
            long[] arr = Arrays.stream(line.split("\\s+")).mapToLong(Long::parseLong).toArray();
            long p = predictFirstValue(arr);
            System.out.println("adding " + p);
            sum += p;
        }
        return sum;
    }

}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day09 {

    String filePath = "input/AOC2023Day09.input";

    long predictNextValue(long[] values) {
        long[] d = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            d[i] = values[values.length - i - 1];
        }
        long sum = 0;
        for (int k = d.length - 1; k > 0; k--) {
            for (int i = 0; i < k; i++) {
                d[i] = d[i] - d[i + 1];
            }
            sum += d[0];
        }
        return values[values.length - 1] + sum;
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        long sum = 0;
        for (String line: lines) {
            long[] arr = Arrays.stream(line.split("\\s+")).mapToLong(Long::parseLong).toArray();
            sum += predictNextValue(arr);
        }
        return sum;
    }

}

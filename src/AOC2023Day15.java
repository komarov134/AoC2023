import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AOC2023Day15 {

    String filePath = "input/AOC2023Day15.input";


    long hash(String s) {
        long h = 0;
        for (char c: s.toCharArray()) {
            h += c;
            h *= 17;
            h %= 256;
        }
        return h;
    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        var line = lines.get(0);
        var steps = line.split(",");
        long sum = 0;
        for (String step: steps) {
            var h = hash(step);
            System.out.println(h);
            sum += hash(step);
        }

        return sum;
    }

}

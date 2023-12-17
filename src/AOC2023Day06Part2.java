import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AOC2023Day06Part2 {

    String filePath = "input/AOC2023Day06.input";

    long dist(long t, long secondsPress) {
        long speed = secondsPress;
        long restSeconds = t - secondsPress;
        return speed * restSeconds;
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        long time = Long.parseLong(lines.get(0).replaceAll("\\D+", ""));
        long distance = Long.parseLong(lines.get(1).replaceAll("\\D+", ""));
        long ways = 0;
        for (int secondsPress = 0; secondsPress <= time; secondsPress++) {
            if (dist(time, secondsPress) > distance) ways++;
        }
        return ways;
    }

}

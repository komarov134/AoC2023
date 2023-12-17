import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day06 {

    String filePath = "input/AOC2023Day06.input";

    long dist(long t, long secondsPress) {
        long speed = secondsPress;
        long restSeconds = t - secondsPress;
        return speed * restSeconds;
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        List<Long> times = Arrays.stream(lines.get(0).replace("Time:", "").trim().split("\\s+")).map(Long::parseLong).toList();
        List<Long> distances = Arrays.stream(lines.get(1).replace("Distance:", "").trim().split("\\s+")).map(Long::parseLong).toList();
        assert times.size() == distances.size();

        long marginOfError = 1;
        for (int i = 0; i < times.size(); i++) {
            long time = times.get(i);
            long distance = distances.get(i);
            long ways = 0;
            for (int secondsPress = 0; secondsPress <= time; secondsPress++) {
                if (dist(time, secondsPress) > distance) ways++;
            }
            marginOfError *= ways;
        }

        return marginOfError;
    }

}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day12 {

    String filePath = "input/AOC2023Day12.input";

    static char OPERATIONAL = '.';
    static char DAMAGED = '#';
    static char UNKNOWN = '?';

    boolean damagedOrUnknown(char c) {
        return c == DAMAGED || c == UNKNOWN;
    }

    boolean operationalOrUnknown(char c) {
        return c == OPERATIONAL || c == UNKNOWN;
    }


    // damage group should be enclosed with OPERATIONAL
    boolean canStartWithDamagedGroup(String s, int groupSize) {
        if (s.length() < groupSize + 1) return false;
        for (int i = 0; i < groupSize; i++) {
            if (!damagedOrUnknown(s.charAt(i))) return false;
        }
        return operationalOrUnknown(s.charAt(groupSize));
    }

    long startedWithDamaged(String s, List<Integer> groups) {
        if (groups.isEmpty()) return 0;
        var groupSize = groups.get(0);
        if (!canStartWithDamagedGroup(s, groupSize)) return 0;
        var newGroups = groups.subList(1, groups.size());
        return possibleArrangements(s.substring(groupSize + 1), newGroups);
    }

    long startedWithOperational(String s, List<Integer> groups) {
        return possibleArrangements(s.substring(1), groups);
    }

    long possibleArrangementsNonEmpty(String s, List<Integer> groups) {
        assert !s.isEmpty();
        return switch (s.charAt(0)) {
            case '.' -> startedWithOperational(s, groups);
            case '#' -> startedWithDamaged(s, groups);
            case '?' -> startedWithOperational(s, groups) + startedWithDamaged(s, groups);
            default -> throw new RuntimeException("unexpected case symbol: " + s.charAt(0));
        };
    }

    long possibleArrangements(String s, List<Integer> groups) {
        if (s.isEmpty()) return groups.isEmpty() ? 1 : 0;
        return possibleArrangementsNonEmpty(s, groups);
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        long sum = 0;
        for (String line: lines) {
            String[] arr = line.split("\\s+");
            var s = arr[0];
            var groups = Arrays.stream(arr[1].split(",")).map(Integer::parseInt).toList();
            var endingWithOperational = s + ".";    // it's needed for every group is closed
            sum += possibleArrangements(endingWithOperational, groups);
        }

        return sum;
    }

}

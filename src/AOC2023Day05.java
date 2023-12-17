import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day05 {

    String filePath = "input/AOC2023Day05.input";

    record RangeMapping(long destRangeStart, long sourceRangeStart, long length) {

        boolean covers(long source) {
            return sourceRangeStart <= source && source < sourceRangeStart + length;
        }

        long delta() {
            return destRangeStart - sourceRangeStart;
        }

        long destination(long source) {
            if (!covers(source)) throw new RuntimeException(this + " doesn't cover source " + source);
            return source + delta();
        }
    }

    record Mapping(List<RangeMapping> rangeMappings) {

        long destination(long source) {
            return rangeMappings.stream()
                    .filter(rm -> rm.covers(source))
                    .findFirst()
                    .map(rm -> rm.destination(source))
                    .orElse(source);
        }

    }

    record AllMappings(Map<String, Mapping> map) {
        void add(String name, RangeMapping rm) {
            map.computeIfAbsent(name, k -> new Mapping(new ArrayList<>())).rangeMappings.add(rm);
        }

        long destination(List<String> names, long source) {
            return names.stream().map(map::get).reduce(source, (s, m) -> m.destination(s), (a, b) -> b);
        }
    }

    String parseMapName(String line) {
        return line.replace(" map:", "");
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        List<Long> seeds = null;
        AllMappings allMappings = new AllMappings(new HashMap<>());
        String currentMapName = null;
        for (String line : lines) {
            if (line.contains("seeds: ")) {
                seeds = Arrays.stream(line.replace("seeds: ", "").split(" ")).map(Long::parseLong).toList();
            } else if (line.contains(" map:")) {
                currentMapName = parseMapName(line);
            } else if (line.replace(" ", "").matches("\\d+")) {
                List<Long> numbers = Arrays.stream(line.split(" ")).map(Long::parseLong).toList();
                RangeMapping rm = new RangeMapping(numbers.get(0), numbers.get(1), numbers.get(2));
                allMappings.add(currentMapName, rm);
            } else {
                // skip blank lines
            }
        }

        List<String> names = List.of(
                "seed-to-soil",
                "soil-to-fertilizer",
                "fertilizer-to-water",
                "water-to-light",
                "light-to-temperature",
                "temperature-to-humidity",
                "humidity-to-location"
        );
        List<Long> locations = seeds.stream().map(s -> allMappings.destination(names, s)).toList();
        System.out.println(locations);

        return locations.stream().reduce(Long.MAX_VALUE, Math::min);
    }

}

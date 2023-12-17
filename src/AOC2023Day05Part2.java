import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day05Part2 {

    String filePath = "input/AOC2023Day05.input";

    record Range(long start, long length) {
        long end() {
            return start + length - 1;
        }
    }

    record RangeMappingPair(Range source, Range dest) {

        long delta() {
            return dest.start - source.start;
        }

        // returns list of dest ranges: 0 or 1
        List<RangeMappingPair> coveredRange(Range otherSource) {
            if (source.end() < otherSource.start || otherSource.end() < source.start) {
                return List.of();
            } else {
                long l = Math.max(source.start, otherSource.start);
                long r = Math.min(source.end(), otherSource.end());
                long length = r - l + 1;
                Range rangeSource = new Range(l, length);
                Range rangeDest = new Range(l + delta(), length);
                RangeMappingPair rmp = new RangeMappingPair(rangeSource, rangeDest);
                return List.of(rmp);
            }
        }
    }

    record RangeMapping(long destRangeStart, long sourceRangeStart, long length) {

        RangeMappingPair toPair() {
            return new RangeMappingPair(new Range(sourceRangeStart, length), new Range(destRangeStart, length));
        }

    }

    record Mapping(List<RangeMapping> rangeMappings) {

        // returns covered and non-covered ranges
        List<RangeMappingPair> allRanges(Range sourceRange, List<RangeMappingPair> coveredRanges) {
            List<RangeMappingPair> arr = new ArrayList<>();
            Range firstFictiveRange = new Range(sourceRange.start - 1, 1);
            arr.add(new RangeMappingPair(firstFictiveRange, firstFictiveRange));
            arr.addAll(coveredRanges);
            Range lastFictiveRange = new Range(sourceRange.end() + 1, 1);
            arr.add(new RangeMappingPair(lastFictiveRange, lastFictiveRange));

            List<RangeMappingPair> allRanges = new ArrayList<>();
            for (int i = 0; i < arr.size() - 1; i++) {
                RangeMappingPair current = arr.get(i);
                RangeMappingPair next = arr.get(i + 1);
                long startIndex = current.source.end() + 1;
                long length = next.source.start - startIndex;
                Range range = new Range(startIndex, length);
                allRanges.add(current);
                allRanges.add(new RangeMappingPair(range, range));
            }
            return allRanges.stream().filter(r -> r.source.length > 0)
                    .filter(r -> r.source.start >= sourceRange.start)   // delete firstFictiveRange
                    .toList();
        }

        // returns list of dest ranges and not covered ranges as well
        List<Range> destRanges(Range sourceRange) {
            List<RangeMappingPair> pairs = rangeMappings.stream()
                    .map(RangeMapping::toPair).sorted(Comparator.comparingLong(p -> p.source.start)).toList();

            List<RangeMappingPair> coveredRanges = pairs.stream().flatMap(p -> p.coveredRange(sourceRange).stream()).toList();

            List<RangeMappingPair> all = allRanges(sourceRange, coveredRanges);

            return all.stream().map(r -> r.dest).toList();
        }

    }

    record AllMappings(Map<String, Mapping> map) {
        void add(String name, RangeMapping rm) {
            map.computeIfAbsent(name, k -> new Mapping(new ArrayList<>())).rangeMappings.add(rm);
        }

        long minLocation(List<String> names, Range sourceRange) {
            if (names.isEmpty()) {
                return sourceRange.start;
            } else {
                String name = names.get(0);
                List<String> restNames = names.subList(1, names.size());
                return map.get(name).destRanges(sourceRange).stream()
                        .map(r -> minLocation(restNames, r))
                        .reduce(Long.MAX_VALUE, Long::min);
            }
        }
    }

    String parseMapName(String line) {
        return line.replace(" map:", "");
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        List<Range> seeds = new ArrayList<>();
        AllMappings allMappings = new AllMappings(new HashMap<>());
        String currentMapName = null;
        for (String line : lines) {
            if (line.contains("seeds: ")) {
                List<Long> seedNumbers = Arrays.stream(line.replace("seeds: ", "").split(" "))
                        .map(Long::parseLong).toList();
                for (int i = 0; i < seedNumbers.size(); i += 2) {
                    seeds.add(new Range(seedNumbers.get(i), seedNumbers.get(i + 1)));
                }
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

        List<Long> locations = seeds.stream().map(s -> allMappings.minLocation(names, s)).toList();

        return locations.stream().reduce(Long.MAX_VALUE, Math::min);
    }

}

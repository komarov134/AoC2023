import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day15Part2 {

    String filePath = "input/AOC2023Day15.input";


    static int hash(String s) {
        int h = 0;
        for (char c: s.toCharArray()) {
            h += c;
            h *= 17;
            h %= 256;
        }
        return h;
    }

    record Box(java.util.SequencedMap<String, Integer> lenses) {
        void delete(String label) {
            lenses.remove(label);
        }
        void insert(String label, int focalLength) {
            lenses.put(label, focalLength);
        }
    }

    record Boxes(Box[] boxes) {
        void delete(String label) {
            int boxNumber = hash(label);
            boxes[boxNumber].delete(label);
        }
        void insert(String label, int focalLength) {
            boxes[hash(label)].insert(label, focalLength);
        }
        long focusingPowerSum() {
            long sum = 0;
            for (int boxNumber = 1; boxNumber <= boxes.length; boxNumber++) {
                var box = boxes[boxNumber - 1];
                int slot = 1;
                for (int focalLength: box.lenses.sequencedValues()) {
                    long focusingPower = boxNumber * slot * focalLength;
                    sum += focusingPower;
                    slot++;
                }
            }
            return sum;
        }
    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        var line = lines.get(0);
        var steps = line.split(",");
        Box[] boxesArray = new Box[256];
        for (int i = 0; i < boxesArray.length; i++) {
            boxesArray[i] = new Box(new LinkedHashMap<>());
        }
        var boxes = new Boxes(boxesArray);
        for (String step: steps) {
            if (step.endsWith("-")) {
                String label = step.substring(0, step.length() - 1);
                boxes.delete(label);
            } else {
                var arr = step.split("=");
                String label = arr[0];
                int focalLength = Integer.parseInt(arr[1]);
                boxes.insert(label, focalLength);
            }
        }

        for (var b: boxes.boxes) System.out.println(b.lenses);

        return boxes.focusingPowerSum();
    }

}

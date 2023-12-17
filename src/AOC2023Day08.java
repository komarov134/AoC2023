import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class AOC2023Day08 {

    String filePath = "input/AOC2023Day08.input";

    record Ways(String left, String right) {}

    static String START = "AAA";
    static String END = "ZZZ";

    // commands
    static char L = 'L';
    static char R = 'R';


    record Network(Map<String, Ways> nodes) {

    }

    record Instruction(String commands) {

        Stream<Character> cyclicCommands() {
            return Stream.generate(() -> commands.chars().mapToObj(c -> (char)c)).flatMap(s -> s);
        }

        long countSteps(Network network) {
            long steps = 0;
            Iterator<Character> it = cyclicCommands().iterator();
            String current = START;
            while (!current.equals(END)) {
                Character command = it.next();
                Ways ways = network.nodes.get(current);
                current = (command == L) ? ways.left : ways.right;
                steps++;
            }
            return steps;
        }

    }

    Ways parse(String s) {
        String[] arr = s.split(",");
        String left = arr[0].replaceAll("[^A-Z]+", "");
        String right = arr[1].replaceAll("[^A-Z]+", "");
        return new Ways(left, right);
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        Instruction instruction = new Instruction(lines.get(0));
        Map<String, Ways> nodes = new HashMap<>();
        for (String line: lines.subList(2, lines.size())) {
            String[] arr = line.split("=");
            String node = arr[0].trim();
            Ways ways = parse(arr[1]);
            nodes.put(node, ways);
        }
        Network network = new Network(nodes);

        return instruction.countSteps(network);
    }

}

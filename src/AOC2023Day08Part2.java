import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class AOC2023Day08Part2 {

    String filePath = "input/AOC2023Day08.input";

    record Ways(String left, String right) {}

    // commands
    static char L = 'L';
    static char R = 'R';

    record Network(Map<String, Ways> nodes) {

        Position startPosition() {
            return new Position(nodes.keySet().stream().filter(s -> s.endsWith("A")).toArray(String[]::new));
        }

    }

    // mutable
    record Position(String[] nodes) {

        boolean allAreEnd() {
            for (String node: nodes) {
                if (node.charAt(2) != 'Z') return false;
            }
            return true;
        }

        void applyCommand(char command, Network network) {
            for (int i = 0; i < nodes.length; i++) {
                Ways ways = network.nodes.get(nodes[i]);
                nodes[i] = (command == L) ? ways.left : ways.right;
            }
        }
    }

    record Instruction(String commands) {

        Stream<Character> cyclicCommands() {
            return Stream.generate(() -> commands.chars().mapToObj(c -> (char)c)).flatMap(s -> s);
        }

        // this takes too much time
        long countSteps(Network network) {
            long steps = 0;
            Iterator<Character> it = cyclicCommands().iterator();
            Position current = network.startPosition();
            while (!current.allAreEnd()) {
                Character command = it.next();
                current.applyCommand(command, network);
                steps++;
                if (steps % 10000000 == 0) {
                    System.out.println("steps = " + steps);
                }
            }
            return steps;
        }

        // this takes too much time
        long countStepsOptimized(Network network) {
            long steps = 0;
            Position current = network.startPosition();
            int stepIndex = 0;
            while (!current.allAreEnd()) {
                if (stepIndex == commands.length()) stepIndex = 0;
                char command = commands.charAt(stepIndex);
                current.applyCommand(command, network);
                stepIndex++;
                steps++;
                if (steps % 10000000 == 0) {
                    System.out.println("steps = " + steps);
                }
            }
            return steps;
        }

        long countStepsForOne(String node, Network network) {
            long steps = 0;
            String current = node;
            int stepIndex = 0;
            while (!current.endsWith("Z")) {
                if (stepIndex == commands.length()) stepIndex = 0;
                char command = commands.charAt(stepIndex);
                Ways ways = network.nodes.get(current);
                current = (command == L) ? ways.left : ways.right;
                stepIndex++;
                steps++;
            }
            return steps;
        }

        long gcd(long a, long b) {
            return b == 0 ? a : gcd(b, a % b);
        }

        long lcm(long a, long b) {
            return a * b / gcd(a, b);
        }

        long lcm(List<Long> numbers) {
            return numbers.stream().reduce((a, b) -> lcm(a, b)).get();
        }

        // smart version using LCM
        long countStepsUsingLCM(Network network) {
            Position start = network.startPosition();
            List<Long> steps = new ArrayList<>();
            for (String startNode: start.nodes) {
                steps.add(countStepsForOne(startNode, network));
            }
            return lcm(steps);
        }

    }

    Ways parse(String s) {
        String[] arr = s.split(",");
        String left = arr[0].replaceAll("\\W+", "");
        String right = arr[1].replaceAll("\\W+", "");
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

        return instruction.countStepsUsingLCM(network);
    }

}

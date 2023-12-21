import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class AOC2023Day20Part2 {

    String filePath = "input/AOC2023Day20.input";


    enum Pulse {
        LOW, HIGH
    }

    static class Toggle {
        boolean isOn = false;
        boolean flip() {
            isOn = !isOn;
            return isOn;
        }
    }

    interface IModule {
        Pulse handle(Pulse in, String from);
        void initInput(String from);
        List<String> nextModules();
    }
    record FlipFlop(Toggle toggle, List<String> next) implements IModule {
        public Pulse handle(Pulse in, String from) {
            if (in == Pulse.HIGH) return null;
            return toggle.flip() ? Pulse.HIGH : Pulse.LOW;
        }
        public void initInput(String from) {}
        public List<String> nextModules() { return next; }
    }
    record Conjunction(TreeMap<String, Pulse> input, List<String> next) implements IModule {
        boolean allHigh() {
            return input.values().stream().allMatch(p -> p == Pulse.HIGH);
        }
        public Pulse handle(Pulse in, String from) {
            input.put(from, in);
            return allHigh() ? Pulse.LOW : Pulse.HIGH;
        }
        public void initInput(String from) {
            input.put(from, Pulse.LOW);
        }
        public List<String> nextModules() { return next; }
    }
    record Broadcast(List<String> next) implements IModule {
        public Pulse handle(Pulse in, String from) {
            return in;
        }
        public void initInput(String from) {}
        public List<String> nextModules() { return next; }
    }

    record NamedModule(String name, IModule m) {}

    static NamedModule parseModule(String line) {
        String[] moduleAndNext = line.split(" -> ");
        String rawName = moduleAndNext[0];
        List<String> nextList = Arrays.asList(moduleAndNext[1].split(", "));
        return switch (rawName.charAt(0)) {
            case '%' -> new NamedModule(rawName.substring(1), new FlipFlop(new Toggle(), nextList));
            case '&' -> new NamedModule(rawName.substring(1), new Conjunction(new TreeMap<>(), nextList));
            default -> new NamedModule(rawName, new Broadcast(nextList));
        };
    }

    record Signal(String from, String to, Pulse pulse) {}

    record Modules(Map<String, IModule> map) {

        void initInput() {
            for (Map.Entry<String, IModule> e: map.entrySet()) {
                for (var next: e.getValue().nextModules()) {
                    var nextModule = map.get(next);
                    if (nextModule != null) nextModule.initInput(e.getKey());
                }
            }
        }

        boolean pathToTerminalContains(String from, String desired) {
            Set<String> visited = new HashSet<>();
            Queue<String> q = new LinkedList<>();
            q.add(from);
            while (!q.isEmpty()) {
                String cur = q.poll();
                if (visited.contains(cur)) continue;
                visited.add(cur);
                if (map.containsKey(cur)) {
                    for (var next: map.get(cur).nextModules()) {
                        if (!visited.contains(next)) q.add(next);
                    }
                }
            }
            return visited.contains(desired);
        }

        // returns subtree with moduleName root
        void pruneAllNonRelatedTo(String moduleName) {
            for (var k: map.keySet().stream().toList()) {
                if (!pathToTerminalContains(k, moduleName)) map.remove(k);
            }
        }

        // we are looking for signal LOW for a certain module
        // Why? I drew a mermaid flowchart, it turned out that it contains of 4 independent blocks
        long pressButton(int times, String signalModule) {
            initInput();
            Queue<Signal> q = new LinkedList<>();

            for (int i = 1; i <= times; i++) {
                q.add(new Signal("press-buttom", "broadcaster", Pulse.LOW));
                while (!q.isEmpty()) {
                    var signal = q.poll();
                    if (signal.to.equals(signalModule) && signal.pulse == Pulse.LOW) {
                        return i;
                    }
                    var receiverName = signal.to;
                    var receiver = map.get(receiverName);
                    if (receiver != null) {
                        var outPulse = receiver.handle(signal.pulse, signal.from);
                        if (outPulse != null) for (var next: receiver.nextModules()) q.add(new Signal(receiverName, next, outPulse));
                    }
                }

            }

            throw new RuntimeException("LOW pulse wasn't produced after " + times + " cycles");
        }

        long countButtonPressForLowTo(String terminalModule) {
            pruneAllNonRelatedTo(terminalModule);
            return pressButton(100000000, terminalModule);
        }


        String buildName(String name) {
            if (!map.containsKey(name)) return name.concat("-TERMINAL");
            return switch (map.get(name)) {
                case FlipFlop f -> name.concat("-FF");
                case Conjunction f -> name.concat("-CON");
                case Broadcast f -> name;
                default -> throw new RuntimeException("unexpected");
            };
        }

        // print all nodes in Mermaid format
        void printMermaidInput() {
            List<String> lines = new ArrayList<>();
            for (var e: map.entrySet()) {
                String name = buildName(e.getKey());
                for (var next: e.getValue().nextModules()) {
                    String nextName = buildName(next);
                    lines.add(name + " ---> " + nextName);
                }
            }
            lines.sort(Comparator.naturalOrder());
            for (var line: lines) System.out.println(line);
        }

    }

    static Modules parseModule(List<String> lines) {
        Map<String, IModule> map = new HashMap<>();
        for (var line: lines) {
            var m = parseModule(line);
            map.put(m.name, m.m);
        }
        return new Modules(map);
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        parseModule(lines).printMermaidInput();
        System.out.println("---------------------");

        long cdCount = parseModule(lines).countButtonPressForLowTo("cd");
        System.out.println("cdCount=" + cdCount);
        long qxCount = parseModule(lines).countButtonPressForLowTo("qx");
        System.out.println("qxCount=" + qxCount);
        long rkCount = parseModule(lines).countButtonPressForLowTo("rk");
        System.out.println("rkCount=" + rkCount);
        long zfCount = parseModule(lines).countButtonPressForLowTo("zf");
        System.out.println("zfCount=" + zfCount);

        return Utils.lcm(List.of(cdCount, qxCount, rkCount, zfCount));
    }

}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class AOC2023Day20 {

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
    record Conjunction(Map<String, Pulse> input, List<String> next) implements IModule {
        public Pulse handle(Pulse in, String from) {
            input.put(from, in);
            boolean allHigh = input.values().stream().allMatch(p -> p == Pulse.HIGH);
            return allHigh ? Pulse.LOW : Pulse.HIGH;
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
            case '&' -> new NamedModule(rawName.substring(1), new Conjunction(new HashMap<>(), nextList));
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

        // return multiplication of LOW and HIGH pulses
        long pressButton(int times) {
            Queue<Signal> q = new LinkedList<>();

            long lowCounter = 0;
            long highCounter = 0;

            for (int i = 0; i < times; i++) {
                q.add(new Signal("press-buttom", "broadcaster", Pulse.LOW));
                while (!q.isEmpty()) {
                    var signal = q.poll();
                    if (signal.pulse == Pulse.LOW) {
                        lowCounter++;
                    } else {
                        highCounter++;
                    }
                    var receiverName = signal.to;
                    var receiver = map.get(receiverName);
                    if (receiver != null) {
                        var outPulse = receiver.handle(signal.pulse, signal.from);
                        if (outPulse != null) for (var next: receiver.nextModules()) q.add(new Signal(receiverName, next, outPulse));
                    }
                }
            }

            return lowCounter * highCounter;
        }

    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        Map<String, IModule> map = new HashMap<>();
        for (var line: lines) {
            var m = parseModule(line);
            map.put(m.name, m.m);
        }
        Modules modules = new Modules(map);
        for (var m: modules.map.values()) {
            System.out.println(m);
        }
        System.out.println("---------------");
        modules.initInput();

        for (var m: modules.map.values()) {
            System.out.println(m);
        }

        return modules.pressButton(1000);
    }

}

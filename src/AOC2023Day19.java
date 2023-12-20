import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class AOC2023Day19 {

    String filePath = "input/AOC2023Day19.input";

    interface Action {}
    record Reject() implements Action {}
    record Accept() implements Action {}
    record Transfer(String workflowName) implements Action {}

    record Rule(Function<Part, Boolean> f, Action action) {}

    record Workflow(String name, List<Rule> rules) {}

    record Part(int x, int m, int a, int s) {
        long sum() {
            return x + m + a + s;
        }
    }

    static String START_WORKFLOW_NAME = "in";

    record Workflows(Map<String, Workflow> map, List<Part> parts) {

        Action endAction(Workflow w, Part p) {
            for (var r: w.rules) {
                if (r.f.apply(p)) {
                    if (r.action instanceof Transfer) {
                        return endAction(map.get(((Transfer) r.action).workflowName), p);
                    } else {
                        return r.action;
                    }
                }
            }
            throw new RuntimeException("endAction not found");
        }

        long countAccepted() {
            long sum = 0;
            for (var p: parts) {
                Action a = endAction(map.get(START_WORKFLOW_NAME), p);
                if (a instanceof Accept) {
                    sum += p.sum();
                }
            }
            return sum;
        }
    }


    // a<2006
    Function<Part, Boolean> parsePredicate(String s) {
        char tag = s.charAt(0);
        char cond = s.charAt(1);
        boolean gt = cond == '>';
        int condNumber = Integer.parseInt(s.substring(2));
        return switch (tag) {
            case 'x' -> p -> gt ? p.x > condNumber : p.x < condNumber;
            case 'm' -> p -> gt ? p.m > condNumber : p.m < condNumber;
            case 'a' -> p -> gt ? p.a > condNumber : p.a < condNumber;
            case 's' -> p -> gt ? p.s > condNumber : p.s < condNumber;
            default -> throw new RuntimeException("unexpected tag = " + tag);
        };
    }

    Action parseAction(String s) {
        return switch (s) {
            case "A" -> new Accept();
            case "R" -> new Reject();
            default -> new Transfer(s);
        };
    }

    // px{a<2006:qkq,m>2090:A,rfg}
    Workflow parseWorkflow(String s) {
        String[] name = s.split("\\{");
        // a<2006:qkq,m>2090:A,rfg} -> a<2006:qkq m>2090:A rfg
        String[] rules = name[1].substring(0, name[1].length() - 1).split(",");
        List<Rule> ruleList = new LinkedList<>();
        for (var rule: rules) {
            if (rule.contains(":")) {
                String[] condAndAction = rule.split(":");
                var p = parsePredicate(condAndAction[0]);
                Action action = parseAction(condAndAction[1]);
                ruleList.add(new Rule(p, action));
            } else {
                Action lastAction = parseAction(rule);
                ruleList.add(new Rule(p -> true, lastAction));
            }
        }
        return new Workflow(name[0], ruleList);
    }

    // {x=1088,m=324,a=1314,s=1170}
    Part parsePart(String str) {
        int x = 0, m = 0, a = 0, s = 0;
        for (var nameNumber: str.substring(1, str.length() - 1).split(",")) {
            int number = Integer.parseInt(nameNumber.substring(2));
            switch (nameNumber.charAt(0)) {
                case 'x' -> x = number;
                case 'm' -> m = number;
                case 'a' -> a = number;
                case 's' -> s = number;
                default -> x = x;
            }
        }
        return new Part(x, m, a, s);
    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        List<Workflow> workflowList = new ArrayList<>();
        List<Part> partList = new ArrayList<>();

        boolean readWorkflow = true;
        for (var line: lines) {
            if (line.isEmpty()) {
                readWorkflow = false;
                continue;
            }
            if (readWorkflow) {
                workflowList.add(parseWorkflow(line));
            } else {
                partList.add(parsePart(line));
            }
        }
        for (var w: workflowList) System.out.println(w);
        System.out.println("-------------------------------------");
        for (var p: partList) System.out.println(p);

        Map<String, Workflow> workflowMap = new HashMap<>();
        for (var w: workflowList) workflowMap.put(w.name, w);

        Workflows workflows = new Workflows(workflowMap, partList);

        return workflows.countAccepted();
    }

}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class AOC2023Day19Part2 {

    String filePath = "input/AOC2023Day19.input";


    interface Tree {}
    record Node(Tree r1, Tree r2) implements Tree {}
    record Apply(Tree r, Condition c) implements Tree {}
    record AcceptAny() implements Tree {}
    record RejectAll() implements Tree {}

    // could be optimized if we store List<Pair<Condition, MaterializedSet>>
    // so that we apply one condition on each level
    static long countCombinations(List<Condition> currentConditions) {
        Map<Character, IntStream> sets = new HashMap<>();
        for (char c: "xmas".toCharArray()) sets.put(c, IntStream.range(1, 4001));
        for (var cond: currentConditions) {
            sets.computeIfPresent(cond.tag, (k, v) -> v.filter(cond::apply));
        }
        return sets.values().stream().map(s -> s.count()).reduce(1L, (a, b) -> a * b);
    }

    static void walk(Tree t, List<Condition> currentConditions, AtomicLong sum) {
        switch (t) {
            case RejectAll a -> {
            }
            case AcceptAny a -> {
                sum.addAndGet(countCombinations(currentConditions));
            }
            case Apply a -> {
                currentConditions.add(a.c);
                walk(a.r, currentConditions, sum);
                currentConditions.removeLast();
            }
            case Node u -> {
                walk(u.r1, currentConditions, sum);
                walk(u.r2, currentConditions, sum);
            }
            default -> {}
        }
    }

    static long combinations(Tree t) {
        AtomicLong sum = new AtomicLong();  // just a mutable holder
        List<Condition> currentConditions = new ArrayList<>();
        walk(t, currentConditions, sum);
        return sum.get();
    }


    static void printResult(Tree r, int level) {
        String offset = String.join("", Collections.nCopies(level, " "));
        switch (r) {
            case Node u -> {
                System.out.println(offset + "Union:");
                printResult(u.r1, level + 1);
                printResult(u.r2, level + 1);
            }
            case Apply a -> {
                System.out.println(offset + "Apply: " + a.c);
                printResult(a.r, level + 1);
            }
            case AcceptAny a -> System.out.println(offset + "AcceptAny");
            case RejectAll a -> System.out.println(offset + "RejectAll");
            default -> {}
        }
    }

    record Condition(char tag, boolean lessThan, int number, boolean inverted) {

        Condition invert() {
            return new Condition(tag, lessThan, number, !inverted);
        }

        public boolean apply(int n) {
            boolean b = lessThan ? n < number : n > number;
            return inverted ? !b : b;
        }
    }

    interface Action {}
    record Reject() implements Action {}
    record Accept() implements Action {}
    record Transfer(String workflowName) implements Action {}

    record Rule(Condition condition, Action action) {}

    record Workflow(String name, List<Rule> rules) {}


    static String START_WORKFLOW_NAME = "in";

    record Workflows(Map<String, Workflow> map) {

        Tree builtTree(List<Rule> rules) {
            var rule = rules.getFirst();
            if (rules.size() == 1) {
                // last unconditional action
                return switch (rule.action) {
                    case Transfer t -> builtTree(map.get(t.workflowName).rules);
                    case Reject r -> new RejectAll();
                    case Accept a -> new AcceptAny();
                    default -> throw new IllegalStateException("Unexpected value: " + rule.action);
                };
            } else {
                var restRules = rules.subList(1, rules.size());
                var c = rule.condition;
                return switch (rule.action) {
                    // walk left, walk right, union the results
                    case Transfer t -> new Node(new Apply(builtTree(map.get(t.workflowName).rules), c), new Apply(builtTree(restRules), c.invert()));
                    case Reject r -> new Apply(builtTree(restRules), c.invert());
                    case Accept a -> new Node(new Apply(new AcceptAny(), c), new Apply(builtTree(restRules), c.invert()));
                    default -> throw new IllegalStateException("Unexpected value: " + rule.action);
                };
            }
        }

        Tree possibleParts() {
            return builtTree(map.get(START_WORKFLOW_NAME).rules);
        }
    }


    // a<2006
    Condition parseCondition(String s) {
        char tag = s.charAt(0);
        char cond = s.charAt(1);
        boolean lt = cond == '<';
        int condNumber = Integer.parseInt(s.substring(2));
        return new Condition(tag, lt, condNumber, false);
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
                var condition = parseCondition(condAndAction[0]);
                Action action = parseAction(condAndAction[1]);
                ruleList.add(new Rule(condition, action));
            } else {
                Action lastAction = parseAction(rule);
                ruleList.add(new Rule(null, lastAction));
            }
        }
        return new Workflow(name[0], ruleList);
    }


    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        List<Workflow> workflowList = new ArrayList<>();

        for (var line: lines) {
            if (line.isEmpty()) break;
            workflowList.add(parseWorkflow(line));
        }
//        for (var w: workflowList) System.out.println(w);
//        System.out.println("-------------------------------------");

        Map<String, Workflow> workflowMap = new HashMap<>();
        for (var w: workflowList) workflowMap.put(w.name, w);

        Workflows workflows = new Workflows(workflowMap);

        Tree tree = workflows.possibleParts();
//        printResult(tree, 0);

        return combinations(tree);
    }

}

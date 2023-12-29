import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        long result = printTimeSpent(Main::solve);
        System.out.println(result);
    }

    static Long solve() {
        try {
            return new AOC2023Day23Part2().solve();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Long printTimeSpent(Supplier<Long> f) {
        long t0 = System.nanoTime();
        long result = f.get();
        long t1 = System.nanoTime();
        long ms = (t1 - t0) / 1000000;
        String line = ms < 1000 ? "Took %d ms".formatted(ms) : "Took %.1f seconds".formatted(ms / 1000.0f);
        System.out.println(line);
        return result;
    }
}
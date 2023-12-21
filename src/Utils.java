import java.util.*;

public class Utils {


    static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    static long lcm(long a, long b) {
        return a * b / gcd(a, b);
    }

    static long lcm(List<Long> numbers) {
        return numbers.stream().reduce((a, b) -> lcm(a, b)).get();
    }


}

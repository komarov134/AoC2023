import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AOC2023Day12Part2 {

    String filePath = "input/AOC2023Day12.input";

    static char OPERATIONAL = '.';
    static char DAMAGED = '#';
    static char UNKNOWN = '?';


    // d[i][j] -- possible arrangements for i groups from the end and s starting from j
    // example(s length is 19): .??????#???#??????? 2,7,1,1
    // d[0][19] means empty list of groups and s=''
    // d[0][17] means empty list of groups and s='??'
    // d[2][17] means groups={1,1} and s='??'
    // d[3][17] means groups={7,1,1} and s='??'
    // d[3][10] means groups={7,1,1} and s='?#???????'
    // so d[4][0] is the answer
    record Arrangements(String s, int[] groups, long[][] d) {

        void init() {
            d[0][s.length()] = 1;
            for (int j = s.length() - 1; j >= 0; j--) {
                if (s.charAt(j) != DAMAGED) d[0][j] = d[0][j + 1];
            }
        }

        long get(int i, int j) {
            if (i < 0 || j >= d[0].length) return 0;
            return d[i][j];
        }

        boolean matchDamaged(int start, int groupSize) {
            int operationalOrUnknownIndex = start + groupSize;
            int lastExistingIndex = s.length() - 1;
            if (operationalOrUnknownIndex > lastExistingIndex) return false;
            for (int j = start; j < operationalOrUnknownIndex; j++) {
                if (s.charAt(j) == OPERATIONAL) return false;
            }
            return s.charAt(operationalOrUnknownIndex) != DAMAGED;
        }

        int group(int i) {
            return groups[groups.length - i];
        }

        void populate() {
            for (int i = 1; i < d.length; i++) {
                for (int j = d[0].length - 2; j >= 0 ; j--) {
                    char c = s.charAt(j);
                    if (c == OPERATIONAL) {
                        d[i][j] = get(i, j + 1);
                    } else if (c == DAMAGED) {
                        int groupSize = group(i);
                        if (matchDamaged(j, groupSize)) d[i][j] += get(i - 1, j + groupSize + 1);
                    } else if (c == UNKNOWN) {
                        int groupSize = group(i);
                        if (matchDamaged(j, groupSize)) d[i][j] += get(i - 1, j + groupSize + 1);
                        d[i][j] += get(i, j + 1);   // operational
                    } else {
                        throw new RuntimeException();
                    }
                }
            }
        }

        long possibleArrangements() {
            init();
            populate();
            return d[groups.length][0];
        }

    }

    Arrangements createArrangements(String s, int[] groups) {
        long[][] d = new long[groups.length + 1][s.length() + 1];
        return new Arrangements(s, groups, d);
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        long sum = 0;
        for (String line: lines) {
            String[] arr = line.split("\\s+");
            var s = arr[0];
            var groupsS = arr[1];
            var unfoldedS = String.join("?", Collections.nCopies(5, s));
//            var unfoldedS = s;
            var unfoldedGroupsS = String.join(",", Collections.nCopies(5, groupsS));
//            var unfoldedGroupsS = groupsS;
            var groups = Arrays.stream(unfoldedGroupsS.split(",")).mapToInt(Integer::parseInt).toArray();
            var endingWithOperational = unfoldedS + ".";    // it's needed for every group is closed
            var arrangements = createArrangements(endingWithOperational, groups);
            long possible = arrangements.possibleArrangements();
//            System.out.println("possible " + possible);
            sum += possible;
        }

        return sum;
    }

}

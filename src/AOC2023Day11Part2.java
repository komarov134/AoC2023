import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AOC2023Day11Part2 {

    String filePath = "input/AOC2023Day11.input";

    static char GALAXY = '#';
    static char SPACE = '.';

    static long MULTIPLIER = 1000000;

    record Position(int x, int y) {}

    record Field(char[][] map) {

        List<Integer> expandedRows() {
            List<Integer> rows = new ArrayList<>();
            char[] spaceRow = Arrays.copyOf(map[0], map[0].length);
            Arrays.fill(spaceRow, SPACE);
            for (int i = 0; i < map.length; i++) {
                if (Arrays.equals(map[i], spaceRow)) rows.add(i);
            }
            return rows;
        }

        List<Integer> expandedColumns() {
            List<Integer> columns = new ArrayList<>();
            for (int j = 0; j < map.length; j++) {
                boolean allSpaces = true;
                for (int i = 0; i < map.length && allSpaces; i++) {
                    if (map[i][j] != SPACE) allSpaces = false;
                }
                if (allSpaces) columns.add(j);
            }
            return columns;
        }

        List<Position> galaxies() {
            List<Position> positions = new ArrayList<>();
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    Position p = new Position(i, j);
                    if (isGalaxy(p)) positions.add(p);
                }
            }
            return positions;
        }

        boolean isGalaxy(Position p) {
            return map[p.x][p.y] == GALAXY;
        }

        long distance(Position p1, Position p2) {
            int minX = Math.min(p1.x, p2.x);
            int maxX = Math.max(p1.x, p2.x);
            int minY = Math.min(p1.y, p2.y);
            int maxY = Math.max(p1.y, p2.y);
            long addRows = expandedRows().stream().filter(x -> minX < x && x < maxX).count();
            long addColumns = expandedColumns().stream().filter(y -> minY < y && y < maxY).count();

            // part 2 difference
            addRows = addRows * (MULTIPLIER - 1);
            addColumns = addColumns * (MULTIPLIER - 1);

            return (maxX - minX + addRows) + (maxY - minY + addColumns);
        }


        long sumDistances() {
            List<Position> galaxies = galaxies();
            long sum = 0;
            for (int i = 0; i < galaxies.size(); i++) {
                for (int j = i + 1; j < galaxies.size(); j++) {
                    sum += distance(galaxies.get(i), galaxies.get(j));
                }
            }
            return sum;
        }

    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        int rows = lines.size();
        int columns = lines.get(0).length();

        char[][] map = new char[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                map[i][j] = lines.get(i).charAt(j);
            }
        }

        Field field = new Field(map);

        return field.sumDistances();
    }

}

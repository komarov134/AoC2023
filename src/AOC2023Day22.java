import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AOC2023Day22 {

    String filePath = "input/AOC2023Day22.input";


    static class Pos {
        public int x;
        public int y;
        public int z;

        Pos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "Pos{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }

    static class Brick {

        public Pos p1;
        public Pos p2;
        public int id;

        Brick(Pos p1, Pos p2, int id) {
            this.p1 = p1; this.p2 = p2; this.id = id;
        }

        void moveDown() {
            p1.z--;
            p2.z--;
        }

        void moveUp() {
            p1.z++;
            p2.z++;
        }

        boolean segmentIntersect(int a1, int a2, int b1, int b2) {
            return !(Math.min(a1, a2) > Math.max(b1, b2) || Math.min(b1, b2) > Math.max(a1, a2));
        }

        boolean intersect(Brick other) {
            return segmentIntersect(this.p1.x, this.p2.x, other.p1.x, other.p2.x) &&
                    segmentIntersect(this.p1.y, this.p2.y, other.p1.y, other.p2.y) &&
                    segmentIntersect(this.p1.z, this.p2.z, other.p1.z, other.p2.z);
        }

        int bottomLevel() {
            return Math.min(p1.z, p2.z);
        }

        @Override
        public String toString() {
            return "Brick{" +
                    "p1=" + p1 +
                    ", p2=" + p2 +
                    ", id=" + id +
                    '}';
        }
    }

    static boolean canGoDown(Brick brick, List<Brick> bricks) {
        if (brick.bottomLevel() == 1) return false; // it's on the ground
        brick.moveDown();
        boolean intersectsWithOther = bricks.stream().filter(b -> b.id != brick.id).anyMatch(brick::intersect);
        brick.moveUp();
        return !intersectsWithOther;
    }

    static void land(Brick brick, List<Brick> bricks) {
        while (canGoDown(brick, bricks)) brick.moveDown();
    }

    // non-optimized solution, but it's ok for Part 1
    static int countDisintegrateOptions(List<Brick> bricks) {
        int count = 0;
        for (var b: bricks) {
            List<Brick> withoutBrick = bricks.stream().filter(bb -> bb.id != b.id).toList();
            if (withoutBrick.stream().noneMatch(bb -> canGoDown(bb, withoutBrick))) count++;
        }
        return count;
    }


    static Pos parsePos(String s) {
        String[] numbers = s.split(",");
        return new Pos(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), Integer.parseInt(numbers[2]));
    }

    static Brick parseBrick(String s, int id) {
        String[] coords = s.split("~");
        return new Brick(parsePos(coords[0]), parsePos(coords[1]), id);
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        List<Brick> sortedBricks = new ArrayList<>();
        for (int i = 1; i <= lines.size(); i++) sortedBricks.add(parseBrick(lines.get(i - 1), i));
        sortedBricks.sort(Comparator.comparing(Brick::bottomLevel));

        List<Brick> bricks = new ArrayList<>();
        for (var brick: sortedBricks) {
            land(brick, bricks);
            bricks.add(brick);
        }

        return countDisintegrateOptions(bricks);
    }

}

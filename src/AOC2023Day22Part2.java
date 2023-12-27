import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AOC2023Day22Part2 {

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
            return "Pos{x=" + x + ", y=" + y + ", z=" + z + '}';
        }
    }

    record Brick(Pos p1, Pos p2, int id, AtomicBoolean removed) {

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
            if (other.removed.get()) return false;
            return segmentIntersect(this.p1.x, this.p2.x, other.p1.x, other.p2.x) &&
                    segmentIntersect(this.p1.y, this.p2.y, other.p1.y, other.p2.y) &&
                    segmentIntersect(this.p1.z, this.p2.z, other.p1.z, other.p2.z);
        }

        int bottomLevel() {
            return Math.min(p1.z, p2.z);
        }

        int upLevel() {
            return Math.max(p1.z, p2.z);
        }

    }

    record Indexes(List<Brick>[] levelByBottom, List<Brick>[] levelByUp) {

        void add(Brick brick) {
            levelByBottom[brick.bottomLevel()].add(brick);
            levelByUp[brick.upLevel()].add(brick);
        }

        List<Brick> allBricksAbove(Brick brick) {
            return levelByBottom[brick.upLevel() + 1];
        }

        List<Brick> allBricksBelow(Brick brick) {
            return levelByUp[brick.bottomLevel() - 1];
        }

    }

    static Indexes buildIndexes() {
        int levels = 300;
        Indexes indexes = new Indexes(new List[levels], new List[levels]);
        for (int i = 0; i < levels; i++) {
            indexes.levelByBottom[i] = new LinkedList<>();
            indexes.levelByUp[i] = new LinkedList<>();
        }
        return indexes;
    }

    static boolean canGoDown(Brick brick, Indexes indexes) {
        if (brick.bottomLevel() == 1) return false; // it's on the ground
        List<Brick> bricksBelow = indexes.allBricksBelow(brick);
        brick.moveDown();
        boolean intersectsWithOther = bricksBelow.stream().anyMatch(brick::intersect);
        brick.moveUp();
        return !intersectsWithOther;
    }

    static void land(Brick brick, Indexes indexes) {
        while (canGoDown(brick, indexes)) brick.moveDown();
        indexes.add(brick);
    }

    static int countDisintegrateOptions(List<Brick> bricks, Indexes indexes) {
        int count = 0;
        for (var brick : bricks) {
            brick.removed.set(true);
            if (indexes.allBricksAbove(brick).stream().noneMatch(b -> canGoDown(b, indexes))) count++;
            brick.removed.set(false);
        }
        return count;
    }

    static void markFallenBricks(Brick initial, Indexes indexes) {
        Queue<Brick> queue = new LinkedList<>();
        queue.offer(initial);
        while (!queue.isEmpty()) {
            Brick removedBrick = queue.poll();
            if (removedBrick.removed.get()) continue;
            removedBrick.removed.set(true);
            for (var brickAbove: indexes.allBricksAbove(removedBrick)) if (canGoDown(brickAbove, indexes)) queue.offer(brickAbove);
        }
    }

    static Pos parsePos(String s) {
        String[] numbers = s.split(",");
        return new Pos(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), Integer.parseInt(numbers[2]));
    }

    static Brick parseBrick(String s, int id) {
        String[] coords = s.split("~");
        return new Brick(parsePos(coords[0]), parsePos(coords[1]), id, new AtomicBoolean());
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        List<Brick> sortedBricks = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) sortedBricks.add(parseBrick(lines.get(i), i));
        sortedBricks.sort(Comparator.comparing(Brick::bottomLevel));

        List<Brick> bricks = new ArrayList<>();
        Indexes indexes = buildIndexes();
        for (var brick : sortedBricks) {
            land(brick, indexes);
            bricks.add(brick);
        }

        long sum = 0;
        for (var brick: sortedBricks) {
            markFallenBricks(brick, indexes);
            long count = bricks.stream().filter(b -> b.removed.getAndSet(false)).count() - 1;  // do not count the initial one
            sum += count;
            System.out.println("processed " + brick + ". Removing it would fall " + count + " bricks above");
        }

        return sum;
    }

}

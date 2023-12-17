import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class AOC2023Day07Part2 {

    String filePath = "input/AOC2023Day07.input";

    // A, K, Q, T, 9, 8, 7, 6, 5, 4, 3, 2, J
    enum Card {
        JOKERS('J'),
        TWO('2'), THREE('3'), FOUR('4'), FIVE('5'), SIX('6'),
        SEVEN('7'), EIGHT('8'), NINE('9'), TEN('T'),
        QUEEN('Q'), KING('K'), ACE('A');

        public final char symbol;

        Card(char symbol) {
            this.symbol = symbol;
        }

    }

    record Hand(List<Card> cards) implements Comparable<Hand> {

        private Map<Card, Long> countCards() {
            return cards.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        }

        private String makeCountString(Map<Card, Long> cardCounts) {
            return cardCounts.values().stream().sorted().map(Object::toString).collect(Collectors.joining());
        }

        private int firstOrderFrom(String countString) {
            return switch (countString) {
                // Five of a kind, where all five cards have the same label: AAAAA
                case "5" -> 7;
                // Four of a kind, where four cards have the same label and one card has a different label: AA8AA
                case "14" -> 6;
                // Full house, where three cards have the same label, and the remaining two cards share a different label: 23332
                case "23" -> 5;
                // Three of a kind, where three cards have the same label, and the remaining two cards are each different from any other card in the hand: TTT98
                case "113" -> 4;
                // Two pair, where two cards share one label, two other cards share a second label, and the remaining card has a third label: 23432
                case "122" -> 3;
                // One pair, where two cards share one label, and the other three cards have a different label from the pair and each other: A23A4
                case "1112" -> 2;
                // High card, where all cards' labels are distinct: 23456
                case "11111" -> 1;
                default -> throw new RuntimeException("Unexpected hand: " + countString + ". Cards: " + cards);
            };
        }

        int pretendingFirstOrder() {
            Map<Card, Long> cardCounts = countCards();
            long jokersCount = cardCounts.getOrDefault(Card.JOKERS, 0L);

            if (jokersCount == 5) {
                return 7;   // pretend all cards are the equal
            } else {
                Map.Entry<Card, Long> maxFreqCard = cardCounts.entrySet().stream()
                        .filter(e -> !e.getKey().equals(Card.JOKERS))
                        .max(Comparator.comparingLong(e -> e.getValue())).get();
                // build new card counts
                Map<Card, Long> newCardCounts = new HashMap<>();
                for (Map.Entry<Card, Long> e: cardCounts.entrySet()) {
                    if (!e.getKey().equals(Card.JOKERS)) {
                        long addJokers = e.getKey().equals(maxFreqCard.getKey()) ? jokersCount : 0L;
                        newCardCounts.put(e.getKey(), e.getValue() + addJokers);
                    }
                }

                return firstOrderFrom(makeCountString(newCardCounts));
            }
        }



        // TWO is 'a', Three is 'b' and so on
        String comparableString() {
            return cards.stream().map(c -> (char) ('a' + c.ordinal())).map(String::valueOf).collect(Collectors.joining());
        }


        @Override
        public int compareTo(Hand o) {
            int diff = this.pretendingFirstOrder() - o.pretendingFirstOrder();
            if (diff == 0) {
                return this.comparableString().compareTo(o.comparableString());
            } else {
                return diff;
            }
        }

    }

    record HandBid(Hand hand, long bid) implements Comparable<HandBid> {

        @Override
        public int compareTo(HandBid o) {
            return this.hand.compareTo(o.hand);
        }
    }

    Hand parseHand(String s) {
        List<Card> cards = new ArrayList<>();
        for (char c: s.toCharArray()) {
            Card foundCard = Arrays.stream(Card.values()).filter(card -> card.symbol == c).findFirst().get();
            cards.add(foundCard);
        }
        return new Hand(cards);
    }

    HandBid parse(String line) {
        String[] arr = line.split("\\s+");
        return new HandBid(parseHand(arr[0]), Long.parseLong(arr[1]));
    }

    public long solve() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

        List<HandBid> sortedHands = lines.stream().map(this::parse).sorted().toList();
        int rank = 1;
        long total = 0;
        for (HandBid handBid: sortedHands) {
            total += handBid.bid * rank++;
        }
        return total;
    }

}

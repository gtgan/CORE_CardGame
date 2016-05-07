package cards;

// Definitely not complete
public class CardTest {
    public static void main(String[] args) {
        CardLibrary lib = new CardLibrary(args.length > 0 ? args[0] : "cards/Cards.txt");
        System.out.println(lib.size() + " cards");
        Deck[] decks = new Deck[2];
        for (int i = 0; i < 2; i ++) {
            decks[i] = new Deck();
            for (int j = 0; j < 32; j ++)
                decks[i].add(lib.getRandomCard());
        }
        Player[] players = Player.initPlayers(decks[0], decks[1]);
        printPlayers(true, players);
    }
    public static void printPlayers(boolean printDeck, Player... players) {
        for (int i = 0; i < players.length; i ++) {
            System.out.printf("Player %d: %d/%d MP, %d/%d HP\n", i, players[i].getMana(), players[i].getMaxMana(),
                              players[i].getHealth(), players[i].getMaxHealth());
            System.out.print("Hand:");
            Card[] hand = players[i].getHand();
            for (int j = 0; j < hand.length; j ++) {
                System.out.print("\t" + j + ": ");
                if (hand[j] != null)
                    System.out.print(cardSummary(hand[j]));
                System.out.println();
            }
            if (printDeck) {
                System.out.print("Deck:");
                Deck deck = players[i].getDeck();
                for (Card card : deck)
                    System.out.println("\t   " + cardSummary(card));
            }
        }
    }
    public static String cardSummary(Card card) {
        String str = card.getName() + ": " + card.getManaCost() + " MP";
        if (card instanceof MonsterCard) {
            MonsterCard mcard = (MonsterCard) card;
            str += ", " + mcard.getAtk() + " ATK / " + mcard.getDef() + " DEF, tier " + mcard.sacrifices;
        }
        return str;
    }
}

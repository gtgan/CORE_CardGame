package cards;

import java.io.*;

// Definitely not complete
public class CardTest implements Closeable {
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
        System.out.println("\n");
        CardTest test = new CardTest(players, new InputStream[]{System.in, System.in});
        int winner = test.game();
        try {
            test.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println(winner < 0 ? "No winner" : String.format("Player %d wins", winner));
    }

    private BufferedReader[] readers;
    private Player[] players;
    private boolean unfinished;

    /** Creates a new CardTest, which is an incomplete version of the game.
      * @param players an array of the two players, which should be opponents of one another
      * @param inStreams an array of two input streams which should be used by the respective players */
    public CardTest(Player[] players, InputStream[] inStreams) {
        this.players = players;
        readers = new BufferedReader[inStreams.length];
        for (int i = 0; i < inStreams.length; i ++)
            readers[i] = new BufferedReader(new InputStreamReader(inStreams[i]));
    }

    @Override
    public void close() throws IOException {
        for (Reader reader : readers)
            reader.close();
    }
    public boolean isFinished() { return !unfinished; }
    public int game() {
        int action;
        unfinished = true;
        do {
            monsterPhase(players[0], readers[0]);
            // monsterPhase(players[1], readers[1]);
            // TODO magic and modifier phases
            printPlayers(false, true, true, false, players);
            for (Player p : players)
                p.battlePhase();
            System.out.println("Turn over");
            printPlayers(true, true, true, false, players[0]);
            for (int i = 0; i < players.length; i ++) { // check if is over
                boolean handEmpty = true;
                for (Card card : players[i].getHand())
                    if (card != null) {
                        handEmpty = true;
                        break;
                    }
                boolean monstersEmpty = true;
                for (Card card : players[i].getMonsters())
                    if (card != null) {
                        monstersEmpty = true;
                        break;
                    }
                if (players[i].isDead() || players[i].getDeck().size() == 0 && handEmpty && monstersEmpty) {
                    unfinished = false;
                    return (i + 1) % 2;
                }
            }
        } while (unfinished);
        unfinished = false;
        return -1;
    }
    private int magicPhase(Player player, BufferedReader in) {
        printPlayers(true, true, true, false, player);
        Card cardToPlay = null;
        MagicCard card = null;
        int playFrom;
        Player playTo = player;
        do {
            playFrom = 7;
            System.out.print("Select magic card index (out of bounds to skip): ");
            try {
                playFrom = Integer.parseInt(in.readLine());
                if (playFrom < 0 || playFrom > 6) {
                    System.out.println("Skipping");
                    return -1;
                }
                cardToPlay = player.getHand()[playFrom];
                if (cardToPlay == null || !(cardToPlay instanceof MagicCard)) {
                    System.out.println("Not a magic card; please try again: ");
                    playFrom = 7;
                } else if (cardToPlay.getManaCost() > player.getMana()) {
                    System.out.print("Not enough mana; please try again: ");
                    playFrom = 8;
                } else {
                    card = (MagicCard) cardToPlay;
                    Ability abil = card.getAbilities()[0]; // get primary ability
                }
            } catch(Exception e) {
                System.out.print("Invalid input; please try again: ");
            }
        } while (playFrom > 6);
        return -1;
    }
    private int monsterPhase(Player player, BufferedReader in) {
        printPlayers(true, true, true, false, player);
        Card cardToPlay = null;
        int playFrom;
        do {
            playFrom = 7;
            System.out.print("Select monster card index (out of bounds to skip): ");
            try {
                playFrom = Integer.parseInt(in.readLine());
                if (playFrom < 0 || playFrom > 6) {
                    System.out.println("Skipping");
                    return -1;
                }
                cardToPlay = player.getHand()[playFrom];
                if (cardToPlay == null || !(cardToPlay instanceof MonsterCard)) {
                    System.out.print("Not a monster card; please try again: ");
                    playFrom = 7;
                } else if (cardToPlay.getManaCost() > player.getMana()) {
                    System.out.print("Not enough mana; please try again: ");
                    playFrom = 8;
                } else {
                    int monstersInPlay = 0;
                    for (MonsterCard monster : player.getMonsters())
                        if (monster != null)
                            monstersInPlay ++;
                    if (monstersInPlay < ((MonsterCard) cardToPlay).sacrifices) {
                        System.out.print("Not enough sacrifices; please try again: ");
                    }
                }
            } catch(Exception e) {
                System.out.print("Invalid input; please try again: ");
                playFrom = 10;
            }
        } while (playFrom > 6);

        MonsterCard card = (MonsterCard) cardToPlay;
        if (card.sacrifices > 0) {
            printPlayers(false, true, false, false, player);
            System.out.print("Select " + card.sacrifices + " monster ind" + (card.sacrifices == 1 ? "ex" : "ices") + " to sacrifice (out of bounds to cancel): ");
            int[] sacrifices = new int[card.sacrifices];
            MonsterCard[] toSacrifice = new MonsterCard[card.sacrifices];
            for (int i = 0; i < sacrifices.length; i ++)
                do {
                    sacrifices[i] = 5;
                    try {
                        sacrifices[i] = Integer.parseInt(in.readLine());
                        if (sacrifices[i] < 0 || sacrifices[i] > 4) {
                            System.out.println("Cancelling monster phase");
                            return -1;
                        }
                        toSacrifice[i] = player.getMonsters()[sacrifices[i]];
                        if (toSacrifice[i] == null) {
                            System.out.println("No monster at index " + i);
                            sacrifices[i] = 5;
                        }
                    } catch(Exception e) {
                        System.out.println("Invalid input");
                        sacrifices[i] = 6;
                    }
                } while (sacrifices[i] > 4);
            for (int i = 0; i < toSacrifice.length; i ++) {
                toSacrifice[i].sacrifice();
                player.getGraveyard().add(toSacrifice[i]);
                player.getMonsters()[sacrifices[i]] = null;
            }
        }

        MonsterCard[] monsters = player.getMonsters();
        for (int i = 0; i < monsters.length; i ++)
            if (monsters[i] == null) {
                player.play(playFrom, i, player);
                break;
            }
        return playFrom;
    }

    private void printPlayers(boolean printHand, boolean printMonsters, boolean printModifiers, boolean printDeck, Player... players) {
        for (int i = 0; i < players.length; i ++) {
            System.out.printf("Player %d: %d/%d MP, %d/%d HP\n", i, players[i].getMana(), players[i].getMaxMana(),
                              players[i].getHealth(), players[i].getMaxHealth());
            if (printHand) {
                System.out.print("Hand:");
                printCards(players[i].getHand());
            }
            if (printMonsters) {
                System.out.print("Mons:");
                printCards(players[i].getMonsters());
            }
            if (printModifiers) {
                System.out.print("Mods:");
                printCards(players[i].getModifiers());
            }
            if (printDeck) {
                System.out.print("Deck:");
                Deck deck = players[i].getDeck();
                for (Card card : deck)
                    System.out.println("\t   " + cardSummary(card));
            }
        }
    }
    private void printCards(Card[] cards) {
        for (int j = 0; j < cards.length; j ++) {
            System.out.print("\t" + j + ": ");
            if (cards[j] != null)
                System.out.print(cardSummary(cards[j]));
            System.out.println();
        }
    }
    private String cardSummary(Card card) {
        String str = card.getName() + ": " + card.getManaCost() + " MP";
        if (card instanceof MonsterCard) {
            MonsterCard mcard = (MonsterCard) card;
            str += ", " + mcard.getAtk() + " ATK / " + mcard.getDef() + " DEF, tier " + mcard.sacrifices;
        }
        return str;
    }
}

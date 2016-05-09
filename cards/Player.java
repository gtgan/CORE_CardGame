package cards;

import java.util.LinkedList;

public class Player implements PlayerInterface {
    private Player opponent;
    private Deck deck;
    private Card[] hand;
    private MonsterCard[] monsters;
    private ModifierCard[] modifiers;
    private int mana, maxMana, manaRegen, health, maxHealth;
    private LinkedList<Card> graveyard;
    private LinkedList<Card> playedThisTurn;

    public Player(Deck deck) {
        mana = 30;
        maxMana = 100;
        manaRegen = 10;
        health = maxHealth = 50;
        hand = new Card[7]; // at most 7 cards in hand
        monsters = new MonsterCard[5]; // 0 index is main monster
        modifiers = new ModifierCard[3];
        graveyard = new LinkedList<Card>();
        this.deck = deck;
        boolean needsL0Monster = false;
        for (int i = 1; i < 4; i ++) {
            hand[i] = draw();
            if (hand[i] instanceof MonsterCard && ((MonsterCard) hand[i]).sacrifices == 0)
                needsL0Monster = true;
        }
        hand[0] = draw(needsL0Monster);
        playedThisTurn = new LinkedList<Card>();
    }

    @Override public MonsterCard drawL0Monster() { return (MonsterCard) draw(true); }
    @Override public Card draw()                 { return draw(false); }
    @Override
    public Card draw(boolean l0Monster) {
        int i = 0;
        Card c = null;
        for (; i < hand.length; i ++) // cannot draw if hand is full
            if (hand[i] == null)
                break;
        if (i < hand.length) {
            c = (l0Monster ? deck.drawL0Monster() : deck.draw());
            hand[i] = c;
        }
        return c;
    }
    @Override
    public Card play(int placeInHand, int placeToPlay, Player playTo) {
        Card toPlay = hand[placeInHand];
        if (toPlay == null || toPlay.getManaCost() > mana)
            return null;
        else if (toPlay instanceof MonsterCard) {
            MonsterCard monster = (MonsterCard) toPlay;
            if (monsters[placeToPlay] != null)
                return null;
            monsters[placeToPlay] = monster;
        } else if (toPlay instanceof MagicCard) { // TODO implement play magic
            MagicCard magic = (MagicCard) toPlay;
            if (playTo.getMonsters()[placeToPlay] == null)
                return null;
        } else if (toPlay instanceof ModifierCard) {
            ModifierCard modifier = (ModifierCard) toPlay;
            if (playTo.getModifiers()[placeToPlay] != null)
                return null;
            playTo.getModifiers()[placeToPlay] = modifier;
        }
        hand[placeInHand] = null;
        mana -= toPlay.getManaCost();
        playedThisTurn.add(toPlay);
        return toPlay;
    }
    @Override
    public Card discardFromHand(int index) {
        Card toDiscard = hand[index];
        if (toDiscard != null) {
            hand[index] = null;
            graveyard.add(toDiscard);
        }
        return toDiscard;
    }

    @Override public int              getHealth()    { return health;    }
    @Override public int              getMana()      { return mana;      }
    @Override public int              getMaxHealth() { return maxHealth; }
    @Override public int              getMaxMana()   { return maxMana;   }
    @Override public int              getManaRegen() { return manaRegen; }
    @Override public Player           getOpponent()  { return opponent;  }
    @Override public Deck             getDeck()      { return deck;      }
    @Override public Card[]           getHand()      { return hand;      }
    @Override public MonsterCard[]    getMonsters()  { return monsters;  }
    @Override public ModifierCard[]   getModifiers() { return modifiers; }
    @Override public LinkedList<Card> getGraveyard() { return graveyard; }

    @Override
    public int changeHealth(int change) {
        health = Math.min(health + change, maxHealth);
        return health;
    }
    @Override
    public int changeMana(int change) {
        mana = Math.min(mana + change, maxMana);
        return mana;
    }
    @Override
    public int changeMaxHealth(int change) {
        maxHealth = Math.max(1, maxHealth + change);
        if (health > maxHealth)
            health = maxHealth;
        return maxHealth;
    }
    @Override
    public int changeMaxMana(int change) {
        maxMana = Math.max(1, maxMana + change);
        if (mana > maxMana)
            mana = maxMana;
        return maxMana;
    }
    @Override
    public int changeManaRegen(int change) {
        manaRegen += change;
        return manaRegen;
    }

    @Override
    public Player setOpponent(Player opponent) {
        Player hold = this.opponent;
        this.opponent = opponent;
        this.opponent.opponent = this;
        return hold;
    }

    @Override
    public int attack(Card attacker, Attackable... targets) {
        for (Attackable target : targets)
            if (attacker instanceof MonsterCard) {
                target.defend((MonsterCard) attacker, ((MonsterCard) attacker).getAtk(), true);
            } else {
                // TODO implement attack for non-monsters
            }
        return targets.length;
    }
    @Override
    public int attack(Card attacker, boolean useSpecialIfPossible) {
        int dmg = 0;
        Ability ability = null;
        LinkedList<Attackable> targets = new LinkedList<Attackable>();
        if (attacker instanceof MonsterCard) {
            if (useSpecialIfPossible) {
                for (Ability a : attacker.getAbilities())
                    if (a.activateType.contains("active")) {
                        ability = a;
                        break;
                    }
                if (ability != null) {
                    if (ability.targetSpec.contains("player")) {
                        if (ability.target.contains("all"))
                            targets.add(this);
                        if (ability.target.contains("opponent") || ability.target.equals("all"))
                            targets.add(opponent);
                    }
                }
                for (Attackable target : targets)
                    applyTo(ability, attacker, target);
            } else
                dmg = ((MonsterCard) attacker).getAtk();
        } else {
            // TODO implement attack for non-monsters
        }
        int i = 0;
        for (Attackable defender : targets)
            if (useSpecialIfPossible && ability != null) {
                applyTo(ability, attacker, defender);
            } else {
                i += defender.defend(attacker, dmg, true);
            }
        return i;
    }

    @Override
    public int defend(Card attacker, int damage, boolean allowCounter) {
        int hold = Math.min(damage, health);
        health -= damage;
        if (isDead())
            death(null);
        return hold;
    }

    @Override public boolean isDead() { return health <= 0; }
    @Override
    public void death(Card attacker) {
        if (attacker instanceof Attackable && !((Attackable) attacker).isDead()) {
            // TODO implement onDeath ability activation
        }
    }

    public int getNumCardsLeft() { return deck.size(); }
    public MonsterCard getMainMonster() { return monsters[0]; }

    public void battlePhase(Card... useSpecial) {
        for (Card card : playedThisTurn) {
            for (Ability ability : card.getAbilities()) {
                if (ability.activateType.contains("onPlay")) {
                    if (ability.targetSpec.contains("player")) { // abilities with effects on players
                        boolean both = ability.target.contains("all");
                        Player[] target = new Player[both ? 1 : 2];
                        if (both || ability.target.contains("ally"))
                            target[0] = this;
                        if (ability.target.contains("opponent"))
                            target[target.length - 1] = opponent;
                        for (Player p : target)
                            applyTo(ability, card, p);
                    } else { // TODO implement abilities with non-player targets

                    }
                }
            }
        }
        for (Card sCard : useSpecial)
            attack((MonsterCard) sCard, true);
        if (monsters[0] != null) {
            boolean contains = false;
            for (Card c : useSpecial)
                if (c.equals(monsters[0]))
                    contains = true;
            if (!contains) {
                MonsterCard opponentMainMon = opponent.getMainMonster();
                monsters[0].attack(opponentMainMon == null ? opponent : opponentMainMon);
            } else {

            }
        }
        playedThisTurn.clear();
        changeMana(manaRegen);
        for (int i = 0; i < hand.length; i ++)
            if (hand[i] == null) {
                hand[i] = draw();
                break;
            }
    }
    public void buryDead() { // remove dead monsters from the field
        for (int i = 0; i < monsters.length; i ++)
            if (monsters[i].isDead()) {
                graveyard.add(monsters[i]);
                monsters[i] = null;
            }
        // TODO remove destroyed Magic and Modifier cards
    }
    private boolean applyTo(Ability ability, Card source, Object target) { // apply ability effect from source to target
        if (! (target instanceof Attackable || target instanceof Card) || ability.manaCost > mana)
            return false;
        switch(ability.abilityType) { // could have used reflection, but this seems more stable
            case "damage":
                if (target instanceof Attackable) {
                    ((Attackable) target).defend(source, (int) ability.magnitude, true);
                    break;
                }
                return false;
            case "changeAtk":
                if (target instanceof MonsterCard) {
                    ((MonsterCard) target).changeAtk((int) ability.magnitude);
                    break;
                }
                return false;
            case "changeDef":
                if (target instanceof MonsterCard) {
                    ((MonsterCard) target).changeDef((int) ability.magnitude);
                    break;
                }
                return false;
            case "changeHealth":
                if (target instanceof Player) {
                    ((Player) target).changeHealth((int) ability.magnitude);
                    break;
                } else if (target instanceof MonsterCard) {
                    ((MonsterCard) target).changeHP((int) ability.magnitude);
                    break;
                }
                return false;
            case "changeMaxHealth":
                if (target instanceof Player) {
                    ((Player) target).changeMaxHealth((int) ability.magnitude);
                    break;
                } else if (target instanceof MonsterCard) {
                    ((MonsterCard) target).changeDef((int) ability.magnitude);
                    break;
                }
                return false;
            case "changeMana":
                if (target instanceof Player) {
                    ((Player) target).changeMana((int) ability.magnitude);
                    break;
                }
                return false;
            case "changeMaxMana":
                if (target instanceof Player) {
                    ((Player) target).changeMaxMana((int) ability.magnitude);
                    break;
                }
                return false;
            case "changeManaRegen":
                if (target instanceof Player) {
                    ((Player) target).changeManaRegen((int) ability.magnitude);
                    break;
                }
                return false;
            case "destroy":
                if (target instanceof MonsterCard) {
                    ((MonsterCard) target).changeHP(-512);
                    break;
                } else if (target instanceof MagicCard || target instanceof ModifierCard) {
                    // TODO flag target for removal
                }
                return false;
            case "negateEffect":
                // TODO implement negate effect
                break;
        }
        mana -= ability.manaCost;
        return true;
    }

    /**
     * The preferred method for instantiating two Player objects which are opponents of one another.
     * @param deck1 Player 0's deck
     * @param deck2 Player 1's deck
     * @return an array containing Player 0 and Player 1
     */
    public static Player[] initPlayers(Deck deck0, Deck deck1) {
        Player[] players = new Player[2];
        players[0] = new Player(deck0);
        players[1] = new Player(deck1);
        players[0].setOpponent(players[1]);
        return players;
    }
}

package cards;

import java.util.LinkedList;

public interface PlayerInterface extends Attackable {
    /**
     * Draws a card from the deck, if possible.
     * @param l0Monster whether the card drawn must be a Level 0 monster
     * @return a card which should be a Level 0 monster if specified, or null if no such card can be found
     */
    default Card draw(boolean l0Monster) { return (l0Monster ? drawL0Monster() : draw()); }
    /**
     * Draws a card from the deck, if possible.
     * @return a card, or null if the deck is empty
     */
    public Card draw();
    /**
     * Draws a Level 0 (no-sacrifices-required) monster from the deck, if possible.
     * @return a Level 0 monster, or null if none is available
     */
    public Card drawL0Monster();
    public Card[] getHand();
    public Card[] getMonsters();
    public Card[] getModifiers();
    public LinkedList<Card> getGraveyard();
    /**
     * Plays a card.
     * @param placeInHand the desired card's index in the hand
     * @param placeToPlay the desired card's destination index in an applicable space, depending on the card's
     * type and abilities
     * @param playTo the player on whom the card should be played
     * @return the card played
     */
    public Card play(int placeInHand, int placeToPlay, Player playTo);
    /**
     * Sends the card at the specified index in the hand to the graveyard.
     * @param index the index of the card to discard
     * @return the discarded card
     */
    public Card discardFromHand(int index);
    /*public void monsterPhase(MonsterCard monster, int toPosition);
    public void magicPhase(MagicCard[] magics, Object[] magicTargets, MagicCard[] modifiers,
                           int[] modifierToPosition, Player[] modifierPlayTo);
    public void battlePhase(MonsterCard... useAbilityInsteadOfAttack);*/
    public Player getOpponent();
    /**
     * Sets this player's opponent.
     * @param opponent the new opponent
     * @return the previous opponent
     */
    public Player setOpponent(Player opponent);
    public int getMana();
    /**
     * Changes this player's amount of mana.
     * @param change the change in mana
     * @return the new mana value
     */
    public int changeMana(int change);
    public int getMaxMana();
    /**
     * Changes this player's maximum amount of mana.
     * @param change the change in maximum mana
     * @return the new maximum mana value
     */
    public int changeMaxMana(int change);
    public int getHealth();
    /**
     * Changes this player's amount of health.
     * @param change the change in health
     * @return the new health value
     */
    public int changeHealth(int change);
    public int getMaxHealth();
    /**
     * Changes this player's maximum amount of health.
     * @param change the change in maximum health
     * @return the new maximum health value
     */
    public int changeMaxHealth(int change);
    public int getManaRegen();
    /**
     * Changes this player's mana regen.
     * @param change the change in mana regen
     * @return the new mana regen
     */
    public int changeManaRegen(int change);
    public Deck getDeck();
    public int attack(Card attacker, Attackable... targets);
    public int attack(Card attacker, boolean useSpecialIfPossible);
}

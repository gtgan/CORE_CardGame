package cards;

import java.util.LinkedList;
import java.util.ListIterator;

public class MonsterCard extends Card implements Attackable {
    public final int sacrifices, manaCost;
    private int atk, def, hp;
    private LinkedList<MagicCard> equips;
    private Ability[] abilities;

    public MonsterCard(String name, String description, int manaCost, int atk, int def, int sacrifices) {
        this(name, description, manaCost, null, atk, def, sacrifices);
    }
    public MonsterCard(String name, String description, Element element, int manaCost, int atk, int def,
                       int sacrifices) {
        this(name, description, element, manaCost, atk, def, sacrifices, null);
    }
    public MonsterCard(String name, String description, int manaCost, Ability[] abilities, int atk, int def,
                       int sacrifices) {
        this(name, description, null, manaCost, atk, def, sacrifices, abilities);
    }
    public MonsterCard(String name, String description, Element element, int manaCost, int atk, int def,
                       int sacrifices, Ability[] abilities) {
        super(name, description, element, manaCost, abilities);
        this.sacrifices = sacrifices;
        this.manaCost = manaCost;
        this.hp = this.def = def;
        this.atk = atk;
        equips = new LinkedList<MagicCard>();
    }

    public int attack(Attackable target) { return attack(target, atk, true); }
    public int attack(Attackable target, int damage, boolean allowCounter) {
        if (!(target instanceof Attackable))
            return 0;
        // TODO implement attack player if target == null
        return target.defend(this, damage, allowCounter);
    }
    @Override
    public int defend(Card attacker, int damage, boolean allowCounter) {
        for (Ability a : abilities)
            if (a != null && a.activateType.equals("attacked"))
                switch(a.abilityType) {
                case "counter":
                    if (allowCounter)
                        attack((MonsterCard) attacker, (int) a.magnitude, false);
                }
        hp -= Math.max(attacker.getElement().dmgOn(getElement(), damage), 0); // don't heal
        if (hp <= 0)
            death(allowCounter ? attacker : null);
        return damage;
    }
    public void sacrifice() { death(null); }
    @Override
    public void death(Card attacker) {
        if (attacker != null)
            for (Ability a : abilities)
                if (a != null && a.activateType.equals("death"))
                    switch(a.abilityType) {
                    case "counter":
                        attack((MonsterCard) attacker, (int) a.magnitude, false);
                        break;
                    // More cases will be added as the need arises.
                    }
    }

    public boolean equip(MagicCard equip)              { return equips.add(equip); }
    public boolean equip(LinkedList<MagicCard> equips) { return this.equips.addAll(equips); }
    public LinkedList<MagicCard> getEquips()           { return equips; }
    public MagicCard removeEquip(MagicCard equip) {
        ListIterator<MagicCard> iter = equips.listIterator();
        while (iter.hasNext()) {
            MagicCard card = iter.next();
            if (card.equals(equip)) {
                iter.remove();
                return card;
            }
        }
        return null;
    }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getHP()  { return hp;  }
    public int changeAtk(int change) {
        atk = Math.min(0, atk + change);
        return atk;
    }
    public int changeDef(int change) {
        if (isDead() || hp - change <= 0)
            return 0;
        def += change;
        hp += change;
        return def;
    }
    public int changeHP(int change) {
        hp = Math.min(hp + change, def);
        return hp;
    }
    @Override public boolean isDead() { return hp <= 0; }

    @Override
    public Card clone() {
        return new MonsterCard(getName(), getDescription(), getElement(), manaCost, atk, def, sacrifices,
                               abilities);
    }
}

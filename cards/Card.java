package cards;

public abstract class Card implements Comparable<Card>, CardInterface {
    private int manaCost;
    private boolean faceUp;
    private Element element;
    private String name, description;
    private Ability[] abilities;

    public Card(String name, String description, int manaCost) { this(name, description, manaCost, null); }
    public Card(String name, String description, Element element, int manaCost) {
        this(name, description, element, manaCost, null);
    }
    public Card(String name, String description, int manaCost, Ability[] abilities) {
        this(name, description, null, manaCost, abilities);
    }
    public Card(String name, String description, Element element, int manaCost, Ability[] abilities) {
        this.name = name;
        this.description = description;
        this.manaCost = manaCost;
        this.element = element;
        this.abilities = abilities;
        faceUp = false;
    }

    @Override
    public boolean setAbilities(Ability... abilities) {
        if (this.abilities == null) {
            this.abilities = abilities;
            return true;
        }
        return false;
    }

    @Override public Element getElement()      { return element; }
    @Override public String getName()          { return name; }
    @Override public String getDescription()   { return description; }
    @Override public Ability[] getAbilities()  { return abilities; }
    @Override public String getType()          { return getClass().getSimpleName(); }
    @Override public int compareTo(Card other) { return name.compareTo(other.getName()); }
    @Override public int getManaCost()         { return manaCost; }
    @Override public boolean isFaceUp()        { return faceUp; }
    @Override
    public boolean flip() {
        faceUp = !faceUp;
        return faceUp;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+" \""+name+"\" ("+element+", "+ manaCost+"): "+description;
    }
}

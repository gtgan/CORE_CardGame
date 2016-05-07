package cards;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * A loader and factory class for cards.
 */
public class CardLibrary {
    private HashMap<String, String> library;

    public CardLibrary(String filePath) {
        library = new HashMap<String, String>();
        load(new File(filePath));
    }

    public boolean load(File file) {
        String str = null;
        try(Scanner in = new Scanner(file)) {
            while (in.hasNextLine()) {
                str = in.nextLine();
                Card card = parseCard(str);
                if (card != null)
                    library.putIfAbsent(card.getName(), str);
            }
        } catch(Exception e) {
            System.out.println(str);
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public Card getCard(String name) { return parseCard(library.get(name)); }
    public Card getRandomCard() {
        String[] cards = library.values().toArray(new String[library.size()]);
        return parseCard(cards[(int) (Math.random() * cards.length)]);
    }
    public int size() { return library.size(); }

    private Card parseCard(String str) {
        Scanner hold = new Scanner(str);
        Scanner in = hold.useDelimiter("`");
        Card card = null;
        if (!in.hasNext())
            return card;
        String type = in.next();
        if (!(type.equals("mon") || type.equals("mag") || type.equals("mod")))
            return card;
        String name = in.next();
        String description = in.next();
        String el = in.next();
        Element element = null;
        if (!el.equals("null"))
            element = Element.valueOf(el.toUpperCase());
        int manaCost = Integer.parseInt(in.next());
        switch(type) {
            case "mon": // add atk, def, sacrifices
                card = new MonsterCard(name, description, element, manaCost, Integer.parseInt(in.next()),
                                       Integer.parseInt(in.next()), Integer.parseInt(in.next()));
                break;
            case "mag": // add isEquip
                card = new MagicCard(name, description, element, manaCost, Boolean.parseBoolean(in.next()));
                break;
            case "mod":
                card = new ModifierCard(name, description, element, manaCost);
                break;
            default:
                // um... I dunno.
        }
        LinkedList<Ability> abilities = new LinkedList<Ability>();
        while (in.hasNext())
            abilities.add(new Ability(in.next()));
        card.setAbilities(abilities.toArray(new Ability[abilities.size()]));
        hold.close();
        in.close();
        return card;
    }
}

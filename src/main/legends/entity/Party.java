package legends.entity;

import java.util.*;

public final class Party {
    private final List<Hero> heroes;
    private int row, col;

    public Party(List<Hero> heroes) {
        this.heroes = Collections.unmodifiableList(new ArrayList<>(heroes));
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public List<Hero> getLiving() {
        List<Hero> livingHeroes = new ArrayList<>();
        for(Hero hero : heroes)
            if(hero.isAlive())
                livingHeroes.add(hero);
        return livingHeroes;
    }

    public int getHighestLevel() {
        int highestLevel = 1;
        for(Hero hero : heroes)
            highestLevel = Math.max(highestLevel, hero.getLevel());
        return highestLevel;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void position(int newRow, int newCol) {
        row = newRow;
        col = newCol;
    }
}

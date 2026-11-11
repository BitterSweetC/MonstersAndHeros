package legends.entity;

import java.util.*;

public final class Battle {
    private final Party party;
    private final List<Monster> monsters;
    private final SessionStatistics statistics;
    private final long number;
    private int nextHero;
    private int round = 1;
    private boolean settled;

    public Battle(Party party, List<Monster> monsters) {
        this(party, monsters, new SessionStatistics());
    }

    public Battle(Party party, List<Monster> monsters, SessionStatistics statistics) {
        this.party = party;
        this.monsters = Collections.unmodifiableList(new ArrayList<>(monsters));
        this.statistics = statistics;
        number = statistics.startBattle(party, monsters);
    }

    public SessionStatistics getStatistics() {
        return statistics;
    }

    public long getNumber() {
        return number;
    }

    public List<Monster> getMonsters() {
        return monsters;
    }

    public List<Monster> getLiving() {
        List<Monster> livingMonsters = new ArrayList<Monster>();
        for(Monster monster : monsters)
            if(monster.isAlive())
                livingMonsters.add(monster);
        return livingMonsters;
    }

    public boolean isOver() {
        return party.getLiving().isEmpty() || getLiving().isEmpty();
    }

    public boolean isVictory() {
        return !party.getLiving().isEmpty() && getLiving().isEmpty();
    }

    public Hero getCurrentHero() {
        if(isOver())
            return null;
        // skip dead heroes
        while(nextHero < party.getHeroes().size() && !party.getHeroes().get(nextHero).isAlive())
            nextHero++;
        return nextHero < party.getHeroes().size() ? party.getHeroes().get(nextHero) : null;
    }

    public void acted() {
        nextHero++;
    }

    public int getRound() {
        return round;
    }

    public void nextRound() {
        nextHero = 0;
        round++;
    }

    public boolean isSettled() {
        return settled;
    }

    public void markSettled() {
        settled = true;
    }

    public Party getParty() {
        return party;
    }
}

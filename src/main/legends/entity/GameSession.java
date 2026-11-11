package legends.entity;

import java.util.*;

import legends.config.GameConfig;
import legends.factory.WorldGenerator;
import legends.service.MarketService;
import legends.service.impl.MarketServiceImpl;

/** Everything that makes up one game session  */
public final class GameSession {
    private final GameConfig config;
    private final Random random;
    private final MarketService markets;
    private final SessionStatistics statistics = new SessionStatistics();
    private Party party;
    private World world;
    private Battle battle;
    private int victories;

    public GameSession(GameConfig config, long seed) {
        this.config = config;
        random = new Random(seed);
        markets = new MarketServiceImpl(config, random, statistics);
    }

    public void start(List<Hero> heroes) {
        party = new Party(heroes);
        for(Hero hero : heroes) {
            statistics.register(hero);
            markets.starter(hero);
        }
        world = new WorldGenerator().generate(random, party, markets);
    }

    public void victory() {
        victories++;
        if(victories % 5 == 0)
            for(Market market : world.getMarkets())
                markets.restock(market, party.getHighestLevel());
    }

    public GameConfig getConfig() {
        return config;
    }

    public SessionStatistics getStatistics() {
        return statistics;
    }

    public Random getRandom() {
        return random;
    }

    public MarketService getMarkets() {
        return markets;
    }

    public Party getParty() {
        return party;
    }

    public World getWorld() {
        return world;
    }

    public Battle getBattle() {
        return battle;
    }

    public int getVictories() {
        return victories;
    }

    public void setBattle(Battle battle) {
        this.battle = battle;
    }
}

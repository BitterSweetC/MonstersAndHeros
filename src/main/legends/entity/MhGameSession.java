package legends.entity;

import java.util.*;

import legends.config.GameConfig;
import legends.factory.MhWorldGenerator;
import legends.service.MarketService;
import legends.service.impl.MarketServiceImpl;

/** Everything that makes up one game session  */
public final class MhGameSession {
    private final GameConfig config;
    private final Random random;
    private final MarketService markets;
    private final SessionStatistics statistics = new SessionStatistics();
    private Party party;
    private MhWorld mhWorld;
    private Battle battle;
    private int victories;

    public MhGameSession(GameConfig config, long seed) {
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
        mhWorld = new MhWorldGenerator().generate(random, party, markets);
    }

    public void victory() {
        victories++;
        if(victories % 5 == 0)
            for(Market market : mhWorld.getMarkets())
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

    public MhWorld getWorld() {
        return mhWorld;
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

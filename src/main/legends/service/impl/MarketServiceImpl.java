package legends.service.impl;

import java.math.*;
import java.util.*;

import legends.config.GameConfig;
import legends.entity.Hero;
import legends.entity.ItemInstance;
import legends.entity.Market;
import legends.entity.SessionStatistics;
import legends.entity.template.ItemTto;
import legends.service.MarketService;

// Handles all market transactions - buying, selling, repairs, restocking
public final class MarketServiceImpl implements MarketService {
    private final GameConfig config;
    private final Random random;
    private final SessionStatistics statistics;
    private long nextId = 1;

    public MarketServiceImpl(GameConfig config, Random random) {
        this(config, random, new SessionStatistics());
    }

    public MarketServiceImpl(GameConfig config, Random random, SessionStatistics statistics) {
        this.config = config;
        this.random = random;
        this.statistics = statistics;
    }

    @Override
    public ItemInstance create(ItemTto definition, int level, boolean starter) {
        return new ItemInstance(nextId++, definition, level, starter);
    }

    @Override
    public ItemInstance starter(Hero hero) {
        String id = config.getEquipmentRuleTto(hero.getHeroClass()).getStarterWeapon();
        ItemInstance item = create(config.getWeapon(id), 1, true);
        hero.getInventory().add(item);
        hero.getEquipment().equip(hero, item, item.getWeapon().getHands() == 2 ? "BOTH" : "SINGLE");
        return item;
    }

    @Override
    public Market generate() {
        Market market = new Market();
        int count = 6 + random.nextInt(5);
        for(int index = 0; index < count; index++)
            market.add(randomItem(0));
        boolean potion = false;
        for(ItemInstance item : market.getStock())
            potion |= item.getCategory().equals("potions");
        if(!potion)
            market.replace(market.getStock().get(0),
                    create(config.getPotions().get(random.nextInt(config.getPotions().size())), 1, false));
        // always stock hp and mp potions so players have something useful to buy
        ensurePotion(market, "health", 0);
        ensurePotion(market, "mana", 1);
        return market;
    }

    private void ensurePotion(Market market, String id, int replacementIndex) {
        for(ItemInstance item : market.getStock())
            if(item.getCategory().equals("potions") && item.getDefinition().getId().equals(id))
                return;
        ItemInstance previous = market.getStock().get(replacementIndex);
        if(id.equals("mana") && previous.getCategory().equals("potions") && previous.getDefinition().getId().equals("health"))
            previous = market.getStock().get(2);
        market.replace(previous, create(config.getPotion(id), 1, false));
    }

    @Override
    public String getSignature(Market market) {
        List<String> entries = new ArrayList<String>();
        for(ItemInstance item : market.getStock())
            entries.add(item.getCategory() + ":" + item.getDefinition().getId() + ":" + item.getLevel());
        Collections.sort(entries);
        return entries.toString();
    }

    @Override
    public void distinguish(Market market, Set<String> signatures) {
        int tries = 0;
        while(signatures.contains(getSignature(market)) && tries++ < 100) {
            Market other = generate();
            market.replaceStock(other.getStock());
        }

        if(signatures.contains(getSignature(market)))
            throw new IllegalStateException("Could not generate distinct market");
        signatures.add(getSignature(market));
    }

    private ItemInstance randomItem(int highestLevel) {
        if(highestLevel == 0) {
            String category = new String[] { "weapons", "armor", "potions", "spells" }[random.nextInt(4)];
            List<? extends ItemTto> definitions = config.getItems(category);
            return create(definitions.get(random.nextInt(definitions.size())), 1, false);
        }
        double roll = random.nextDouble();
        int level;
        if(roll < 0.55) {
            int low = Math.max(1, highestLevel - 2);
            level = low + random.nextInt(highestLevel - low + 1);
        }
        else {
            double plusOneThreshold = 0.55 + 0.30;
            int levelIncrease = roll < plusOneThreshold ? 1 : 2;
            level = highestLevel + levelIncrease;
        }
        String category = new String[] { "weapons", "armor", "potions", "spells" }[random.nextInt(4)];
        List<? extends ItemTto> definitions = config.getItems(category);
        return create(definitions.get(random.nextInt(definitions.size())), level, false);
    }

    @Override
    public void restock(Market market, int highestLevel) {
        int cap = 18;
        if(market.getStock().size() > cap)
            return;
        List<ItemInstance> obsolete = new ArrayList<>();
        for(ItemInstance item : market.getStock())
            if(!market.isPlayerSold(item) && item.getLevel() < highestLevel - 2)
                obsolete.add(item);
        Collections.sort(obsolete, Comparator.comparingInt(item -> item.getLevel()));
        for(int index = 0; index < 2; index++) {
            if(market.getStock().size() == cap && obsolete.isEmpty())
                break;
            ItemInstance fresh;
            if(index == 0 || random.nextDouble() < 0.45)
                fresh = create(config.getPotion(index == 0 ? "health" : "mana"), highestLevel, false);
            else
                fresh = randomItem(highestLevel);
            if(market.getStock().size() < cap)
                market.add(fresh);
            else
                market.replace(obsolete.remove(0), fresh);
        }
    }

    // BigDecimal to avoid float rounding issues with gold
    private long money(long price, double ratio, int numerator, int denominator, RoundingMode mode) {
        return BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(ratio)).multiply(BigDecimal.valueOf(numerator))
                .divide(BigDecimal.valueOf(denominator), 0, mode).longValue();
    }

    @Override
    public long getBuyPrice(ItemInstance item) {
        if(!item.isDurable())
            return item.getPrice();
        long price;
        if(item.getDurability() == 0)
            price = money(item.getPrice(), 0.10, 1, 1, RoundingMode.CEILING);
        else
            price = money(item.getPrice(), 1, item.getDurability(), item.getMaxDurability(), RoundingMode.FLOOR);
        return Math.max(1, price);
    }

    @Override
    public long getSellPrice(ItemInstance item) {
        if(!item.isDurable())
            return money(item.getPrice(), 0.50, 1, 1, RoundingMode.FLOOR);
        if(item.getDurability() == 0)
            return money(item.getPrice(), 0.05, 1, 1, RoundingMode.FLOOR);
        return money(item.getPrice(), 0.50, item.getDurability(), item.getMaxDurability(),
                RoundingMode.FLOOR);
    }

    @Override
    public long getRepairPrice(ItemInstance item) {
        int missingDurability = item.getMaxDurability() - item.getDurability();
        return money(item.getPrice(), 0.50, missingDurability,
                item.getMaxDurability(), RoundingMode.CEILING);
    }

    @Override
    public void buy(Market market, Hero hero, ItemInstance item) {
        if(item.getLevel() > hero.getLevel())
            throw new IllegalArgumentException(item.getName() + " requires hero level " + item.getLevel() + "; current level: " + hero.getLevel() + ".");
        long price = getBuyPrice(item);
        hero.pay(price);
        market.remove(item);
        hero.getInventory().add(item);
        statistics.bought(hero, item, price);
    }

    @Override
    public void sell(Market market, Hero hero, ItemInstance item) {
        if(hero.getEquipment().isEquipped(item))
            throw new IllegalArgumentException(item.getName() + " is equipped. Leave the market with 0, use E to unequip it, then return to sell it.");
        long price = getSellPrice(item);
        hero.credit(price);
        hero.getInventory().remove(item);
        market.acceptSale(item);
        statistics.sold(hero, item, price);
    }

    @Override
    public void repair(Hero hero, ItemInstance item) {
        long price = getRepairPrice(item);
        hero.pay(price);
        item.repair();
        statistics.repaired(hero, item, price);
    }
}

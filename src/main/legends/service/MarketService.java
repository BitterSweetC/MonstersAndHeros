package legends.service;

import java.util.Set;

import legends.entity.Hero;
import legends.entity.ItemInstance;
import legends.entity.Market;
import legends.entity.template.ItemTto;

//Defines item creation, market stock, pricing, and trading operations.
public interface MarketService {
    /**
     * Creates an independent item instance. Spells use their fixed required level and price; other items
     * use the requested level, with starter pricing and durability when flagged.
     */
    ItemInstance create(ItemTto definition, int level, boolean starter);

    /**
     * Creates the hero class's starter weapon, adds it to the inventory, equips it, and returns it.
     */
    ItemInstance starter(Hero hero);

    /**
     * Creates a market with random initial stock, including health and mana potions.
     */
    Market generate();

    /**
     * Returns a stock comparison key based on item categories, definition IDs, and levels, regardless of
     * stock order.
     */
    String getSignature(Market market);

    /**
     * Regenerates duplicate stock until its signature differs from known markets, then records the
     * resulting signature.
     */
    void distinguish(Market market, Set<String> signatures);

    /**
     * Adds stock scaled to the highest hero level, respecting the stock cap and protecting player-sold
     * items.
     */
    void restock(Market market, int highestLevel);

    /**
     * Returns the gold needed to buy an item, accounting for equipment durability and broken-item pricing.
     */
    long getBuyPrice(ItemInstance item);

    /**
     * Returns the gold paid for selling an item, accounting for resale ratios and equipment durability.
     */
    long getSellPrice(ItemInstance item);

    /**
     * Returns the gold needed to restore equipment to full durability, based on its missing durability.
     */
    long getRepairPrice(ItemInstance item);

    /**
     * Buys the selected stock item after checking hero level and gold, transferring it to the inventory.
     */
    void buy(Market market, Hero hero, ItemInstance item);

    /**
     * Transfers an owned, unequipped item to the market and credits the hero with its resale value.
     */
    void sell(Market market, Hero hero, ItemInstance item);

    /**
     * Charges the hero the repair price and restores an owned, damaged piece of equipment to full
     * durability.
     */
    void repair(Hero hero, ItemInstance item);
}

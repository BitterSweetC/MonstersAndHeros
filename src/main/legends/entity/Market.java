package legends.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// One market's stock. Player-sold items are tracked separately.
public final class Market {
    private final List<ItemInstance> stock = new ArrayList<>();
    private final Set<Long> playerSales = new HashSet<>();

    public boolean isPlayerSold(ItemInstance item) {
        return playerSales.contains(item.getId());
    }

    public void acceptSale(ItemInstance item) {
        add(item);
        playerSales.add(item.getId());
    }

    public List<ItemInstance> getStock() {
        return Collections.unmodifiableList(stock);
    }

    public void add(ItemInstance item) {
        stock.add(item);
    }

    public void remove(ItemInstance item) {
        stock.remove(item);
        playerSales.remove(item.getId());
    }

    public void replace(ItemInstance current, ItemInstance replacement) {
        int index = stock.indexOf(current);
        stock.set(index, replacement);
    }

    public void replaceStock(List<ItemInstance> items) {
        List<ItemInstance> replacement = new ArrayList<>(items);
        stock.clear();
        stock.addAll(replacement);
        Set<Long> retained = new HashSet<>();
        for(ItemInstance item : stock)
            retained.add(item.getId());

        playerSales.retainAll(retained);
    }
}

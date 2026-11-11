package legends.entity;

import java.util.*;

public final class Inventory {
    private final List<ItemInstance> items = new ArrayList<ItemInstance>();

    public List<ItemInstance> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean contains(ItemInstance item) {
        return items.contains(item);
    }

    public void add(ItemInstance item) {
        items.add(item);
    }

    public void remove(ItemInstance item) {
        items.remove(item);
    }
}

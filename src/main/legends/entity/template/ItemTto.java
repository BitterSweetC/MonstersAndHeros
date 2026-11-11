package legends.entity.template;

public abstract class ItemTto {
    private final String id;
    private final String name;
    private final long basePrice;

    protected ItemTto(String id, String name, long basePrice) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getBasePrice() {
        return basePrice;
    }

    public abstract String getCategory();

    public String toString() {
        return name;
    }
}

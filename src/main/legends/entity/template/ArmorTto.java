package legends.entity.template;

public final class ArmorTto extends ItemTto {
    private final double defense;
    private final double defenseGrowth;
    private final String type;

    public ArmorTto(String id, String name, long basePrice, double defense, double defenseGrowth, String type) {
        super(id, name, basePrice);
        this.defense = defense;
        this.defenseGrowth = defenseGrowth;
        this.type = type;
    }

    public double getDefense() {
        return defense;
    }

    public double getDefenseGrowth() {
        return defenseGrowth;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return "armor";
    }
}

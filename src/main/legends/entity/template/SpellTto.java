package legends.entity.template;

// Defines one named market spell's required level, element, costs and damage.
public final class SpellTto extends ItemTto {
    private final int requiredLevel;
    private final String type;
    private final double baseDamage;
    private final double baseMp;

    public SpellTto(String id, String name, long basePrice, int requiredLevel, String type, double baseDamage,
            double baseMp) {
        super(id, name, basePrice);
        this.requiredLevel = requiredLevel;
        this.type = type;
        this.baseDamage = baseDamage;
        this.baseMp = baseMp;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public String getType() {
        return type;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public double getBaseMp() {
        return baseMp;
    }

    public String getCategory() {
        return "spells";
    }
}

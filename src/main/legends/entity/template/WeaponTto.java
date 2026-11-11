package legends.entity.template;

public final class WeaponTto extends ItemTto {
    private final double baseDamage;
    private final int hands;
    private final double damageGrowth;
    private final double magicBonus;
    private final double magicGrowth;

    public WeaponTto(String id, String name, long basePrice, double baseDamage, int hands, double damageGrowth,
            double magicBonus, double magicGrowth) {
        super(id, name, basePrice);
        this.baseDamage = baseDamage;
        this.hands = hands;
        this.damageGrowth = damageGrowth;
        this.magicBonus = magicBonus;
        this.magicGrowth = magicGrowth;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public int getHands() {
        return hands;
    }

    public double getDamageGrowth() {
        return damageGrowth;
    }

    public double getMagicBonus() {
        return magicBonus;
    }

    public double getMagicGrowth() {
        return magicGrowth;
    }

    public String getCategory() {
        return "weapons";
    }
}

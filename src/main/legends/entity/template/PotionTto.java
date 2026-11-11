package legends.entity.template;

// Potion data
public final class PotionTto extends ItemTto {
    private final String effectType;
    private final double baseAmount;
    private final double amountGrowth;
    private final double maxRatio;

    public PotionTto(String id, String name, long basePrice, String effectType, double baseAmount, double amountGrowth, double maxRatio) {
        super(id, name, basePrice);
        this.effectType = effectType;
        this.baseAmount = baseAmount;
        this.amountGrowth = amountGrowth;
        this.maxRatio = maxRatio;
    }

    public String getEffectType() {
        return effectType;
    }

    public double getBaseAmount() {
        return baseAmount;
    }

    public double getAmountGrowth() {
        return amountGrowth;
    }

    public double getMaxRatio() {
        return maxRatio;
    }

    public String getCategory() {
        return "potions";
    }
}

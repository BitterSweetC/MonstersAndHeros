package legends.entity;

import legends.entity.template.ArmorTto;
import legends.entity.template.ItemTto;
import legends.entity.template.PotionTto;
import legends.entity.template.SpellTto;
import legends.entity.template.WeaponTto;

// Wraps an item definition with instance-specific state
public final class ItemInstance {
    private final long id;
    private final ItemTto definition;
    private final String category;
    private final int level, maxDurability;
    private final long price;
    private int durability;
    private boolean used;

    public ItemInstance(long id, ItemTto definition, int level, boolean starter) {
        this.id = id;
        this.category = definition.getCategory();
        this.definition = definition;
        this.level = definition instanceof SpellTto ? getSpell().getRequiredLevel() : level;
        if(category.equals("spells"))
            price = definition.getBasePrice();
        else if(starter)
            price = 80L;
        else {
            double priceMultiplier = 1 + 0.55 * (level - 1);
            price = (long) Math.ceil(definition.getBasePrice() * priceMultiplier);
        }

        if(category.equals("weapons")) {
            if(starter)
                maxDurability = 35;
            else {
                int extraDurability = 2 * (level - 1);
                maxDurability = 30 + extraDurability;
            }
        }else if(category.equals("armor")) {
            int extraDurability = 3 * (level - 1);
            maxDurability = 40 + extraDurability;
        }else
            maxDurability = 0;
        durability = maxDurability;
    }

    public String getName() {
        return definition.getName();
    }

    public int getDurability() {
        return durability;
    }

    public boolean isConsumed() {
        return used;
    }

    public boolean isDurable() {
        return maxDurability > 0;
    }

    public boolean isUsable() {
        return !used && (!isDurable() || durability > 0);
    }

    public void wear() {
        if(isDurable() && durability > 0)
            durability--;
    }

    public void repair() {
        durability = maxDurability;
    }

    public void consume() {
        used = true;
    }

    public WeaponTto getWeapon() {
        return (WeaponTto) definition;
    }

    public ArmorTto getArmor() {
        return (ArmorTto) definition;
    }

    public PotionTto getPotion() {
        return (PotionTto) definition;
    }

    public SpellTto getSpell() {
        return (SpellTto) definition;
    }

    public double getDamage() {
        if(definition instanceof WeaponTto)
            return getWeapon().getBaseDamage() + (level - 1) * getWeapon().getDamageGrowth();
        return getSpell().getBaseDamage();
    }

    public double getMagicBonus() {
        return getWeapon().getMagicBonus() + (level - 1) * getWeapon().getMagicGrowth();
    }

    public double getDefense() {
        return getArmor().getDefense() + (level - 1) * getArmor().getDefenseGrowth();
    }

    public double getPotionAmount() {
        return getPotion().getBaseAmount() + (level - 1) * getPotion().getAmountGrowth();
    }

    public double getManaCost() {
        return getSpell().getBaseMp();
    }

    public String getDetails() {
        if(category.equals("weapons"))
            return "damage " + (long) Math.ceil(getDamage()) + ", magic "
                + (long) Math.ceil(getMagicBonus()) + ", hands " + getWeapon().getHands();
        
        if(category.equals("armor"))
            return getArmor().getType() + " armor, defense " + (long) Math.ceil(getDefense());
        
        if(category.equals("spells"))
            return "single-use spell, base damage " + (long) Math.ceil(getDamage()) + ", MP cost "
                    + (long) Math.ceil(getManaCost()) + ", " + getSpell().getType();
        
        String effect = getPotion().getEffectType();
        boolean resource = effect.equals("HEALTH") || effect.equals("MANA");
        
        if(resource)
            return "Restores " + (long) Math.ceil(getPotionAmount()) + (effect.equals("HEALTH") ? " HP" : " MP") + " (single use)";
        return effect + " +" + Math.round(100 * Math.min(getPotion().getMaxRatio(), getPotionAmount()))
                + "% until battle ends (does not stack)";
    }

    public String toString() {
        return "#" + id + " " + getName() + " Lv" + level + " price " + price
                + (isDurable() ? " durability " + durability + "/" + maxDurability : "") + " | " + getDetails();
    }

    public long getId() {
        return id;
    }

    public ItemTto getDefinition() {
        return definition;
    }

    public String getCategory() {
        return category;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public long getPrice() {
        return price;
    }
}

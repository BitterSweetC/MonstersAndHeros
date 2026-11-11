package legends.entity;

import legends.entity.template.EquipmentRuleTto;
import java.util.Objects;

/**
 * Manages the hero's weapon and armor slots.
 * Handles dual wield, two-handed grip, and broken weapon fallback.
 */
public final class Equipment {
    private ItemInstance main, off, armor;
    private boolean bothHands;

    public ItemInstance getMain() {
        normalize();
        return main;
    }

    public ItemInstance getOff() {
        normalize();
        return off;
    }

    public ItemInstance getArmor() {
        return armor != null && armor.isUsable() ? armor : null;
    }

    public boolean isEquipped(ItemInstance item) {
        return main == item || off == item || armor == item;
    }

    public boolean canEquip(Hero hero, ItemInstance item) {
        EquipmentRuleTto rules = hero.getConfig().getEquipmentRuleTto(hero.getHeroClass());
        if(item.getCategory().equals("armor"))
            return rules.getArmor().contains(item.getArmor().getType());
        
        if(item.getCategory().equals("weapons"))
            return rules.getWeapons().contains(item.getDefinition().getId());
        
        return true;
    }

    public void validate(Hero hero, ItemInstance item, String grip) {
        if(!item.isUsable())
            throw new IllegalArgumentException(item.getName() + " is broken or already used. Repair broken equipment before equipping.");
        if(item.getLevel() > hero.getLevel())
            throw new IllegalArgumentException(item.getName() + " requires hero level " + item.getLevel() + "; current level: " + hero.getLevel() + ".");
        
        if(!grip.equals("SINGLE") && !grip.equals("BOTH") && !grip.equals("OFF"))
            throw new IllegalArgumentException("Unknown grip. Use SINGLE for the main hand, BOTH for both hands, or OFF for the off hand.");
        
        EquipmentRuleTto rules = hero.getConfig().getEquipmentRuleTto(hero.getHeroClass());
        if(item.getCategory().equals("armor")) {
            if(!rules.getArmor().contains(item.getArmor().getType()))
                throw new IllegalArgumentException(hero.getHeroClass() + " cannot equip " + item.getName() + " (" + item.getArmor().getType() + " armor).");
            return;
        }
        
        if(!rules.getWeapons().contains(item.getDefinition().getId()))
            throw new IllegalArgumentException(hero.getHeroClass() + " cannot equip the weapon " + item.getName() + ".");
        
        if(grip.equals("OFF") && (!rules.canDualWield() || item.getWeapon().getHands() != 1
                || getMain() == null || main.getWeapon().getHands() != 1 || main == item || bothHands))
            throw new IllegalArgumentException("OFF requires a class that can dual wield and two different one-handed weapons. Equip the main weapon in SINGLE grip first.");
        
        // two-handed weapons can't be held in one hand
        if(grip.equals("SINGLE") && item.getWeapon().getHands() == 2)
            throw new IllegalArgumentException(item.getName() + " requires both hands. Enter BOTH.");
    }

    public void equip(Hero hero, ItemInstance item, String grip) {
        validate(hero, item, grip);
        if(item.getCategory().equals("armor"))
            armor = item;
        else if(grip.equals("OFF"))
            off = item;
        else {
            main = item;
            off = null;
            bothHands = grip.equals("BOTH");
        }
    }

    public void unequip(ItemInstance item) {
        if(main == item)
            main = null;
        if(off == item)
            off = null;
        if(armor == item)
            armor = null;
        normalize();
    }

    private void normalize() {
        if(main != null && !main.isUsable())
            main = null;
        if(off != null && !off.isUsable())
            off = null;
        if(main == null && off != null) {
            main = off;
            off = null;
            bothHands = false;
        }
        if(main == null)
            bothHands = false;
    }

    public double getWeapon() {
        normalize();
        if(main == null)
            return 0;
        
        double power = main.getDamage();
        if(off != null)
            power += off.getDamage() * 0.60;
        else if(bothHands && main.getWeapon().getHands() == 1)
            power *= 1.20;
        return power;
    }

    public double getMagic() {
        normalize();
        if(main == null)
            return 0;
        double value = main.getMagicBonus();
        if(off != null)
            value += off.getMagicBonus() * 0.60;
        else if(bothHands && main.getWeapon().getHands() == 1)
            value *= 1.20;
        return value;
    }

    public void wearWeapons() {
        normalize();
        if(main != null)
            main.wear();
        if(off != null)
            off.wear();
    }

    public String toString() {
        return "Main hand: " + Objects.toString(getMain(), "none") + "; off hand: " + Objects.toString(getOff(), "none")
                + "; grip: " + (bothHands ? "BOTH" : "SINGLE") + "; armor: " + Objects.toString(getArmor(), "none");
    }
}

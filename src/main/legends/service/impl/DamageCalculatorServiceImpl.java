package legends.service.impl;

import legends.entity.Combatant;
import legends.entity.Hero;
import legends.entity.ItemInstance;
import legends.entity.Monster;
import legends.service.DamageCalculatorService;

// Pure calculation
public final class DamageCalculatorServiceImpl implements DamageCalculatorService {
    @Override
    public double getPhysical(Hero hero) {
        return (hero.getStat("STR") + hero.getEquipment().getWeapon()) * 0.5;
    }

    @Override
    public double getMagic(Hero hero) {
        return (hero.getStat("DEX") + hero.getEquipment().getMagic()) * 0.75;
    }

    @Override
    public double getSpell(Hero hero, ItemInstance spell) {
        return spell.getDamage() * (1 + hero.getStat("DEX") / 3000.0);
    }

    @Override
    public long getDamage(double rawDamage, double defense, double reductionMultiplier) {
        // damage: raw * 200/(200+def), minimum 1
        double defenseScale = 200.0;
        double scaledDamage = rawDamage * defenseScale;
        double damageAfterDefense = scaledDamage / (defenseScale + defense);
        double finalDamage = damageAfterDefense * reductionMultiplier;
        return Math.max(1, (long) Math.ceil(finalDamage));
    }

    @Override
    public double getDodge(Combatant target, double bonus) {
        double dodgeChance;
        if(target instanceof Hero) {
            Hero hero = (Hero) target;
            double agility = hero.getStat("AGI");
            // Approach 45% gradually so additional agility keeps improving dodge.
            dodgeChance = 0.08 + 0.37 * agility / (agility + 1000.0);
        }else {
            Monster monster = (Monster) target;
            double attributeBonus = monster.getDodgeAttribute() / 1000.0;
            dodgeChance = 0.04 + attributeBonus + bonus;
            dodgeChance = Math.min(0.35, dodgeChance);
            dodgeChance *= 1 - monster.getStrongest("LIGHTNING");
        }
        return dodgeChance;
    }

    @Override
    public double getCritical(Hero hero) {
        double luckBonus = hero.getStat("LUCK") / 1000.0;
        double criticalChance = 0.03 + luckBonus;
        return Math.min(0.35, criticalChance);
    }
}

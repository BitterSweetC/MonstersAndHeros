package legends.service;

import legends.entity.Combatant;
import legends.entity.Hero;
import legends.entity.ItemInstance;

//Defines attack power, damage mitigation, dodge, and critical calculations.
public interface DamageCalculatorService {
    /**
     * Returns raw physical attack power by scaling the sum of strength and equipped weapon damage.
     */
    double getPhysical(Hero hero);

    /**
     * Returns raw class-magic power by scaling the sum of dexterity and equipped weapon magic bonuses.
     */
    double getMagic(Hero hero);

    /**
     * Returns raw market-spell damage scaled by hero dexterity; consumes no resources.
     */
    double getSpell(Hero hero, ItemInstance spell);

    /**
     * Applies defense and the supplied remaining-damage multiplier, then rounds up to at least one point
     * of damage.
     */
    long getDamage(double rawDamage, double defense, double reductionMultiplier);

    /**
     * Returns dodge probability: heroes gain diminishing benefits from agility toward 45%; monsters
     * use their dodge attribute, applicable bonus, cap, and lightning debuff.
     */
    double getDodge(Combatant target, double bonus);

    /**
     * Returns the hero's critical-hit probability from luck, limited by the cap.
     */
    double getCritical(Hero hero);
}

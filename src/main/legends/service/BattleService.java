package legends.service;

import java.util.List;

import legends.entity.Battle;
import legends.entity.Combatant;
import legends.entity.Hero;
import legends.entity.ItemInstance;
import legends.entity.Monster;
import legends.entity.Skill;

//Defines hero actions, round completion, and battle settlement operations.
public interface BattleService {
    /**
     * Performs a normal attack, wears the equipped weapons, and spends one hero action; returns the combat
     * log.
     */////////sssss
    List<String> attack(Battle battle, Hero hero, Monster monster);

    /**
     * Casts a market spell and returns its name, target, MP cost, and combat result. A valid cast spends
     * one action, the spell item, and MP even if dodged. A surviving target hit by ice/fire/lightning loses 10% damage/defense/dodge
     * chance for 3 rounds including the casting round; reapplying the same element refreshes the
     * duration without stacking its strength.
     */
    List<String> cast(Battle battle, Hero hero, ItemInstance spell, Monster monster);

    /**
     * Applies an owned potion, consumes it, and spends one hero action; returns its name and actual
     * resource or attribute change, including the values before and after use.
     */
    List<String> potion(Battle battle, Hero hero, ItemInstance item);

    /**
     * Equips an owned item using the requested grip and spends one hero action; returns an equipment
     * message.
     */
    List<String> equip(Battle battle, Hero hero, ItemInstance item, String grip);

    /**
     * Removes an owned item from its equipment slot and spends one hero action; returns an equipment
     * message.
     */
    List<String> unequip(Battle battle, Hero hero, ItemInstance item);

    /**
     * Uses an equipped class skill on valid targets, spending MP and an action and starting its cooldown;
     * returns the combat log.
     */
    List<String> skill(Battle battle, Hero hero, Skill skill, List<Combatant> targets);

    /**
     * Runs monster turns and end-of-round effects and recovery after all heroes act; returns attack
     * results with HP changes and actual HP/MP recovery messages.
     */
    List<String> finishRound(Battle battle);

    /**
     * Settles a finished battle once. Returns true only for a newly settled victory, after rewarding
     * survivors and reviving fainted heroes.
     */
    boolean settle(Battle battle);
}

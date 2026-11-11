package legends.service.impl;

import java.util.*;

import legends.entity.Battle;
import legends.entity.Combatant;
import legends.entity.Hero;
import legends.entity.ItemInstance;
import legends.entity.Monster;
import legends.entity.Skill;
import legends.entity.StatusEffect;
import legends.enums.SkillEffectType;
import legends.enums.SkillTargetMode;
import legends.service.BattleService;
import legends.service.DamageCalculatorService;

// Hero actions, monster turns, end-of-round processing, victory rewards.
public final class BattleServiceImpl implements BattleService {
    private final Random random;
    private final DamageCalculatorService calculator;

    public BattleServiceImpl(Random random) {
        this.random = random;
        calculator = new DamageCalculatorServiceImpl();
    }

    private boolean hit(Battle battle, Combatant source, Combatant target, double rawDamage, boolean physical, List<String> log) {
        String side = source instanceof Hero ? "Hero attack: " : "Enemy attack: ";
        // spirit's passive gives them a bonus dodge chance on top of the normal one
        double dodgeBonus = 0;
        if(target instanceof Monster && ((Monster) target).getType().equals("SPIRIT") && random.nextDouble() < 0.15)
            dodgeBonus = 0.10;
        
        if(random.nextDouble() < calculator.getDodge(target, dodgeBonus)) {
            log.add(side + source.getName() + " -> " + target.getName() + ": dodged");
            return false;
        }
        
        boolean critical = source instanceof Hero && random.nextDouble() < calculator.getCritical((Hero) source);
        if(critical)
            rawDamage *= 1.5;
        if(source instanceof Monster)
            rawDamage = applyAttackPassive((Monster) source, rawDamage);

        double reductionMultiplier = damageReduction(target, physical);
        long beforeHp = target.getHp();
        long actualDamage = target.hurt(calculator.getDamage(rawDamage, target.getDefense(), reductionMultiplier));
        battle.getStatistics().damage(source, target, actualDamage);
        log.add(side + source.getName() + " -> " + target.getName() + ": " + actualDamage + " damage"
                + (critical ? " (critical)" : "") + " | HP " + beforeHp + " -> " + target.getHp() + "/" + target.getMaxHp()
                + (!target.isAlive() ? (target instanceof Hero ? "; fainted" : "; defeated") : ""));
        
        applyHitEffects(source, target, actualDamage, log);
        return true;
    }

    private double applyAttackPassive(Monster monster, double rawDamage) {
        if(monster.getType().equals("DRAGON") && random.nextDouble() < 0.20)
            rawDamage *= 1.30;
        if(monster.getType().equals("GOBLIN") && random.nextDouble() < 0.18)
            rawDamage *= 1.50;
        return rawDamage;
    }

    private double damageReduction(Combatant target, boolean physical) {
        double multiplier = 1;
        for(StatusEffect effect : target.getEffects())
            if(effect.getKind().equals("REDUCTION"))
                multiplier *= 1 - effect.getValue();
        if(physical && target instanceof Monster && ((Monster) target).getType().equals("GOLEM"))
            multiplier *= 1 - 0.15;
        return multiplier;
    }

    private void applyHitEffects(Combatant source, Combatant target, long actualDamage, List<String> log) {
        if(target instanceof Hero) {
            ItemInstance armor = ((Hero) target).getEquipment().getArmor();
            if(armor != null && actualDamage > 0)
                armor.wear();
        }

        if(source instanceof Monster && ((Monster) source).getType().equals("VAMPIRE")
                && random.nextDouble() < 0.25) {
            long beforeHp = source.getHp();
            source.heal((long) Math.ceil(actualDamage * 0.35));
            log.add("Life steal: " + source.getName() + " | HP +" + (source.getHp() - beforeHp)
                    + " (" + beforeHp + " -> " + source.getHp() + "/" + source.getMaxHp() + ")");
        }
    }

    @Override
    public List<String> attack(Battle battle, Hero hero, Monster monster) {
        // snapshot damage before weapon wear
        double rawDamage = calculator.getPhysical(hero);
        hero.getEquipment().wearWeapons();
        hero.commit(null);
        List<String> log = new ArrayList<>();
        hit(battle, hero, monster, rawDamage, true, log);
        battle.acted();
        battle.getStatistics().action(battle, "Attack", log);
        return log;
    }

    @Override
    public List<String> cast(Battle battle, Hero hero, ItemInstance spell, Monster monster) {
        if(spell.getLevel() > hero.getLevel())
            throw new IllegalArgumentException(spell.getName() + " requires hero level " + spell.getLevel() + "; current level: " + hero.getLevel() + ".");

        String type = spell.getSpell().getType();
        String attribute;
        switch(type) {
            case "ICE" :
                attribute = "damage";
                break;
            case "FIRE" :
                attribute = "defense";
                break;
            case "LIGHTNING" :
                attribute = "dodge chance";
                break;
            default :
                throw new IllegalArgumentException("Unknown spell type: " + type);
        }
        
        long mp = (long) Math.ceil(spell.getManaCost());
        
        double rawDamage = calculator.getSpell(hero, spell);
        hero.spendMp(mp);
        spell.consume();
        hero.getInventory().remove(spell);
        hero.commit(null);
        
        List<String> log = new ArrayList<>();
        log.add(hero.getName() + " casts " + spell.getName() + " on " + monster.getName() + " (" + mp + " MP)");
        if(hit(battle, hero, monster, rawDamage, false, log) && monster.isAlive()) {
            monster.effect("spell_" + type, type, 0.10, 3);
            log.add(monster.getName() + ": " + type + " reduces " + attribute
                    + " by 10% for 3 rounds including this round (reapplying refreshes duration; does not stack)");
        }
        battle.acted();
        battle.getStatistics().action(battle, "Spell", log);
        return log;
    }

    @Override
    public List<String> potion(Battle battle, Hero hero, ItemInstance item) {
        String kind = item.getPotion().getEffectType();
        boolean resource = kind.equals("HEALTH") || kind.equals("MANA");
        double before = kind.equals("HEALTH") ? hero.getHp() : kind.equals("MANA") ? hero.getMp() : hero.getStat(kind);
        long beforeHp = hero.getHp(), beforeMp = hero.getMp();
        hero.drink(item);
        battle.getStatistics().potion(hero, beforeHp, beforeMp);
        double after = kind.equals("HEALTH") ? hero.getHp() : kind.equals("MANA") ? hero.getMp() : hero.getStat(kind);
        hero.commit(null);
        battle.acted();
        String attribute = kind.equals("HEALTH") ? "HP" : kind.equals("MANA") ? "MP" : kind;
        String result = String.format(
                resource ? "%s +%.0f (%.0f -> %.0f)" : "%s +%.2f (%.2f -> %.2f) until battle ends",
                attribute, after - before, before, after);
        List<String> log = Collections.singletonList(hero.getName() + " used " + item.getName() + ": " + result);
        battle.getStatistics().action(battle, "Potion", log);
        return log;
    }

    @Override
    public List<String> equip(Battle battle, Hero hero, ItemInstance item, String grip) {
        hero.getEquipment().equip(hero, item, grip);
        hero.commit(null);
        battle.acted();
        List<String> log = Collections.singletonList(hero.getName() + " equipped " + item.getName() + " (" + grip + ")");
        battle.getStatistics().action(battle, "Equipment", log);
        return log;
    }

    @Override
    public List<String> unequip(Battle battle, Hero hero, ItemInstance item) {
        hero.getEquipment().unequip(item);
        hero.commit(null);
        battle.acted();
        List<String> log = Collections.singletonList(hero.getName() + " unequipped " + item.getName());
        battle.getStatistics().action(battle, "Equipment", log);
        return log;
    }

    @Override
    public List<String> skill(Battle battle, Hero hero, Skill skill, List<Combatant> targets) {
        if(!hero.isReady(skill))
            throw new IllegalArgumentException(skill.getName() + " is cooling down. Choose another action; viewing or canceling menus does not advance cooldown.");

        List<Combatant> expected = skillTargets(battle, hero, skill, targets);
        SkillEffectType kind = skill.getEffectType();
        double multiplier = skill.getMultiplier();
        int duration = skill.getDuration();
        List<String> log = new ArrayList<>();
        boolean attack = kind == SkillEffectType.PHYSICAL || kind == SkillEffectType.MAGIC;
        boolean physical = kind == SkillEffectType.PHYSICAL;
        double rawDamage = attack ? (physical ? calculator.getPhysical(hero) : calculator.getMagic(hero)) * multiplier : 0;
        hero.commit(skill);
        if(physical)
            hero.getEquipment().wearWeapons();
        log.add(hero.getName() + " uses " + skill.getName());
        
        for(Combatant target : expected) {
            if(!target.isAlive())
                continue;
            if(attack)
                hit(battle, hero, target, rawDamage, physical, log);
            else {
                target.effect(skill.getId(), kind.name(), multiplier, duration);
                battle.getStatistics().record(battle, "Skill effect",
                Collections.singletonList(hero.getName() + " -> " + target.getName() + ": "
                + skill.getName() + " " + kind + " " + multiplier
                    + " for " + duration + " rounds"));
            }
            if(battle.isOver())
                break;
        }
        
        battle.acted();
        battle.getStatistics().action(battle, "Skill", log);
        return log;
    }

    private List<Combatant> skillTargets(Battle battle, Hero hero, Skill skill, List<Combatant> targets) {
        SkillTargetMode mode = skill.getTargetMode();
        List<Combatant> expected = new ArrayList<>();

        if(mode == SkillTargetMode.SELF)
            expected.add(hero);
        else if(mode == SkillTargetMode.ALL_ENEMIES)
            expected.addAll(battle.getLiving());
        else
            expected.addAll(targets);
        return expected;
    }

    @Override
    public List<String> finishRound(Battle battle) {
        List<String> log = new ArrayList<>();
        if(battle.isOver())
            return log;
        
        // monsters pick random living hero as target
        for(Monster monster : battle.getLiving()) {
            List<Hero> living = battle.getParty().getLiving();
            if(living.isEmpty())
                break;
            Hero hero = living.get(random.nextInt(living.size()));
            hit(battle, monster, hero, monster.getDamage(), true, log);
            if(battle.isOver())
                break;
        }

        battle.getStatistics().record(battle, "Monster attack", log);
        if(battle.isOver())
            return log;
        
        List<Combatant> all = new ArrayList<>(battle.getParty().getHeroes());
        all.addAll(battle.getMonsters());
        
        List<String> recovery = new ArrayList<>();
        for(Hero hero : battle.getParty().getLiving()) {
            long beforeHp = hero.getHp(), beforeMp = hero.getMp();
            hero.heal(hero.getHpRecovery());
            hero.restoreMp(hero.getMpRecovery());
            recovery.add("Round recovery: " + hero.getName() + " | HP +" + (hero.getHp() - beforeHp)
                    + " (" + beforeHp + " -> " + hero.getHp() + "/" + hero.getMaxHp() + ") | MP +"
                    + (hero.getMp() - beforeMp) + " (" + beforeMp + " -> " + hero.getMp() + "/" + hero.getMaxMp() + ")");
        }
        battle.getStatistics().record(battle, "Round recovery", recovery);
        log.addAll(recovery);
        
        for(Combatant target : all)
            target.endEffects();
        battle.nextRound();
        return log;
    }

    @Override
    public boolean settle(Battle battle) {
        if(!battle.isOver() || battle.isSettled())
            return false;
        battle.markSettled();
        battle.getStatistics().settled(battle);
        if(!battle.isVictory())
            return false;
        
        List<Hero> survivors = battle.getParty().getLiving();
        long experienceReward = 0, gold = 0;
        
        for(Monster monster : battle.getMonsters()) {
            experienceReward += monster.getExp();
            gold += monster.getGold();
        }
        
        for(Hero hero : battle.getParty().getHeroes())
            hero.endBattle();
        
        for(Hero hero : survivors) {
            hero.credit(gold);
            hero.gainExperience(experienceReward);
            battle.getStatistics().reward(hero, gold, experienceReward);
        }
        
        for(Hero hero : battle.getParty().getHeroes())
            if(!survivors.contains(hero))
                hero.revive();
        return true;
    }

}

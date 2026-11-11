package legends.entity;

import legends.enums.SkillEffectType;
import legends.enums.SkillTargetMode;

// Immutable skill definition read from config
public final class Skill {
    private final String id;
    private final String name;
    private final String heroClass;
    private final int unlockLevel;
    private final long mpCost;
    private final long cooldown;
    private final SkillEffectType effectType;
    private final SkillTargetMode targetMode;
    private final double multiplier;
    private final int duration;

    public Skill(String id, String name, String heroClass, int unlockLevel, long mpCost, long cooldown,
         SkillEffectType effectType, SkillTargetMode targetMode, double multiplier, int duration) {
        this.id = id;
        this.name = name;
        this.heroClass = heroClass;
        this.unlockLevel = unlockLevel;
        this.mpCost = mpCost;
        this.cooldown = cooldown;
        this.effectType = effectType;
        this.targetMode = targetMode;
        this.multiplier = multiplier;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHeroClass() {
        return heroClass;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public long getMpCost() {
        return mpCost;
    }

    public long getCooldown() {
        return cooldown;
    }

    public SkillEffectType getEffectType() {
        return effectType;
    }

    public SkillTargetMode getTargetMode() {
        return targetMode;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getDuration() {
        return duration;
    }

    public String toString() {
        return name;
    }
}

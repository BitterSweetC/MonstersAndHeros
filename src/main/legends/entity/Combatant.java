package legends.entity;

import java.util.*;

/** Base class for anything that fights */
public abstract class Combatant {
    private final String name;
    protected int level;
    protected long hp, maxHp;
    private final LinkedHashMap<String, StatusEffect> effects = new LinkedHashMap<String, StatusEffect>();

    protected Combatant(String name, int level, long maxHp) {
        this.name = name;
        this.level = level;
        this.hp = maxHp;
        this.maxHp = maxHp;
    }

    public int getLevel() {
        return level;
    }

    public long getHp() {
        return hp;
    }

    public long getMaxHp() {
        return maxHp;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public long hurt(long amount) {
        long actual = Math.min(hp, amount);
        hp -= actual;
        return actual;
    }

    public void heal(long amount) {
        if(isAlive() && hp < maxHp)
            hp += Math.min(amount, maxHp - hp);
    }

    public void effect(String key, String kind, double value, int duration) {
        // keep the stronger effect if same key already exists
        StatusEffect old = effects.get(key);
        if(old == null)
            effects.put(key, new StatusEffect(key, kind, value, duration));
        else {
            if(value > old.getValue())
                old.setValue(value);
            old.setRemaining(duration);
        }
    }

    public double getStrongest(String kind) {
        double strongestValue = 0;
        for(StatusEffect effect : effects.values())
            if(effect.getKind().equals(kind))
                strongestValue = Math.max(strongestValue, effect.getValue());
        return strongestValue;
    }

    public List<StatusEffect> getEffects() {
        return Collections.unmodifiableList(new ArrayList<>(effects.values()));
    }

    public void endEffects() {
        for(StatusEffect effect : getEffects()) {
            if(effect.getRemaining() > 0) {
                effect.setRemaining(effect.getRemaining() - 1);
                if(effect.getRemaining() == 0)
                    effects.remove(effect.getKey());
            }
        }
    }

    public void clearEffects() {
        effects.clear();
        hp = Math.min(hp, maxHp);
    }

    public abstract double getDefense();

    public String toString() {
        return name + " Lv" + level + " HP " + hp + "/" + maxHp;
    }

    public String getName() {
        return name;
    }
}

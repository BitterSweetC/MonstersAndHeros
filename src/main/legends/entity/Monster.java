package legends.entity;

import legends.entity.template.MonsterTto;

// Enemy combatant
public final class Monster extends Combatant {
    private final MonsterTto definition;
    private final String type;

    public Monster(MonsterTto definition, int level) {
        super(definition.getName(), level, definition.getHp() + definition.getHpGrowth() * (level - 1L));
        this.definition = definition;
        type = definition.getType();
    }

    public double getDamage() {
        double damage = definition.getDamage() + definition.getDamageGrowth() * (level - 1L);
        return damage * (1 - getStrongest("ICE"));
    }

    public double getDodgeAttribute() {
        return definition.getDodge() + definition.getDodgeGrowth() * (level - 1L);
    }

    public double getDefense() {
        double defense = definition.getDefense() + definition.getDefenseGrowth() * (level - 1L);
        // exoskeleton passive kicks in at low hp
        if(type.equals("EXOSKELETON") && hp < maxHp * 0.40)
            defense *= 1.30;
        return defense * (1 - getStrongest("FIRE"));
    }

    public long getExp() {
        return level * definition.getExpPerLevel();
    }

    public long getGold() {
        return level * definition.getGoldPerLevel();
    }

    public MonsterTto getDefinition() {
        return definition;
    }

    public String getType() {
        return type;
    }
}

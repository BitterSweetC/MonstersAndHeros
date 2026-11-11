package legends.entity.template;

// Defines one named monster's attributes, growth, encounter weight and rewards.
public final class MonsterTto {
    private final String id;
    private final String name;
    private final String type;
    private final long hp;
    private final long damage;
    private final long defense;
    private final long dodge;
    private final long hpGrowth;
    private final long damageGrowth;
    private final long defenseGrowth;
    private final long dodgeGrowth;
    private final int weight;
    private final long expPerLevel;
    private final long goldPerLevel;

    public MonsterTto(String id, String name, String type, long hp, long damage, long defense, long dodge,
          long hpGrowth, long damageGrowth, long defenseGrowth, long dodgeGrowth, int weight, long expPerLevel, long goldPerLevel) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.hp = hp;
        this.damage = damage;
        this.defense = defense;
        this.dodge = dodge;
        this.hpGrowth = hpGrowth;
        this.damageGrowth = damageGrowth;
        this.defenseGrowth = defenseGrowth;
        this.dodgeGrowth = dodgeGrowth;
        this.weight = weight;
        this.expPerLevel = expPerLevel;
        this.goldPerLevel = goldPerLevel;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getHp() {
        return hp;
    }

    public long getDamage() {
        return damage;
    }

    public long getDefense() {
        return defense;
    }

    public long getDodge() {
        return dodge;
    }

    public long getHpGrowth() {
        return hpGrowth;
    }

    public long getDamageGrowth() {
        return damageGrowth;
    }

    public long getDefenseGrowth() {
        return defenseGrowth;
    }

    public long getDodgeGrowth() {
        return dodgeGrowth;
    }

    public int getWeight() {
        return weight;
    }

    public long getExpPerLevel() {
        return expPerLevel;
    }

    public long getGoldPerLevel() {
        return goldPerLevel;
    }
}

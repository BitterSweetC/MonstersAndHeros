package legends.entity.template;

// Hero template from heroes.txt
public final class HeroTto {
    private final String id;
    private final String name;
    private final String heroClass;
    private final long hp;
    private final long mp;
    private final long strength;
    private final long dexterity;
    private final long agility;
    private final long luck;
    private final long defense;
    private final long hpGrowth;
    private final long mpGrowth;
    private final long initialGold;
    private final long initialExp;

    public HeroTto(String id, String name, String heroClass, long hp, long mp, long strength, long dexterity,
       long agility, long luck, long defense, long hpGrowth, long mpGrowth, long initialGold, long initialExp) {
        this.id = id;
        this.name = name;
        this.heroClass = heroClass;
        this.hp = hp;
        this.mp = mp;
        this.strength = strength;
        this.dexterity = dexterity;
        this.agility = agility;
        this.luck = luck;
        this.defense = defense;
        this.hpGrowth = hpGrowth;
        this.mpGrowth = mpGrowth;
        this.initialGold = initialGold;
        this.initialExp = initialExp;
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

    public long getHp() {
        return hp;
    }

    public long getMp() {
        return mp;
    }

    public long getStrength() {
        return strength;
    }

    public long getDexterity() {
        return dexterity;
    }

    public long getAgility() {
        return agility;
    }

    public long getLuck() {
        return luck;
    }

    public long getDefense() {
        return defense;
    }

    public long getHpGrowth() {
        return hpGrowth;
    }

    public long getMpGrowth() {
        return mpGrowth;
    }

    public long getInitialGold() {
        return initialGold;
    }

    public long getInitialExp() {
        return initialExp;
    }

    public long getStat(String attribute) {
        switch(attribute) {
            case "STR" :
                return strength;
            case "DEX" :
                return dexterity;
            case "AGI" :
                return agility;
            case "LUCK" :
                return luck;
            case "DEF" :
                return defense;
            default :
                throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }
    }

    public String toString() {
        return name;
    }
}

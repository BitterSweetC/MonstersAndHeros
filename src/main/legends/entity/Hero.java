package legends.entity;

import java.util.*;

import legends.config.GameConfig;
import legends.entity.template.HeroTto;

/** The main playable character. Handles stats, leveling, inventory and combat skills. */
public final class Hero extends Combatant {
    private final GameConfig config;
    private final String heroClass;
    private final Inventory inventory = new Inventory();
    private final Equipment equipment = new Equipment();
    private final HeroTto template;
    private long mp, maxMp, gold, experience;
    private final Map<String, Long> attributes = new LinkedHashMap<String, Long>();
    private final List<Skill> slots = new ArrayList<>();
    private final Map<String, Long> availableAt = new HashMap<>();
    private long opportunities;

    public Hero(HeroTto definition, GameConfig config) {
        super(definition.getName(), 1, definition.getHp());
        template = definition;
        this.config = config;
        heroClass = definition.getHeroClass();
        mp = maxMp = definition.getMp();
        gold = definition.getInitialGold();
        experience = definition.getInitialExp();
        for(String attribute : new String[] { "STR", "DEX", "AGI", "LUCK", "DEF" })
            attributes.put(attribute, definition.getStat(attribute));
        for(Skill skill : getUnlockedSkills())
            if(skill.getUnlockLevel() == 1)
                slots.add(skill);
    }

    public long getMp() {
        return mp;
    }

    public long getMaxMp() {
        return maxMp;
    }

    public long getGold() {
        return gold;
    }

    public long getExperience() {
        return experience;
    }

    public double getStat(String name) {
        return attributes.get(name) * (1 + getStrongest("POTION_" + name));
    }

    public double getDefense() {
        ItemInstance armor = equipment.getArmor();
        double armorDefense = armor == null ? 0 : armor.getDefense();
        double totalDefense = getStat("DEF") + armorDefense;
        double defenseBuff = 1 + getStrongest("DEFENSE");
        return totalDefense * defenseBuff;
    }

    public void pay(long amount) {
        if(amount < 0 || amount > gold)
            throw new IllegalArgumentException("Not enough gold: need " + amount + ", have " + gold + ".");
        gold -= amount;
    }

    public void credit(long amount) {
        gold += amount;
    }

    public void spendMp(long amount) {
        if(amount < 0 || amount > mp)
            throw new IllegalArgumentException("Not enough MP: need " + amount + ", have " + mp + ".");
        mp -= amount;
    }

    public void restoreMp(long amount) {
        if(mp < maxMp)
            mp += Math.min(amount, maxMp - mp);
    }

    public void drink(ItemInstance item) {
        if(item.getLevel() > level)
            throw new IllegalArgumentException(item.getName() + " requires hero level " + item.getLevel() + "; current level: " + level + ".");
        
        String kind = item.getPotion().getEffectType();
        double amount = item.getPotionAmount();
        // attribute potions don't stack - reject if we already have a stronger one
        if(!kind.equals("HEALTH") && !kind.equals("MANA")) {
            amount = Math.min(item.getPotion().getMaxRatio(), amount);
            if(amount <= getStrongest("POTION_" + kind))
                throw new IllegalArgumentException("An equal or stronger " + kind + " potion boost is already active. Potion boosts do not stack.");
            effect("potion_" + kind, "POTION_" + kind, amount, 0);
        }else if(kind.equals("HEALTH"))
            hp += (long) Math.ceil(amount);
        else
            mp += (long) Math.ceil(amount);
        item.consume();
        inventory.remove(item);
    }

    public List<Skill> getUnlockedSkills() {
        List<Skill> unlockedSkills = new ArrayList<>();
        for(Skill skill : config.getSkills())
            if(skill.getHeroClass().equals(heroClass) && skill.getUnlockLevel() <= level)
                unlockedSkills.add(skill);
        return Collections.unmodifiableList(unlockedSkills);
    }

    public List<Skill> getSkillSlots() {
        return Collections.unmodifiableList(slots);
    }

    public void slot(int index, Skill skill) {
        if(slots.contains(skill))
            throw new IllegalArgumentException("Skill already equipped");
        if(index == slots.size())
            slots.add(skill);
        else
            slots.set(index, skill);
    }

    public boolean isReady(Skill skill) {
        Long readyAt = availableAt.get(skill.getId());
        return readyAt == null || opportunities >= readyAt;
    }

    public void commit(Skill skill) {
        if(skill != null) {
            spendMp(skill.getMpCost());
            availableAt.put(skill.getId(), opportunities + skill.getCooldown() + 1);
        }
        opportunities++;
    }

    public void gainExperience(long amount) {
        experience += amount;
        // can chain multiple level-ups from a single exp reward
        while(experience >= getThreshold(level + 1)) {
            level++;
            growAttributes();
            hp = maxHp = template.getHp() + template.getHpGrowth() * (level - 1L);
            mp = maxMp = template.getMp() + template.getMpGrowth() * (level - 1L);
        }
    }

    private void growAttributes() {
        // favored stats grow faster (10% vs 5%)
        List<String> favoredAttributes;
        switch(heroClass) {
            case "WARRIOR" :
                favoredAttributes = Arrays.asList("STR", "AGI");
                break;
            case "SORCERER" :
                favoredAttributes = Arrays.asList("DEX", "AGI");
                break;
            case "PALADIN" :
                favoredAttributes = Arrays.asList("STR", "DEX");
                break;
            default :
                throw new IllegalArgumentException("Unknown hero class: " + heroClass);
        }
        for(String attribute : attributes.keySet()) {
            boolean favored = favoredAttributes.contains(attribute);
            double growth = favored ? 1.10 : 1.05;
            attributes.put(attribute, Math.round(attributes.get(attribute) * growth));
        }
    }

    public long getThreshold(int targetLevel) {
        // Experience is cumulative; each level costs current level * factor.
        return 10L * targetLevel * (targetLevel - 1L) / 2;
    }

    public long getHpRecovery() {
        return (long) Math.ceil(maxHp * 0.05);
    }

    public long getMpRecovery() {
        return (long) Math.ceil(maxMp * 0.08);
    }

    public void endBattle() {
        clearEffects();
        mp = Math.min(mp, maxMp);
        availableAt.clear();
        opportunities = 0;
    }

    public void revive() {
        hp = (long) Math.ceil(maxHp * 0.50);
        mp = (long) Math.ceil(maxMp * 0.50);
    }

    public String toString() {
        return super.toString() + " " + heroClass + " MP " + mp + "/" + maxMp + " EXP " + experience + " Gold " + gold
                + " Stats " + attributes;
    }

    public GameConfig getConfig() {
        return config;
    }

    public String getHeroClass() {
        return heroClass;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Equipment getEquipment() {
        return equipment;
    }
}

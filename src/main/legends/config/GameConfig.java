package legends.config;

import java.util.*;

import legends.entity.Skill;
import legends.entity.template.ArmorTto;
import legends.entity.template.EquipmentRuleTto;
import legends.entity.template.HeroTto;
import legends.entity.template.ItemTto;
import legends.entity.template.MonsterTto;
import legends.entity.template.PotionTto;
import legends.entity.template.SpellTto;
import legends.entity.template.WeaponTto;

/** Immutable container for game data loaded from txt files. */
public final class GameConfig {
    private final List<HeroTto> heroTtos;
    private final List<MonsterTto> monsterTtos;
    private final List<WeaponTto> weapons;
    private final List<ArmorTto> armor;
    private final List<PotionTto> potions;
    private final List<SpellTto> spells;
    private final List<Skill> skills;
    private final List<EquipmentRuleTto> equipmentRuleTtos;

    GameConfig(List<HeroTto> heroTtos, List<MonsterTto> monsterTtos, List<WeaponTto> weapons,
        List<ArmorTto> armor, List<PotionTto> potions, List<SpellTto> spells, List<Skill> skills, List<EquipmentRuleTto> equipmentRuleTtos) {
        this.heroTtos = Collections.unmodifiableList(new ArrayList<>(heroTtos));
        this.monsterTtos = Collections.unmodifiableList(new ArrayList<>(monsterTtos));
        this.weapons = Collections.unmodifiableList(new ArrayList<WeaponTto>(weapons));
        this.armor = Collections.unmodifiableList(new ArrayList<ArmorTto>(armor));
        this.potions = Collections.unmodifiableList(new ArrayList<PotionTto>(potions));
        this.spells = Collections.unmodifiableList(new ArrayList<SpellTto>(spells));
        this.skills = Collections.unmodifiableList(new ArrayList<Skill>(skills));
        this.equipmentRuleTtos = Collections.unmodifiableList(new ArrayList<EquipmentRuleTto>(equipmentRuleTtos));
    }

    public List<HeroTto> getHeroTtos() {
        return heroTtos;
    }

    public HeroTto getHeroTto(String id) {
        for(HeroTto definition : heroTtos)
            if(definition.getId().equals(id))
                return definition;
        throw new IllegalArgumentException("Unknown heroes: " + id);
    }

    public List<MonsterTto> getMonsterTtos() {
        return monsterTtos;
    }

    public MonsterTto getMonsterTto(String id) {
        for(MonsterTto definition : monsterTtos)
            if(definition.getId().equals(id))
                return definition;
        throw new IllegalArgumentException("Unknown monsters: " + id);
    }

    public List<WeaponTto> getWeapons() {
        return weapons;
    }

    public WeaponTto getWeapon(String id) {
        for(WeaponTto definition : weapons)
            if(definition.getId().equals(id))
                return definition;
        throw new IllegalArgumentException("Unknown weapons: " + id);
    }

    public List<ArmorTto> getArmor() {
        return armor;
    }

    public ArmorTto getArmor(String id) {
        for(ArmorTto definition : armor)
            if(definition.getId().equals(id))
                return definition;
        throw new IllegalArgumentException("Unknown armor: " + id);
    }

    public List<PotionTto> getPotions() {
        return potions;
    }

    public PotionTto getPotion(String id) {
        for(PotionTto definition : potions)
            if(definition.getId().equals(id))
                return definition;
        throw new IllegalArgumentException("Unknown potions: " + id);
    }

    public List<SpellTto> getSpells() {
        return spells;
    }

    public SpellTto getSpell(String id) {
        for(SpellTto definition : spells)
            if(definition.getId().equals(id))
                return definition;
        throw new IllegalArgumentException("Unknown spells: " + id);
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public Skill getSkill(String id) {
        for(Skill definition : skills)
            if(definition.getId().equals(id))
                return definition;
        throw new IllegalArgumentException("Unknown skills: " + id);
    }

    public List<EquipmentRuleTto> getEquipmentRuleTtos() {
        return equipmentRuleTtos;
    }

    public EquipmentRuleTto getEquipmentRuleTto(String heroClass) {
        for(EquipmentRuleTto definition : equipmentRuleTtos)
            if(definition.getHeroClass().equals(heroClass))
                return definition;
        throw new IllegalArgumentException("Unknown equipment-rules: " + heroClass);
    }

    public List<? extends ItemTto> getItems(String category) {
        switch(category) {
            case "weapons" :
                return weapons;
            case "armor" :
                return armor;
            case "potions" :
                return potions;
            case "spells" :
                return spells;
            default :
                throw new IllegalArgumentException("Unknown item category: " + category);
        }
    }
}

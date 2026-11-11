package legends.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import legends.entity.Skill;
import legends.entity.template.ArmorTto;
import legends.entity.template.EquipmentRuleTto;
import legends.entity.template.HeroTto;
import legends.entity.template.MonsterTto;
import legends.entity.template.PotionTto;
import legends.entity.template.SpellTto;
import legends.entity.template.WeaponTto;
import legends.enums.SkillEffectType;
import legends.enums.SkillTargetMode;

// Reads files and builds the immutable game config
public final class ConfigLoader {
    public GameConfig load(Path directory) throws IOException {
        List<HeroTto> heroTtos = readTable(directory.resolve("heroes.txt"), row ->
                new HeroTto(row.get("id"), row.get("name"), row.get("class"),
                Long.parseLong(row.get("hp")), Long.parseLong(row.get("mp")),
                Long.parseLong(row.get("str")), Long.parseLong(row.get("dex")),
                Long.parseLong(row.get("agi")), Long.parseLong(row.get("luck")),
                Long.parseLong(row.get("def")),
                Long.parseLong(row.get("hpGrowth")), Long.parseLong(row.get("mpGrowth")),
                Long.parseLong(row.get("initialGold")), Long.parseLong(row.get("initialExp"))));

        List<MonsterTto> monsterTtos = readTable(directory.resolve("monsters.txt"), row ->
                new MonsterTto(row.get("id"), row.get("name"), row.get("type"),
                Long.parseLong(row.get("hp")), Long.parseLong(row.get("damage")),
                Long.parseLong(row.get("def")), Long.parseLong(row.get("dodge")),
                Long.parseLong(row.get("hpGrowth")), Long.parseLong(row.get("damageGrowth")),
                Long.parseLong(row.get("defGrowth")), Long.parseLong(row.get("dodgeGrowth")),
                Integer.parseInt(row.get("weight")),
                Long.parseLong(row.get("expPerLevel")),
                Long.parseLong(row.get("goldPerLevel"))));

        List<WeaponTto> weapons = readTable(directory.resolve("weapons.txt"), row ->
                new WeaponTto(row.get("id"), row.get("name"), Long.parseLong(row.get("basePrice")),
                Double.parseDouble(row.get("baseDamage")),
                Integer.parseInt(row.get("hands")),
                Double.parseDouble(row.get("damageGrowth")),
                Double.parseDouble(row.getOrDefault("magicBonus", "0")),
                Double.parseDouble(row.getOrDefault("magicGrowth", "0"))));

        List<ArmorTto> armor = readTable(directory.resolve("armor.txt"), row ->
                new ArmorTto(row.get("id"), row.get("name"), Long.parseLong(row.get("basePrice")),
                Double.parseDouble(row.get("defense")), Double.parseDouble(row.get("defenseGrowth")),
                row.get("type")));

        List<PotionTto> potions = readTable(directory.resolve("potions.txt"), row ->
                new PotionTto(row.get("id"), row.get("name"), Long.parseLong(row.get("basePrice")),
                row.get("effectType"),
                Double.parseDouble(row.get("baseAmount")), Double.parseDouble(row.get("amountGrowth")),
                Double.parseDouble(row.get("maxRatio"))));

        List<SpellTto> spells = readTable(directory.resolve("spells.txt"), row ->
                new SpellTto(row.get("id"), row.get("name"), Long.parseLong(row.get("basePrice")),
                Integer.parseInt(row.get("requiredLevel")), row.get("type"),
                Double.parseDouble(row.get("baseDamage")), Double.parseDouble(row.get("baseMp"))));

        List<Skill> skills = readTable(directory.resolve("skills.txt"), row ->
                new Skill(row.get("id"), row.get("name"), row.get("class"),
                Integer.parseInt(row.get("unlockLevel")), Long.parseLong(row.get("mp")), Long.parseLong(row.get("cd")),
                SkillEffectType.valueOf(row.get("effectType")), SkillTargetMode.valueOf(row.get("targetMode")),
                Double.parseDouble(row.get("multiplier")), Integer.parseInt(row.get("duration"))));

        List<EquipmentRuleTto> equipmentRuleTtos = readTable(directory.resolve("equipment-rules.txt"), row ->
                new EquipmentRuleTto(row.get("class"), Arrays.asList(row.get("weapons").split(",")),
                Arrays.asList(row.get("armor").split(",")), Boolean.parseBoolean(row.get("dualWield")),
                row.get("starterWeapon")));

        return new GameConfig(heroTtos, monsterTtos, weapons, armor, potions, spells, skills, equipmentRuleTtos);
    }

    private <T> List<T> readTable(Path file, Function<Map<String, String>, T> convert) throws IOException {
        List<T> definitions = new ArrayList<T>();
        String[] columns = null;
        for(String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if(line.startsWith("\uFEFF"))
                line = line.substring(1);
            line = line.trim();
            if(line.isEmpty() || line.startsWith("#"))
                continue;
            if(columns == null) {
                columns = line.split("\\|", -1);
                for(int i = 0; i < columns.length; i++)
                    columns[i] = columns[i].trim();
            }else {
                String[] cells = line.split("\\|", -1);
                Map<String, String> values = new LinkedHashMap<String, String>();
                for(int i = 0; i < columns.length; i++)
                    values.put(columns[i], cells[i].trim());
                definitions.add(convert.apply(values));
            }
        }
        return definitions;
    }
}

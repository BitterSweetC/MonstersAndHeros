package legends.entity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Successful actions and actual outcomes for one party's session. */
public final class SessionStatistics {
    private final Map<Hero, HeroTotals> heroes = new LinkedHashMap<>();
    private final Map<String, Long> actions = new LinkedHashMap<>();
    private final StringBuilder history = new StringBuilder();
    private long steps, encounters, wins, losses;
    private long bought, sold, repairs, purchaseGold, saleGold, repairGold;

    private static final class HeroTotals {
        private final int initialLevel;
        private final long initialExperience;
        private long damageDealt, damageTaken, potionHp, potionMp, rewardGold;

        private HeroTotals(Hero hero) {
            initialLevel = hero.getLevel();
            initialExperience = hero.getExperience();
        }
    }

    public void register(Hero hero) {
        heroes.computeIfAbsent(hero, HeroTotals::new);
    }

    public void record(String event) {
        history.append(event).append('\n');
    }

    public void moved(Party party) {
        steps++;
        record("Move " + steps + " | Position (" + (party.getRow() + 1) + ", " + (party.getCol() + 1) + ")");
    }

    public long startBattle(Party party, List<Monster> monsters) {
        for(Hero hero : party.getHeroes())
            register(hero);
        encounters++;
        record("Battle " + encounters + " | Started | " + monsters);
        return encounters;
    }

    public void record(Battle battle, String kind, List<String> results) {
        for(String result : results)
            record("Battle " + battle.getNumber() + " | Round " + battle.getRound() + " | " + kind + " | " + result);
    }

    public void action(Battle battle, String kind, List<String> results) {
        actions.merge(kind, 1L, Long::sum);
        record(battle, kind, results);
    }

    public void damage(Combatant source, Combatant target, long actualDamage) {
        if(source instanceof Hero)
            heroes.get((Hero) source).damageDealt += actualDamage;
        if(target instanceof Hero)
            heroes.get((Hero) target).damageTaken += actualDamage;
    }

    public void potion(Hero hero, long beforeHp, long beforeMp) {
        HeroTotals totals = heroes.get(hero);
        totals.potionHp += hero.getHp() - beforeHp;
        totals.potionMp += hero.getMp() - beforeMp;
    }

    public void explorationPotion(Hero hero, ItemInstance item, long beforeHp, long beforeMp) {
        potion(hero, beforeHp, beforeMp);
        explorationAction("Potion", hero.getName() + " | Potion " + item.getName() + " | HP +"
                + (hero.getHp() - beforeHp) + ", MP +" + (hero.getMp() - beforeMp)
                + " | Effect " + item.getPotion().getEffectType());
    }

    public void explorationAction(String kind, String result) {
        actions.merge(kind, 1L, Long::sum);
        record("Explore | " + kind + " | " + result);
    }

    public void reward(Hero hero, long gold, long experience) {
        heroes.get(hero).rewardGold += gold;
        record("Reward | " + hero.getName() + " | Gold +" + gold + " | EXP +" + experience);
    }

    public void settled(Battle battle) {
        if(battle.isVictory())
            wins++;
        else
            losses++;
        record("Battle " + battle.getNumber() + " | " + (battle.isVictory() ? "Victory" : "Defeat"));
    }

    public void bought(Hero hero, ItemInstance item, long gold) {
        bought++;
        purchaseGold += gold;
        record("Market | " + hero.getName() + " | Buy " + item.getName() + " | " + gold + " gold");
    }

    public void sold(Hero hero, ItemInstance item, long gold) {
        sold++;
        saleGold += gold;
        record("Market | " + hero.getName() + " | Sell " + item.getName() + " | " + gold + " gold");
    }

    public void repaired(Hero hero, ItemInstance item, long gold) {
        repairs++;
        repairGold += gold;
        record("Market | " + hero.getName() + " | Repair " + item.getName() + " | " + gold + " gold");
    }

    public String summary() {
        StringBuilder result = new StringBuilder("Session statistics\n");
        result.append("Steps: ").append(steps).append(" | Encounters: ").append(encounters).append('\n');
        result.append("Battles: ").append(wins).append(" won, ").append(losses).append(" lost, ")
                .append(encounters - wins - losses).append(" unfinished\n");
        result.append("Actions: ");
        for(String kind : new String[] { "Attack", "Skill", "Spell", "Potion", "Equipment" })
            result.append(kind).append('=').append(actions.getOrDefault(kind, 0L)).append("  ");
        result.append("\nTrading: Bought ").append(bought).append(" / ").append(purchaseGold)
                .append(" gold | Sold ").append(sold).append(" / ").append(saleGold)
                .append(" gold | Repairs ").append(repairs).append(" / ").append(repairGold).append(" gold\n");
        result.append("Hero combat (potion recovery excludes round recovery, leveling and revival)\n");
        result.append(String.format("%-24s %12s %12s %10s %10s%n",
                "Hero", "Damage dealt", "Damage taken", "Potion HP", "Potion MP"));
        for(Map.Entry<Hero, HeroTotals> entry : heroes.entrySet()) {
            HeroTotals totals = entry.getValue();
            result.append(String.format("%-24s %12d %12d %10d %10d%n", entry.getKey().getName(),
                    totals.damageDealt, totals.damageTaken, totals.potionHp, totals.potionMp));
        }
        result.append("Hero growth (reward gold excludes starting gold and sales)\n");
        for(Map.Entry<Hero, HeroTotals> entry : heroes.entrySet()) {
            Hero hero = entry.getKey();
            HeroTotals totals = entry.getValue();
            result.append(hero.getName()).append(" | Level: ").append(hero.getLevel()).append(" (+")
                    .append(hero.getLevel() - totals.initialLevel).append(") | EXP: ")
                    .append(hero.getExperience()).append(" | EXP gained: ")
                    .append(hero.getExperience() - totals.initialExperience).append(" | Reward gold: ")
                    .append(totals.rewardGold).append(" | Gold held: ").append(hero.getGold()).append('\n');
        }
        return result.toString();
    }

    public Path save(Path directory, long seed) throws IOException {
        Files.createDirectories(directory);
        Path file = Files.createTempFile(directory, "session-", ".txt");
        Files.write(file, report(seed).getBytes(StandardCharsets.UTF_8));
        return file;
    }

    public String report(long seed) {
        return "Seed: " + seed + "\n" + summary() + "\nAction history\n" + history;
    }
}

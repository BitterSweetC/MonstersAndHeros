package legends.controller;

import java.io.UncheckedIOException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import legends.config.GameConfig;
import legends.entity.Battle;
import legends.entity.Combatant;
import legends.entity.MhGameSession;
import legends.entity.Hero;
import legends.entity.ItemInstance;
import legends.entity.Market;
import legends.entity.Monster;
import legends.entity.Skill;
import legends.entity.StatusEffect;
import legends.entity.template.HeroTto;
import legends.enums.GameState;
import legends.enums.SkillTargetMode;
import legends.service.BattleService;
import legends.service.ExplorationService;
import legends.service.DamageCalculatorService;
import legends.service.impl.BattleServiceImpl;
import legends.service.impl.ExplorationServiceImpl;
import legends.service.impl.DamageCalculatorServiceImpl;

/**
 * Main game loop. Routes between setup, exploration, battle, and market states.
 */
public final class MhGameController {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private final GameConfig config;
    private final long seed;
    private final Scanner scanner = new Scanner(System.in);
    private final boolean colorEnabled;
    private final ExplorationService exploration;
    private MhGameSession session;
    private GameState state = GameState.SETUP;
    private Hero marketHero;

    public MhGameController(GameConfig config, long seed, boolean colorEnabled) {
        this.config = config;
        this.seed = seed;
        this.colorEnabled = colorEnabled;
        this.exploration = new ExplorationServiceImpl();
    }

    public void run() {
        System.out.println("Legends: Monsters and Heroes | seed " + seed);
        help();
        try {
            while(state != GameState.EXIT) {
                try {
                    switch(state) {
                        case SETUP :
                            setup();
                            break;
                        case WORLD :
                            world();
                            break;
                        case BATTLE :
                            battle();
                            break;
                        case MARKET :
                            market();
                            break;
                        case GAME_OVER :
                            String choice;
                            while(true) {
                                System.out.print(colored("Game over. R restart, X exit > ", CYAN));
                                choice = scanner.nextLine().trim().toUpperCase();
                                if(!handleCommand(choice))
                                    break;
                            }
                            if(choice.equals("R") || choice.equals("r"))
                                state = GameState.SETUP;
                            else
                                state = GameState.EXIT;
                            break;
                        default :
                            state = GameState.EXIT;
                    }
                }
                catch(IllegalArgumentException e) {
                    System.out.println(colored("Cannot do that: " + e.getMessage(), RED));
                }
            }
        }
        catch(NoSuchElementException e) {
            if(scanner.ioException() != null)
                throw new UncheckedIOException(scanner.ioException());
        }
        finishSession();
        System.out.println("Goodbye.");
    }

    private void setup() {
        finishSession();
        session = new MhGameSession(config, seed);
        int heroCount = number("Number of heroes", 1, 3);
        List<HeroTto> available = new ArrayList<>(config.getHeroTtos());
        List<Hero> heroes = new ArrayList<>();
        
        while(heroes.size() < heroCount) {
            HeroTto definition = chooseHero("Select hero " + (heroes.size() + 1), available);
            if(definition != null) {
                heroes.add(new Hero(definition, config));
                available.remove(definition);
            }
        }
        
        session.start(heroes);
        info();
        System.out.println(session.getWorld().render(session.getParty()));
        state = GameState.WORLD;
    }

    private Hero hero() {
        return choose("Select hero", session.getParty().getHeroes());
    }

    private void world() {
        System.out.println("Position (" + (session.getParty().getRow() + 1) + " , " + (session.getParty().getCol() + 1) + ") "
                + session.getWorld().getTile(session.getParty().getRow(), session.getParty().getCol()));
        String command;

        while(true) {
            System.out.print(colored(session.getWorld().getMarket(session.getParty()) != null
                    ? "Explore (M to enter market) > " : "Explore > ", CYAN));
            command = scanner.nextLine().trim().toUpperCase();
            if(!handleCommand(command))
                break;
        }
        int rowChange = 0, colChange = 0;
        // parse movement commands
        switch(command) {
            case "W" :
                rowChange = -1;
                break;
            case "S" :
                rowChange = 1;
                break;
            case "A" :
                colChange = -1;
                break;
            case "D" :
                colChange = 1;
                break;
            case "M" :
                if(session.getWorld().getMarket(session.getParty()) == null)
                    throw new IllegalArgumentException("Stand on a market tile first");
                marketHero = session.getParty().getHeroes().get(0);
                state = GameState.MARKET;
                return;
            case "B" :
                inventory();
                return;
            case "E" : {
                Hero hero = hero();
                if(hero != null)
                    equipment(hero, null);
                return;
            }
            case "K" :
                skills();
                return;
            default :
                System.out.println(colored("Unknown command. H for help.", RED));
                return;
        }

        if(!session.getWorld().move(session.getParty(), rowChange, colChange)) {
                System.out.println(colored("Movement blocked by the map edge or a blocked (#) tile. Choose another direction.", RED));
            return;
        }

        session.getStatistics().moved(session.getParty());

        System.out.println(session.getWorld().render(session.getParty()));
        String event = exploration.entered(session);
        
        if(event.startsWith("Treasure:"))
            System.out.println(colored(event, YELLOW));
        else if(event.equals("Monsters appeared!"))
            System.out.println(colored(event, RED));
        else if(!event.isEmpty())
            System.out.println(event);
        
        if(session.getBattle() != null) {
            state = GameState.BATTLE;
            info();
        }
    }

    private List<ItemInstance> items(Hero hero, String category) {
        List<ItemInstance> list = new ArrayList<>();
        
        for(ItemInstance item : hero.getInventory().getItems())
            if(category == null || item.getCategory().equals(category))
                list.add(item);
        
        return list;
    }

    private void inventory() {
        Hero hero = hero();
        
        if(hero == null)
            return;
        
        printInventory(hero);
        String choice;
        while(true) {
            System.out.print(colored("U use potion, 0 return > ", CYAN));
            choice = scanner.nextLine().trim().toUpperCase();
            if(!handleCommand(choice))
                break;
        }
        if(choice.equals("U")) {
            ItemInstance item = choose("Potion", items(hero, "potions"));
            if(item != null) {
                long beforeHp = hero.getHp(), beforeMp = hero.getMp();
                hero.drink(item);
                session.getStatistics().explorationPotion(hero, item, beforeHp, beforeMp);
                System.out.println(hero.getName() + " used " + item.getName() + ": " + item.getDetails()
                    + " | HP " + beforeHp + " -> " + hero.getHp() + "/" + hero.getMaxHp()
                        + " | MP " + beforeMp + " -> " + hero.getMp() + "/" + hero.getMaxMp());
            }
        }
    }

    private void printInventory(Hero hero) {
        System.out.println(hero.getName() + " inventory:");
        if(hero.getInventory().getItems().isEmpty())
            System.out.println("  (empty)");

        for(ItemInstance item : hero.getInventory().getItems())
            System.out.println("  " + item);
    }

    private void equipment(Hero hero, BattleService service) {
        List<ItemInstance> list = new ArrayList<>();
        for(ItemInstance item : hero.getInventory().getItems())
            if(item.isDurable())
                list.add(item);
        
        System.out.println(hero.getEquipment());

        ItemInstance item = choose("Equipment", list);
        if(item == null)
            return;
        String mode;
        while(true) {
            System.out.print(colored("SINGLE main hand / BOTH both hands / OFF off hand; U unequip, 0 cancel > ", CYAN));
            mode = scanner.nextLine().trim().toUpperCase();
            if(!handleCommand(mode))
                break;
        }
        
        if(mode.equals("0"))
            return;
        
        if(mode.equals("U")) {
            if(!hero.getEquipment().isEquipped(item))
                throw new IllegalArgumentException("Item is not equipped");
            if(service == null)
                hero.getEquipment().unequip(item);
            else
                logs(service.unequip(session.getBattle(), hero, item));
        }else if(service == null)
            hero.getEquipment().equip(hero, item, mode);
        else
            logs(service.equip(session.getBattle(), hero, item, mode));
        if(service == null)
            session.getStatistics().explorationAction("Equipment", hero.getName() + " | " + item.getName() + " | " + mode);
    }

    private void skills() {
        Hero hero = hero();
        if(hero == null)
            return;
        System.out.println("Unlocked skills:");
        Skill skill = choose("Unlocked skill to equip", hero.getUnlockedSkills(), option -> skillDescription(option)
                + (hero.getSkillSlots().contains(option)
                        ? " | equipped in slot " + (hero.getSkillSlots().indexOf(option) + 1) : " | not equipped"));
        if(skill == null)
            return;
        if(hero.getSkillSlots().contains(skill)) {
            System.out.println("Already equipped in slot " + (hero.getSkillSlots().indexOf(skill) + 1) + ". No changes made.");
            return;
        }
        
        int slot = number("Slot (0 cancel)", 0, Math.min(3, hero.getSkillSlots().size() + 1));
        if(slot != 0) {
            hero.slot(slot - 1, skill);
            session.getStatistics().record("Explore | " + hero.getName() + " | Slot " + slot + " | " + skill.getName());
        }
    }

    private void logs(List<String> logs) {
        for(String line : logs) {
            if(line.contains(" damage"))
                System.out.println(colored(line, RED));
            else if(line.endsWith(": dodged"))
                System.out.println(colored(line, CYAN));
            else if(line.startsWith("Round recovery:") || line.startsWith("Life steal:"))
                System.out.println(colored(line, GREEN));
            else
                System.out.println(line);
        }
    }

    private void battle() {
        Battle battle = session.getBattle();
        BattleService service = new BattleServiceImpl(session.getRandom());
        
        // check if battle just ended
        if(battle.isOver()) {
            if(service.settle(battle)) {
                session.victory();
                System.out.println(colored("Victory! Surviving heroes receive full rewards; fainted allies revived.", GREEN));
                System.out.println(session.getWorld().render(session.getParty()));
                state = GameState.WORLD;
            }
            else {
                System.out.println(colored("Your party has fallen.", RED));
                state = GameState.GAME_OVER;
            }
            session.setBattle(null);
            return;
        }
        
        Hero hero = battle.getCurrentHero();
        if(hero == null) {
            System.out.println(colored("\n=== Enemy turn ===", RED));
            logs(service.finishRound(battle));
            return;
        }
        
        System.out.println(colored("\n=== Round " + battle.getRound() + " ===", CYAN));
        System.out.println("Active hero: " + hero.getName() + " (Lv" + hero.getLevel() + " " + hero.getHeroClass() + ")");
        System.out.println("HP: " + hero.getHp() + "/" + hero.getMaxHp() + " | MP: " + hero.getMp() + "/" + hero.getMaxMp());
        System.out.println("Monsters:");

        for(Monster monster : battle.getLiving())
            System.out.println("  " + monster);
        String command;
        while(true) {
            System.out.print(colored("1 attack, 2 skill, 3 spell, 4 potion, 5 equipment > ", CYAN));
            command = scanner.nextLine().trim();
            if(!handleCommand(command))
                break;
        }
        
        switch(command) {
            case "1" : {
                Monster monster = choose("Target", battle.getLiving());
                if(monster != null)
                    logs(service.attack(battle, hero, monster));
                break;
            }
            case "2" :
                useSkill(battle, hero, service);
                break;
            case "3" : {
                ItemInstance item = choose("Market spell", items(hero, "spells"));
                if(item == null)
                    break;
                
                Monster monster = choose("Target", battle.getLiving());
                if(monster != null)
                    logs(service.cast(battle, hero, item, monster));
                break;
            }
            case "4" : {
                ItemInstance item = choose("Potion", items(hero, "potions"));
                if(item != null)
                    logs(service.potion(battle, hero, item));
                break;
            }
            case "5" :
                equipment(hero, service);
                break;
            default :
                System.out.println(colored("Unknown battle action. Choose 1-5, I for information, or H for help.", RED));
        }
    }

    private void useSkill(Battle battle, Hero hero, BattleService service) {
        Skill skill = chooseSkill("Class skill", hero.getSkillSlots());
        if(skill == null)
            return;
        SkillTargetMode mode = skill.getTargetMode();
        List<Combatant> targets = new ArrayList<>();
        
        if(mode == SkillTargetMode.ENEMY) {
            Monster target = choose("Target", battle.getLiving());
            if(target == null)
                return;
            targets.add(target);
        }else if(mode == SkillTargetMode.ALLY) {
            Hero target = choose("Ally", session.getParty().getLiving());
            if(target == null)
                return;
            targets.add(target);
        }
        
        logs(service.skill(battle, hero, skill, targets));
    }

    private void market() {
        Market market = session.getWorld().getMarket(session.getParty());
        System.out.println("Market | " + marketHero.getName() + " | Gold " + marketHero.getGold());
        
        String command;
        while(true) {
            System.out.print(colored("1 buy, 2 sell, 3 repair, 4 select trading hero, 5 inventory, 0 leave > ", CYAN));
            command = scanner.nextLine().trim();
            if(!handleCommand(command))
                break;
        }
        switch(command) {
            case "0" :
                state = GameState.WORLD;
                break;
            case "1" : {
                ItemInstance item = offer(marketHero, market.getStock());
                if(item != null) {
                    long beforeGold = marketHero.getGold();
                    session.getMarkets().buy(market, marketHero, item);
                    System.out.println(colored("Purchased " + item.getName() + " for " + (beforeGold - marketHero.getGold())
                            + " gold. Gold: " + beforeGold + " -> " + marketHero.getGold(), GREEN));
                }
                break;
            }
            case "2" : {
                ItemInstance item = choose("Sell", marketHero.getInventory().getItems(),
                        option -> option + " | sell " + session.getMarkets().getSellPrice(option) + " gold");
                if(item != null) {
                    long beforeGold = marketHero.getGold();
                    session.getMarkets().sell(market, marketHero, item);
                    System.out.println(colored("Sold " + item.getName() + " for " + (marketHero.getGold() - beforeGold)
                            + " gold. Gold: " + beforeGold + " -> " + marketHero.getGold(), GREEN));
                }
                break;
            }
            case "3" : {
                List<ItemInstance> damaged = new ArrayList<ItemInstance>();
                for(ItemInstance item : marketHero.getInventory().getItems())
                    if(item.isDurable() && item.getDurability() < item.getMaxDurability())
                        damaged.add(item);
                if(damaged.isEmpty()) {
                    System.out.println("No damaged equipment to repair. Only weapons and armor below full durability are listed.");
                    break;
                }
                ItemInstance item = choose("Repair", damaged,
                        option -> option + " | repair " + session.getMarkets().getRepairPrice(option) + " gold");
                if(item != null) {
                    long beforeGold = marketHero.getGold();
                    session.getMarkets().repair(marketHero, item);
                    System.out.println(colored("Repaired " + item.getName() + " to " + item.getDurability() + "/"
                            + item.getMaxDurability() + " durability for " + (beforeGold - marketHero.getGold())
                            + " gold. Gold: " + beforeGold + " -> " + marketHero.getGold(), GREEN));
                }
                break;
            }
            case "4" : {
                Hero hero = hero();
                if(hero != null)
                    marketHero = hero;
                break;
            }
            case "5" :
                printInventory(marketHero);
                break;
            default :
                System.out.println(colored("Unknown market action. Choose 1-5, or 0 to leave the market.", RED));
        }
    }

    private void printSummary() {
        if(session == null || session.getParty() == null)
            return;
        System.out.print(session.getStatistics().summary());
    }

    private void finishSession() {
        if(session == null || session.getParty() == null)
            return;
        printSummary();
        try {
            System.out.println("Statistics and action history saved: " + session.getStatistics().save(Paths.get("logs"), seed));
        }
        catch(IOException | SecurityException e) {
            System.err.println("Cannot save statistics: " + e.getMessage() + ". Full report follows; copy it to keep a record.");
            System.out.print(session.getStatistics().report(seed));
        }
    }

    private String colored(String message, String color) {
        return colorEnabled ? color + message + RESET : message;
    }

    private boolean handleCommand(String value) {
        String command = value.toUpperCase();
        if(command.equals("Q")) {
            while(true) {
                System.out.print(colored("Quit? Character progress is not saved; statistics/history will be exported. Y/Q yes, N/0 cancel > ", CYAN));
                String confirm = scanner.nextLine().trim().toUpperCase();
                if(confirm.equals("Y") || confirm.equals("Q")) {
                    finishSession();
                    System.out.println("Goodbye.");
                    System.exit(0);
                }
                if(confirm.equals("N") || confirm.equals("0"))
                    return true;
                if(!global(confirm))
                    System.out.println(colored("Enter Y or N.", RED));
            }
        }
        if(global(command))
            return true;
        if(value.isEmpty()) {
            System.out.println(colored("Enter a command; 0 cancels.", RED));
            return true;
        }
        return false;
    }

    private boolean global(String command) {
        if(command.equals("T")) {
            printSummary();
            return true;
        }
        if(command.equals("H")) {
            help();
            return true;
        }
        if(command.equals("I")) {
            info();
            return true;
        }
        if(command.equals("P")) {
            if(session != null && session.getWorld() != null)
                System.out.println(session.getWorld().render(session.getParty()));
            else
                System.out.println("World not created yet.");
            return true;
        }
        if(session != null && session.getBattle() != null && (command.equals("B") || command.equals("K"))) {
            for(Hero hero : session.getParty().getHeroes()) {
                if(command.equals("B"))
                    printInventory(hero);
                else {
                    System.out.println(hero.getName() + " equipped skills:");
                    for(int slot = 0; slot < hero.getSkillSlots().size(); slot++)
                        System.out.println("  Slot " + (slot + 1) + ": " + skillDescription(hero.getSkillSlots().get(slot)));
                }
            }
            System.out.println("Viewing only. Use the battle action menu to make changes.");
            return true;
        }
        return false;
    }

    private void help() {
        System.out.println("W/A/S/D move; M market (on $ tile); B inventory/potions; E equipment; K skill slots.\n"
                + "Battle: 1 attack, 2 class skill, 3 market spell, 4 potion, 5 equipment.\n"
                + "Global: I information, P map, T statistics, H help, Q quit; 0 cancels menus.\n"
                + "Select entries using the menu number before the dot; # on an item is its unique ID.\n"
                + "Stats: STR strength, DEX dexterity, AGI agility, DEF defense; MP is mana.\n"
                + "Heroes act first. One valid action per hero; viewing/canceling/invalid input is free.\n"
                + "Spells are single-use and cost MP even when dodged. No revival during battle.\n"
                + "Fainted heroes revive after victory without rewards. All fainted = game over.\n"
                + "Equipment loses durability; repair at markets. Saves are not supported.\n"
                + "Statistics and action history are saved to logs/ on exit or restart; a restart resets statistics.");
    }

    private void info() {
        if(session == null || session.getParty() == null) {
            System.out.println("Choose your party first.");
            return;
        }
        System.out.println(colored("\n=== Heroes ===", CYAN));
        for(Hero hero : session.getParty().getHeroes()) {
            System.out.println(hero);
            System.out.println(hero.getEquipment());
            DamageCalculatorService calculator = new DamageCalculatorServiceImpl();
            System.out.println("Attack power " + Math.round(calculator.getPhysical(hero)) + " | Magic power "
                    + Math.round(calculator.getMagic(hero)) + " | Round recovery " + hero.getHpRecovery() + " HP / "
                    + hero.getMpRecovery() + " MP");
            for(StatusEffect effect : hero.getEffects())
                System.out.println("  " + statusDescription(effect));
        }
        if(session.getBattle() != null) {
            System.out.println(colored("\n=== Monsters ===", RED));
            for(Monster monster : session.getBattle().getMonsters()) {
                System.out.println(monster + " damage " + monster.getDamage() + " defense " + monster.getDefense() + " base dodge "
                        + new DamageCalculatorServiceImpl().getDodge(monster, 0));
                for(StatusEffect effect : monster.getEffects())
                    System.out.println("  " + statusDescription(effect));
            }
        }
        System.out.println("\nVictories: " + session.getVictories() + "\n");
    }

    private String statusDescription(StatusEffect effect) {
        String label;
        switch(effect.getKind()) {
            case "ICE":
                label = "Attack damage -";
                break;
            case "FIRE":
                label = "Defense -";
                break;
            case "LIGHTNING":
                label = "Dodge chance -";
                break;
            case "DEFENSE":
                label = "Defense +";
                break;
            case "REDUCTION":
                label = "Damage taken -";
                break;
            default:
                label = effect.getKind().replace("POTION_", "Potion boost ") + " +";
        }
        return label + Math.round(100 * effect.getValue()) + "% | "
                + (effect.getRemaining() == 0 ? "until battle ends"
                        : effect.getRemaining() + " round(s) remaining, including this round");
    }

    private int number(String prompt, int min, int max) {
        while(true) {
            System.out.print(colored(prompt + " > ", CYAN));
            String value = scanner.nextLine().trim();
            if(handleCommand(value))
                continue;
            try {
                int selection = Integer.parseInt(value);
                if(selection >= min && selection <= max)
                    return selection;
            }
            catch(NumberFormatException ignored) {
            }
            System.out.println(colored("Enter a number from " + min + " to " + max + ".", RED));
        }
    }

    private ItemInstance offer(Hero hero, List<ItemInstance> stock) {
        if(stock.isEmpty()) {
            System.out.println("This market is out of stock. Markets restock after every five victories.");
            return null;
        }

        for(int index = 0; index < stock.size(); index++) {
            ItemInstance item = stock.get(index);
            String availability = (item.getLevel() > hero.getLevel() ? "level too high" : "level OK") + "; "
                    + (hero.getEquipment().canEquip(hero, item) ? "class OK" : "class cannot equip")
                    + (item.isUsable() ? "" : "; repair before equipping");
            String power = item.getCategory().equals("spells")
                    ? " | spell power " + Math.round(new DamageCalculatorServiceImpl().getSpell(hero, item))
                    : "";
            System.out.println((index + 1) + ". " + item + " | buy " + session.getMarkets().getBuyPrice(item) + " | " + availability
                    + power);
        }

        int selection = number("Buy (0 cancel)", 0, stock.size());
        return selection == 0 ? null : stock.get(selection - 1);
    }

    private <T> T choose(String prompt, List<T> options) {
        return choose(prompt, options, Object::toString);
    }

    private HeroTto chooseHero(String prompt, List<HeroTto> heroes) {
        List<String> classes = new ArrayList<>();
        for(HeroTto hero : heroes)
            if(!classes.contains(hero.getHeroClass()))
                classes.add(hero.getHeroClass());
        String heroClass = choose("Select class", classes);
        if(heroClass == null)
            return null;
        List<HeroTto> candidates = new ArrayList<>();
        for(HeroTto hero : heroes)
            if(hero.getHeroClass().equals(heroClass))
                candidates.add(hero);
        return choose(prompt, candidates, this::heroDescription);
    }

    private Skill chooseSkill(String prompt, List<Skill> skills) {
        return choose(prompt, skills, this::skillDescription);
    }

    private String heroDescription(HeroTto hero) {
        return hero.getName() + " [" + hero.getHeroClass() + ", HP " + hero.getHp() + ", MP "
                + hero.getMp() + ", STR " + hero.getStrength() + ", DEX " + hero.getDexterity()
                + ", AGI " + hero.getAgility() + ", Gold " + hero.getInitialGold()
                + ", EXP " + hero.getInitialExp() + "]";
    }

    private String skillDescription(Skill skill) {
        String effect;
        switch(skill.getEffectType()) {
            case PHYSICAL:
                effect = "physical damage x" + skill.getMultiplier();
                break;
            case MAGIC:
                effect = "magic damage x" + skill.getMultiplier();
                break;
            case DEFENSE:
                effect = "defense +" + Math.round(100 * skill.getMultiplier()) + "% for " + skill.getDuration() + " rounds";
                break;
            case REDUCTION:
                effect = "damage taken -" + Math.round(100 * skill.getMultiplier()) + "% for " + skill.getDuration() + " rounds";
                break;
            default:
                throw new IllegalArgumentException("Unknown skill effect: " + skill.getEffectType());
        }
        return skill.getName() + " [requires Lv" + skill.getUnlockLevel() + ", MP cost " + skill.getMpCost()
                + ", cooldown: " + skill.getCooldown() + " other actions by this hero; " + effect
                + "; target: " + skill.getTargetMode().name().toLowerCase().replace('_', ' ') + "]";
    }

    private <T> T choose(String prompt, List<T> options, Function<T, String> description) {
        if(options.isEmpty()) {
            System.out.println(prompt + ": nothing available to select. Returning to the previous menu.");
            return null;
        }
        for(int i = 0; i < options.size(); i++)
            System.out.println((i + 1) + ". " + description.apply(options.get(i)));
        int selection = number(prompt + " (0 cancel)", 0, options.size());
        return selection == 0 ? null : options.get(selection - 1);
    }
}

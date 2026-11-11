# CS611 - Assignment
## Legends: Monsters and Heroes
---------------------------------------------------------------------------
Yuheng Guo
yuhengg@bu.edu
U93552042

This is a Java game that runs in the terminal. You can choose 1 to 3 heroes and move around a 10x10 map. You can fight monsters, buy and sell items, change equipment, and get experience and gold. There are three hero classes: Warrior, Sorcerer, and Paladin.

## Files
---------------------------------------------------------------------------

### Core & Controller

- `src/main/legends/Main.java`: Reads the command-line options, loads the game data, and starts the game.
- `src/main/legends/controller/GameController.java`: Reads the player's input and shows the menus. It switches between choosing heroes, exploring, fighting, shopping, and game over.

### Configuration & Data Loading

- `src/main/legends/config/ConfigLoader.java`: Reads the text files in `data/`. The columns are separated by `|`. It uses the values to create hero, monster, item, skill, and equipment rule data.
- `src/main/legends/config/GameConfig.java`: Stores the data loaded from the files. Other classes can read its lists but cannot add or remove entries from them.

### Data Files (`data/`)

- `heroes.txt`: Stores starting stats, HP and MP growth, starting gold, and experience for each hero.
- `monsters.txt`: Stores monster base stats, stat growth per level, spawn weights, and exp/gold rewards.
- `weapons.txt`: Stores weapon prices, physical damage, hand requirements (1 or 2 hands), and magic bonuses.
- `armor.txt`: Stores armor prices, defense values, and armor types (`LIGHT`, `MEDIUM`, `HEAVY`).
- `potions.txt`: Stores potion prices, recovery amounts, and attribute boost limits.
- `spells.txt`: Stores single-use market spells, including level requirements, MP costs, damage, and spell types.
- `skills.txt`: Stores unlockable class skills, MP costs, cooldowns, target modes, and effects.
- `equipment-rules.txt`: Stores weapon and armor restrictions for each hero class, dual-wield rules, and starting weapons.

### Entities & Data Models

- `src/main/legends/entity/Combatant.java`: The parent class of Hero and Monster. It stores their HP and level, and handles damage, healing, and status effects.
- `src/main/legends/entity/Hero.java`: Stores a hero's MP, properties, inventory, equipment, skills, money, and experience. It also handles potions, skill cooldowns, and leveling up.
- `src/main/legends/entity/Monster.java`: Stores a monster's type and uses its level to calculate its properties and rewards. Different monster types have different abilities.
- `src/main/legends/entity/Party.java`: Stores the selected heroes and their position on the map. It can find the living heroes and the team's highest level.
- `src/main/legends/entity/World.java`: Stores the map and its markets. It checks whether the team can move to a place and prints the map.
- `src/main/legends/entity/Battle.java`: Stores the monsters and the current battle round. It decides which hero acts next and records whether the battle rewards have been given.
- `src/main/legends/entity/Market.java`: Stores the items in one market. It also remembers which items were sold by players.
- `src/main/legends/entity/Inventory.java`: Stores the items owned by a hero.
- `src/main/legends/entity/ItemInstance.java`: Stores one item's ID, level, price, durability, and whether it has been used. Items with the same name can have different values.
- `src/main/legends/entity/Equipment.java`: Stores the hero's equipped weapons and armor. It checks class restrictions and hand choices, and handles broken weapons.
- `src/main/legends/entity/Skill.java`: Stores a skill's name, class, unlock level, MP cost, cooldown, target, and effect.
- `src/main/legends/entity/StatusEffect.java`: Stores an effect's type, strength, and remaining time.
- `src/main/legends/entity/GameSession.java`: Stores the team, world, current battle, random number generator, market service, statistics, and number of victories.
- `src/main/legends/entity/SessionStatistics.java`: Counts successful actions, actual damage, potion recovery, trades, and hero growth. It also records the actions and saves a text report.

### Entity Templates (`entity/template/`)

- `ItemTto.java`: The parent class for item templates. It stores the item ID, name, and base price.
- `HeroTto.java`: Stores a hero's starting properties, HP and MP growth, starting gold, and starting experience.
- `MonsterTto.java`: Stores a monster's starting properties, growth, selection weight, and rewards per level.
- `WeaponTto.java`, `ArmorTto.java`, `PotionTto.java`, `SpellTto.java`: Store the shared properties of weapons, armor, potions, and spells.
- `EquipmentRuleTto.java`: Stores which weapons and armor each hero class can use. It also stores whether the class can dual wield and which weapon it starts with.

### Services & Business Logic (`service/` and `service/impl/`)

- `BattleService.java` & `BattleServiceImpl.java`: Handle hero actions, monster attacks, round recovery, and battle rewards.
- `DamageCalculatorService.java` & `DamageCalculatorServiceImpl.java`: Calculate attack power, spell damage, damage after defense, critical chances, and dodge chances.
- `MarketService.java` & `MarketServiceImpl.java`: Create market items, calculate prices, and handle buying, selling, repairing, and restocking.
- `ExplorationService.java` & `ExplorationServiceImpl.java`: Decide what happens when the team enters a common tile. The team may meet monsters, find treasure, or find nothing.

### Factories, Enums & Utilities

- `src/main/legends/factory/WorldGenerator.java`: Creates the map and places the markets and blocked tiles. It uses BFS to check that all walkable places are connected.
- `src/main/legends/factory/EncounterGenerator.java`: Creates monsters based on the team's size and highest level. It uses weights to choose their number, levels, and names.
- `src/main/legends/utils/RandomWeights.java`: Chooses a random result from a list of weights. A larger weight gives a higher chance.
- `src/main/legends/enums/`: `GameState.java`, `TileType.java`, `SkillEffectType.java`, and `SkillTargetMode.java` list the possible game states, tile types, skill effects, and skill targets.

## Notes
---------------------------------------------------------------------------

1. GameController reads the player's input and prints the game information. BattleService, MarketService, and ExplorationService handle the game actions. DamageCalculatorService handles the combat calculations.
2. Hero and Monster both inherit from Combatant. They share the code for HP, healing, and status effects, but they also have their own properties and actions.
3. There are three hero classes. Each class has several heroes with different starting properties.
4. The map has 15 blocked tiles, 25 markets, and 60 common tiles. We use Breadth-First Search (BFS) to check that all walkable places are connected. The team starts on a market tile.
5. The map uses `#` for blocked tiles, `$` for markets, `.` for common tiles, and `@` for the team. Every cell uses the same width, so the map lines up in a terminal with a monospaced font.
6. Defense reduces damage using `raw * 200 / (200 + defense)`. A hero's dodge chance is `0.08 + 0.37 * AGI / (AGI + 1000)`. More agility gives a higher dodge chance, which gets closer to 45%.
7. Weapons and armor lose durability in combat. Markets let heroes buy, sell, and repair items. Selling usually gives half the item's price, adjusted for its durability. We use BigDecimal and rounding rules to calculate gold amounts.
8. Each time the team enters a common tile, there is a 35% chance to meet monsters, a 15% chance to find treasure, and a 50% chance that nothing happens. After finding treasure, there is an 80% chance to get experience and a 20% chance to get gold. The amount also depends on a random reward tier and the team's highest level.
9. The number of monsters depends on the number of heroes in the team. Monster levels depend on the team's highest level and a random level difference.
10. Different monster types have different abilities. For example, goblins can do extra damage, and vampires can recover HP after hitting a hero.
11. The player can choose any living monster as the target of a single-target attack.
12. Every hero can have up to three skill slots. New skills unlock as the hero levels up. Use `K` during exploration to put an unlocked skill into a slot.
13. Market spells can reduce a monster's damage, defense, or dodge chance by 10%. These effects last for three rounds, including the round when the spell is used. Class skills can have different effect times. Attribute potion boosts last until the battle ends.
14. All markets try to add new items after every five team victories. Markets keep the items sold by players when replacing old stock.
15. Heroes act before monsters. Every living hero can take one successful action each round. Viewing information, canceling a choice, and failed actions do not use a turn. At the end of a full round, living heroes recover 5% of their maximum HP and 8% of their maximum MP, rounded up.
16. Only heroes who survive a victory get the full battle rewards. Fallen heroes revive with half of their maximum HP and MP, rounded up, but get no rewards for that battle. When a hero levels up, their HP and MP are filled.
17. Press `T` to view the current statistics. Quitting, restarting, or reaching the end of input saves the statistics and action history in `logs/`. These files cannot be used to load the game. There is no final level to complete, so the player can keep exploring or quit with `Q`.

## Random Events and Probabilities
---------------------------------------------------------------------------

### Map and Exploration

- The map has 15 blocked tiles, 25 markets, and 60 common tiles. Their positions are randomized while keeping all walkable tiles connected. Once the map is generated, each market has a 4% chance of being the starting tile.
- Each time the party enters a common tile, there is a 35% chance of a monster encounter, a 15% chance of treasure, and a 50% chance that nothing happens.
- If the party finds treasure, it has an 80% chance of giving experience and a 20% chance of giving gold. That is a 12% chance of experience and a 3% chance of gold per entry into a common tile. The reward tier is rolled separately, and every hero receives the same reward.

| Treasure tier | Chance after finding treasure | Experience per hero | Gold per hero |
| --- | ---: | ---: | ---: |
| Common | 60% | 3 × party's highest level | 40 × party's highest level |
| Rare | 30% | 8 × party's highest level | 100 × party's highest level |
| High | 10% | 20 × party's highest level | 250 × party's highest level |

### Monster Encounters

Once a battle starts, the party size determines how many monsters can appear. Solo heroes at level 3 or below use a separate distribution.

| Party | 1 monster | 2 monsters | 3 monsters | 4 monsters | 5 monsters |
| --- | ---: | ---: | ---: | ---: | ---: |
| 1 hero, level 3 or below | 60% | 38% | 2% | — | — |
| 1 hero, level 4 or above | 65% | 28% | 7% | — | — |
| 2 heroes | 15% | 50% | 25% | 10% | — |
| 3 heroes | 5% | 15% | 45% | 25% | 10% |

Each monster's level starts from the party's highest level plus a random offset. The resulting level cannot be lower than 1.

| Level offset | −2 | −1 | 0 | +1 | +2 | +3 | +4 | +5 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Chance | 14% | 19% | 40% | 14% | 7% | 3% | 2% | 1% |

Each monster is selected by its weight in [`data/monsters.txt`](data/monsters.txt). With the default data, the chances per monster drawn are 28% Goblin, 22% Exoskeleton, 18% Spirit, 15% Golem, 10% Vampire, and 7% Dragon.

### Combat

Dodge is checked first. Hero critical hits and monster attack abilities are rolled only after a hit lands. The formulas below give probabilities as decimals.

| Event | Chance and effect |
| --- | --- |
| Hero dodge | `0.08 + 0.37 * AGI / (AGI + 1000)`. At 500 AGI, the chance is about 20.3%. |
| Hero critical hit | `min(0.35, 0.03 + LUCK / 1000)` on a hit. A critical hit multiplies raw damage by 1.5. |
| Monster dodge | `min(0.35, 0.04 + dodgeAttribute / 1000 + bonus)`. An active lightning debuff multiplies that chance by 0.9. |
| Spirit dodge bonus | When attacked, a Spirit has a 15% chance to gain 10 percentage points of dodge for that attack, subject to the cap. |
| Dragon attack bonus | On a hit, a Dragon has a 20% chance to multiply raw damage by 1.3. |
| Goblin attack bonus | On a hit, a Goblin has an 18% chance to multiply raw damage by 1.5. |
| Vampire life steal | On a hit, a Vampire has a 25% chance to heal for 35% of the actual damage dealt, rounded up. |
| Monster target selection | Each monster chooses uniformly among living heroes. With three living heroes, each has a 1 in 3 chance of being targeted. |

### Market Stock

- A new market starts with 6 to 10 items, with a 20% chance for each stock size. Each random item has a 25% chance of being a weapon, armor, potion, or spell, and each item definition within that category is equally likely. The game then adjusts stock to guarantee both a health potion and a mana potion.
- After every five victories, each market tries to restock. If it can add or replace stock, the first item is a health potion. If it can restock a second item, there is a 45% chance of choosing a mana potion directly and a 55% chance of drawing a random item. A random item can also be a mana potion.
- For a random restock item, there is a 55% chance to choose uniformly from the party's highest level minus 2 through its highest level (minimum level 1), a 30% chance to use the highest level plus 1, and a 15% chance to use the highest level plus 2. Spells use their own required levels instead.

## Challenges
---------------------------------------------------------------------------

1. The map is generated randomly, but all the places that heroes can walk to need to be connected. We use BFS to check the map after adding a blocked tile. If the tile breaks the connection, we remove it.
2. Different hero classes have different equipment restrictions. We need to check the hero's class, level, and weapon grip before equipping an item. When the main weapon breaks, we also need to move a usable off-hand weapon to the main hand.
3. Every living hero can take one action in each round. Viewing information, canceling a choice, or entering a wrong command should not use the action. We need to move to the next hero only after an action is completed successfully.
4. Different effects last for different amounts of time. Some effects last for several rounds, and potion boosts last until the battle ends. We need to update their remaining time and remove expired effects without changing the hero's basic properties.
5. There can be several items with the same name, but their levels and durability can be different. We use item templates for the shared properties and separate item instances for each item's changing properties.
6. Buying, selling, and repairing items need to update the hero's money and the items correctly. The prices also depend on durability, so we need clear rounding rules. When markets restock, we need to keep the items sold by players.
7. We need to handle heroes who fall during a battle and heroes who survive. Only the surviving heroes get the rewards, and the fallen heroes revive after victory. The same battle should not give rewards more than once.
8. There are many random results in the game, so it can be hard to repeat a problem during testing. We use the same seed and the same actions to repeat the results. Monster numbers and levels also need to consider the team's size and highest level.
9. The game reads data from several text files. Hero classes, item IDs, skills, and equipment rules need to match across these files. Changing one file may affect other parts of the game, so we need to check the related data together.

## How to compile and run
---------------------------------------------------------------------------

### 1. Compile the game
From the project root directory (`MonstersAndHeroes/`):

```bash
javac -d bin -sourcepath src/main src/main/legends/Main.java
```

### 2. Run the game
```bash
java -cp bin legends.Main
```

Optional command-line flags:
- `--seed <integer>`: Run with a specific random seed for deterministic reproduction.
- `--no-color`: Disable ANSI color escape codes if your terminal does not support colors.
- `--data <directory>`: Specify an alternate data directory (defaults to `data`).

Example:
```bash
java -cp bin legends.Main --seed 42 --no-color
```

## Input/Output Example
---------------------------------------------------------------------------

This is a short run with one Warrior. Some menus, map updates, and repeated output are left out. The input lines show what the player types. The output comes from the current game.

Run this command:

```bash
java -cp bin legends.Main --seed 31 --no-color
```

Choose one hero, choose Warrior, and choose Gaerdal_Ironhand:

```text
Input:
1
1
1

Output:
=== Heroes ===
Gaerdal_Ironhand Lv1 HP 125/125 WARRIOR MP 100/100 EXP 7 Gold 1354 Stats {STR=700, DEX=600, AGI=500, LUCK=60, DEF=100}
Main hand: #1 Sword Lv1 price 80 durability 35/35 | damage 52, magic 0, hands 1; off hand: none; grip: SINGLE; armor: none
Attack power 376 | Magic power 450 | Round recovery 7 HP / 8 MP

Victories: 0

Map (# blocked, $ market, . common, @ party)
     1  2  3  4  5  6  7  8  9 10
  1  .  .  $  .  .  .  $  .  .  .
  2  $  .  .  $  .  .  $  $  $  .
  3  $  .  $  .  $  .  $  .  .  .
  4  .  .  .  #  .  .  $  .  $  $
  5  #  #  .  .  .  $  .  .  .  $
  6  #  $  #  .  #  #  #  .  #  .
  7  .  $  .  $  $  .  .  .  .  $
  8  .  .  $  .  @  .  .  #  .  .
  9  .  .  .  #  .  .  .  .  .  .
 10  $  #  .  #  .  .  $  .  #  #

Position (8 , 5) MARKET
Explore (M to enter market) >
```

Move right and then move up. This seed gives a monster encounter:

```text
Input:
D
W

Output:
Monsters appeared!

=== Monsters ===
Iron Colossus Lv1 HP 200/200 damage 28.0 defense 130.0 base dodge 0.045
Goblin Raider Lv1 HP 105/105 damage 25.0 defense 24.0 base dodge 0.065

Victories: 0


=== Round 1 ===
Active hero: Gaerdal_Ironhand (Lv1 WARRIOR)
HP: 125/125 | MP: 100/100
Monsters:
  Iron Colossus Lv1 HP 200/200
  Goblin Raider Lv1 HP 105/105
1 attack, 2 skill, 3 spell, 4 potion, 5 equipment >
```

Choose attack and then choose target 2, Goblin Raider. The game shows the attack, the enemy's turn, and the actual round recovery:

```text
Input:
1
2

Output:
Hero attack: Gaerdal_Ironhand -> Goblin Raider: 105 damage | HP 105 -> 0/105; defeated

=== Enemy turn ===
Enemy attack: Iron Colossus -> Gaerdal_Ironhand: 19 damage | HP 125 -> 106/125
Round recovery: Gaerdal_Ironhand | HP +7 (106 -> 113/125) | MP +0 (100 -> 100/100)

=== Round 2 ===
Active hero: Gaerdal_Ironhand (Lv1 WARRIOR)
HP: 113/125 | MP: 100/100
Monsters:
  Iron Colossus Lv1 HP 200/200
1 attack, 2 skill, 3 spell, 4 potion, 5 equipment >
```

Keep choosing attack and target 1 until the last monster is defeated. The other attack messages are left out here. After victory, the game shows the map again:

```text
Input:
1
1
1
1
1
1

Output:
Victory! Surviving heroes receive full rewards; fainted allies revived.
Map (# blocked, $ market, . common, @ party)
     1  2  3  4  5  6  7  8  9 10
  1  .  .  $  .  .  .  $  .  .  .
  2  $  .  .  $  .  .  $  $  $  .
  3  $  .  $  .  $  .  $  .  .  .
  4  .  .  .  #  .  .  $  .  $  $
  5  #  #  .  .  .  $  .  .  .  $
  6  #  $  #  .  #  #  #  .  #  .
  7  .  $  .  $  $  @  .  .  .  $
  8  .  .  $  .  $  .  .  #  .  .
  9  .  .  .  #  .  .  .  .  .  .
 10  $  #  .  #  .  .  $  .  #  #

Position (7 , 6) COMMON
Explore >
```

Enter `Q` and then `Y` to quit. The game prints the statistics and saves the action history. The report filename changes each time it is saved:

```text
Input:
Q

Output:
Quit? Character progress is not saved; statistics/history will be exported. Y/Q yes, N/0 cancel >

Input:
Y

Output:
Session statistics
Steps: 2 | Encounters: 1
Battles: 1 won, 0 lost, 0 unfinished
Actions: Attack=4  Skill=0  Spell=0  Potion=0  Equipment=0  
Trading: Bought 0 / 0 gold | Sold 0 / 0 gold | Repairs 0 / 0 gold
Hero combat (potion recovery excludes round recovery, leveling and revival)
Hero                     Damage dealt Damage taken  Potion HP  Potion MP
Gaerdal_Ironhand                  305           57          0          0
Hero growth (reward gold excludes starting gold and sales)
Gaerdal_Ironhand | Level: 2 (+1) | EXP: 15 | EXP gained: 8 | Reward gold: 120 | Gold held: 1474
Statistics and action history saved: logs/session-15911121052623728353.txt
Goodbye.
```

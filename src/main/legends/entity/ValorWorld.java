package legends.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import legends.enums.TileType;

/** Stores the Valor board and the current position of each unit. */
public final class ValorWorld {
    private final TileType[][] tiles;
    private final Map<Hero, Position> heroes = new HashMap<>();
    private final Map<Monster, Position> monsters = new HashMap<>();

    public ValorWorld(TileType[][] tiles) {
        this.tiles = new TileType[tiles.length][];
        for(int row = 0; row < tiles.length; row++)
            this.tiles[row] = tiles[row].clone();
    }

    public TileType getTile(int row, int col) {
        return tiles[row][col];
    }

    public boolean placeHero(Hero hero, int row, int col) {
        if(heroes.containsKey(hero) || !ableToEnter(heroes, row, col))
            return false;

        heroes.put(hero, new Position(row, col));
        return true;
    }

    public boolean placeMonster(Monster monster, int row, int col) {
        if(monsters.containsKey(monster) || !ableToEnter(monsters, row, col))
            return false;

        monsters.put(monster, new Position(row, col));

        return true;
    }

    public void removeHero(Hero hero) {
        heroes.remove(hero);
    }

    public void removeMonster(Monster monster) {
        monsters.remove(monster);
    }

    public int getRow(Combatant unit) {
        Position position = positionOf(unit);
        return position == null ? -1 : position.row;
    }

    public int getCol(Combatant unit) {
        Position position = positionOf(unit);
        return position == null ? -1 : position.col;
    }

    public boolean moveHero(Hero hero, int rowChange, int colChange) {
        Position current = heroes.get(hero);
        if (current == null) {
            return false;
        }

        int row = current.row;
        int col = current.col;

        if (colChange == 0) {
            if (rowChange == -1) {
                row--;
            } else if (rowChange == 1) {
                row++;
            } else {
                return false;
            }
        } else if (rowChange == 0) {
            if (colChange == -1) {
                col--;
            } else if (colChange == 1) {
                col++;
            } else {
                return false;
            }
        } else {
            return false;
        }

        if (!ableToEnter(heroes, row, col)) {
            return false;
        }

        heroes.put(hero, new Position(row, col));
        return true;
    }

    public int[] getHeroSpawn(int lane) {
        return null;
    }

    public int[] getMonsterSpawn(int lane) {
        return null;
    }

    public String render() {
        return "";
    }

    public boolean isHeroNexus(int row, int col) {
        return false;
    }

    public boolean isMonsterNexus(int row, int col) {
        return false;
    }

    public List<Monster> getAttackableMonsters(Hero hero) {
        return Collections.emptyList();
    }

    public List<Hero> getAttackableHeroes(Monster monster) {
        return Collections.emptyList();
    }

    public boolean advanceMonster(Monster monster) {
        return false;
    }

    public boolean teleportHero(Hero hero, Hero teammate) {
        return false;
    }

    public boolean recallHero(Hero hero, int row, int col) {
        return false;
    }

    public boolean removeObstacle(Hero hero, int row, int col) {
        return false;
    }

    private Position positionOf(Combatant unit) {
        return unit instanceof Hero ? heroes.get(unit) : monsters.get(unit);
    }

    private boolean ableToEnter(Map<? extends Combatant, Position> allies, int row, int col) {
        if(row < 0 || row >= tiles.length || col < 0 || col >= tiles[row].length)
            return false;

        if(tiles[row][col] == TileType.BLOCKED || tiles[row][col] == TileType.OBSTACLE)
            return false;

        for(Position position : allies.values())
            if(position.row == row && position.col == col)
                return false;

        return true;
    }

    private static final class Position {
        private final int row;
        private final int col;

        private Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}

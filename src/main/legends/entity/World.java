package legends.entity;

import java.util.*;

import legends.enums.TileType;

// The game map tile grid plus market references
public final class World {
    private final int size;
    private final TileType[][] tiles;
    private final Map<Integer, Market> markets;

    public World(TileType[][] tiles, Map<Integer, Market> markets) {
        this.size = tiles.length;
        this.tiles = tiles;
        this.markets = new LinkedHashMap<>(markets);
    }

    public TileType getTile(int row, int col) {
        return tiles[row][col];
    }

    public Market getMarket(Party party) {
        return markets.get(party.getRow() * size + party.getCol());
    }

    public Collection<Market> getMarkets() {
        return Collections.unmodifiableCollection(markets.values());
    }

    public boolean move(Party party, int rowChange, int colChange) {
        int row = party.getRow() + rowChange, col = party.getCol() + colChange;
        if(row < 0 || col < 0 || row >= size || col >= size || tiles[row][col] == TileType.BLOCKED)
            return false;
        party.position(row, col);
        return true;
    }

    public String render(Party party) {
        StringBuilder map = new StringBuilder("Map (# blocked, $ market, . common, @ party)\n   ");
        for(int col = 0; col < size; col++)
            map.append(String.format("%3d", col + 1));
        map.append('\n');
        for(int row = 0; row < size; row++) {
            map.append(String.format("%3d", row + 1));
            for(int col = 0; col < size; col++) {
                String symbol = row == party.getRow() && col == party.getCol() ? "@"
                        : tiles[row][col] == TileType.BLOCKED ? "#" : tiles[row][col] == TileType.MARKET ? "$" : ".";
                map.append("  ").append(symbol);
            }
            map.append('\n');
        }
        return map.toString();
    }

    public int getSize() {
        return size;
    }
}

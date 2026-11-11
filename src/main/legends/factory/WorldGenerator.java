package legends.factory;

import java.util.*;

import legends.entity.Market;
import legends.entity.Party;
import legends.entity.World;
import legends.enums.TileType;
import legends.service.MarketService;

// Generates the map grid ensuring all non-blocked tiles stay connected
public final class WorldGenerator {
    public static boolean connected(TileType[][] tiles) {
        int size = tiles.length, start = -1, openCount = 0;
        
        for(int i = 0; i < size * size; i++) {
            if(tiles[i / size][i % size] != TileType.BLOCKED) {
                openCount++;
                start = i;
            }
        }

        if(start < 0)
            return false;
        Set<Integer> visited = new HashSet<Integer>();
        ArrayDeque<Integer> queue = new ArrayDeque<Integer>();
        queue.add(start);
        visited.add(start);
        while(!queue.isEmpty()) {
            int position = queue.remove();
            for(int[] direction : new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } }) {
                int row = position / size + direction[0];
                int col = position % size + direction[1];
                if(row >= 0 && col >= 0 && row < size && col < size && tiles[row][col] != TileType.BLOCKED && visited.add(row * size + col))
                    queue.add(row * size + col);
            }
        }
        return visited.size() == openCount;
    }

    public World generate(Random random, Party party, MarketService service) {
        int size = 10, blocked = 15;
        TileType[][] tiles = new TileType[size][size];
        List<Integer> positions = new ArrayList<>();
        
        for(int i = 0; i < size * size; i++)
            positions.add(i);
        boolean complete = false;
        
        for(int attempt = 0; attempt < 100 && !complete; attempt++) {
            for(TileType[] row : tiles)
                Arrays.fill(row, TileType.COMMON);
            Collections.shuffle(positions, random);
            int count = 0;
            for(int position : positions) {
                if(count == blocked)
                    break;
                tiles[position / size][position % size] = TileType.BLOCKED;
                if(connected(tiles))
                    count++;
                else
                    tiles[position / size][position % size] = TileType.COMMON;
            }
            complete = count == blocked;
        }
        // fallback layout if random placement can't get enough blocked tiles
        if(!complete) {
            for(int position = 0; position < size * size; position++)
                tiles[position / size][position % size] = position < blocked ? TileType.BLOCKED : TileType.COMMON;
        }
        
        List<Integer> openPositions = new ArrayList<>();
        for(int position = 0; position < size * size; position++)
            if(tiles[position / size][position % size] != TileType.BLOCKED)
                openPositions.add(position);
        
        Collections.shuffle(openPositions, random);
        Map<Integer, Market> markets = new LinkedHashMap<>();
        Set<String> signatures = new HashSet<>();
        
        for(int i = 0; i < 25; i++) {
            int position = openPositions.get(i);
            tiles[position / size][position % size] = TileType.MARKET;
            Market market = service.generate();
            service.distinguish(market, signatures);
            markets.put(position, market);
        }
        
        int spawn = openPositions.get(random.nextInt(25));
        party.position(spawn / size, spawn % size);
        return new World(tiles, markets);
    }
}

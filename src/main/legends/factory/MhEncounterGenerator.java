package legends.factory;

import java.util.*;

import legends.config.GameConfig;
import legends.entity.Monster;
import legends.entity.Party;
import legends.utils.RandomWeights;

// Creates monster groups scaled to the party
public final class MhEncounterGenerator {
    public List<Monster> generate(GameConfig config, Random random, Party party) {
        int highestLevel = party.getHighestLevel();
        int[][] countWeights = { { 65, 28, 7 }, { 15, 50, 25, 10 }, { 5, 15, 45, 25, 10 } };
        int[] distribution = party.getHeroes().size() == 1 && highestLevel <= 3
                ? new int[] { 60, 38, 2 }
                : countWeights[party.getHeroes().size() - 1];
        
        int count = 1 + RandomWeights.pick(random, distribution);
        List<Monster> result = new ArrayList<>();
        int[] weights = new int[config.getMonsterTtos().size()];
        int[] levelOffsets = { -2, -1, 0, 1, 2, 3, 4, 5 };
        int[] levelWeights = { 14, 19, 40, 14, 7, 3, 2, 1 };
        
        for(int i = 0; i < weights.length; i++)
            weights[i] = config.getMonsterTtos().get(i).getWeight();
        
        for(int i = 0; i < count; i++) {
            int level = Math.max(1, highestLevel + levelOffsets[RandomWeights.pick(random, levelWeights)]);
            result.add(new Monster(config.getMonsterTtos().get(RandomWeights.pick(random, weights)), level));
        }
        return result;
    }
}

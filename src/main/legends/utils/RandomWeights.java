package legends.utils;

import java.util.Random;

public final class RandomWeights {
    private RandomWeights() {
    }

    public static int pick(Random random, int[] weights) {
        int total = 0;
        for(int weight : weights)
            total += weight;
        int roll = random.nextInt(total);
        for(int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if(roll < 0)
                return i;
        }
        throw new IllegalArgumentException("Empty weights");
    }
}

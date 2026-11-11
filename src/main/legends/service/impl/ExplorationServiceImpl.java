package legends.service.impl;

import legends.entity.Battle;
import legends.entity.GameSession;
import legends.entity.Hero;
import legends.enums.TileType;
import legends.factory.EncounterGenerator;
import legends.service.ExplorationService;
import legends.utils.RandomWeights;

public class ExplorationServiceImpl implements ExplorationService {
    @Override
    public String entered(GameSession session) {
        if(session.getWorld().getTile(session.getParty().getRow(), session.getParty().getCol()) != TileType.COMMON)
            return "";
        double roll = session.getRandom().nextDouble();
        if(roll < 0.35) {
            session.setBattle(new Battle(session.getParty(), new EncounterGenerator().generate(session.getConfig(), session.getRandom(), session.getParty()), session.getStatistics()));
            return "Monsters appeared!";
        }
        if(roll >= 0.35 + 0.15)
            return "Nothing happened.";

        int level = session.getParty().getHighestLevel();
        boolean experience = session.getRandom().nextDouble() < 0.80;
        int tier = RandomWeights.pick(session.getRandom(), new int[] { 60, 30, 10 });
        int[] rewards = experience ? new int[] { 3, 8, 20 } : new int[] { 40, 100, 250 };
        long reward = (long) level * rewards[tier];
        for(Hero hero : session.getParty().getHeroes()) {
            if(experience)
                hero.gainExperience(reward);
            else
                hero.credit(reward);
            session.getStatistics().reward(hero, experience ? 0 : reward, experience ? reward : 0);
        }
        return "Treasure: each hero receives " + reward + (experience ? " EXP" : " gold");
    }
}

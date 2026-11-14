package legends.service;

import legends.entity.MhGameSession;

//Defines the resolution of events when the party enters a world tile.
public interface ExplorationService {
    /**
     * Resolves a common-tile entry by starting a battle, granting treasure, or reporting no event; returns
     * an empty string on other tiles.
     */
    String entered(MhGameSession session);
}

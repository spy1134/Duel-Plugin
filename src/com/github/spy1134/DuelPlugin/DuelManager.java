package com.github.spy1134.DuelPlugin;

import java.util.ArrayList;
import org.bukkit.entity.Player;

public class DuelManager {

    private final ArrayList<Duel> duels;

    public DuelManager() {
        duels = new ArrayList<>();
    }

    public ArrayList<Duel> getDuels() {
        return duels;
    }

    public void addDuel(Duel duel) {
        duels.add(duel);
    }

    public void removeDuel(Duel duel) {
        duels.remove(duel);
    }

    // Get the duel this player is involved in.
    // Returns null if there isn't any duel the player is involved in.
    public Duel getDuel(Player player) {
        for (Duel duel : duels) {
            if (duel.isPlayerInvolved(player)) {
                return duel;
            }
        }
        return null;
    }

    public boolean isPlayerDueling(Player player) {
        for (Duel duel : duels) {
            if (duel.isPlayerInvolved(player)) {
                return true;
            }
        }
        return false;
    }
}

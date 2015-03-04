package com.github.spy1134.duelplugin;

import org.bukkit.entity.Player;

public class Duel {

    private final Player challenger, challenged;

    public Duel(Player challenger, Player challenged) {
        this.challenger = challenger;
        this.challenged = challenged;
    }

    public Player getChallenger() {
        return challenger;
    }

    public Player getChallenged() {
        return challenged;
    }

    // When given a player, involved in this duel, this function
    // returns the other player involved in the duel.
    // If this player is not involved in this duel, null will be returned.
    public Player getOpponent(Player player) {
        if (player.getUniqueId() == challenger.getUniqueId()) {
            return challenged;
        } else if (player.getUniqueId() == challenged.getUniqueId()) {
            return challenger;
        } else {
            return null;
        }
    }

    public boolean isPlayerInvolved(Player player) {
        if (challenger.getUniqueId() == player.getUniqueId()) {
            return true;
        }

        if (challenged.getUniqueId() == player.getUniqueId()) {
            return true;
        }

        return false;
    }
}

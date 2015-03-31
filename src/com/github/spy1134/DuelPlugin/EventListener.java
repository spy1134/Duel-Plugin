package com.github.spy1134.DuelPlugin;

import static java.lang.Math.floor;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EventListener implements Listener {

    private final Plugin plugin;

    // Takes a reference to the duel manager, the cube representing the arena,
    // A location for the lobby to return players to after the fight, and a boolean
    // that lets the plugin know whether it should notify players with the admin
    // permission that the plugin needs to be configured.
    public EventListener(Plugin parent) {
        plugin = parent;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.configured) {
            return;
        }

        Player dead = event.getEntity();
        if (plugin.duelManager.isPlayerDueling(dead)) {
            Duel duel = plugin.duelManager.getDuel(dead);
            Player killer = dead.getKiller();
            Player opponent = duel.getOpponent(dead);

            // Get whether the dead player was the challenger.
            // This could be used for more fun death messages
            //boolean isChallenger = (dead.getUniqueId() == duel.getChallenger().getUniqueId());
            // If there was no killer associated with the event
            // then assume that the player killed themself.
            if (killer == null) {
                killer = dead;
            }

            // The player who killed this dueler wasn't a participant.
            if (!duel.isPlayerInvolved(killer)) {
                // Kick em!
                killer.kickPlayer("Don't intervene in duels!");
                plugin.getServer().broadcast(killer.getDisplayName() + " was kicked for intervening in a duel!", "duel.admin");
            }

            // Maintain items on death
            /*
             if (plugin.keepInventory) {
             event.setKeepInventory(true);
             }
             */
            PlayerInventory killerInv = killer.getInventory();
            PlayerInventory opponentInv = opponent.getInventory();
            killerInv.setHelmet(new ItemStack(Material.IRON_HELMET, 1));
            killerInv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
            killerInv.setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
            killerInv.setBoots(new ItemStack(Material.IRON_BOOTS, 1));
            opponentInv.setHelmet(new ItemStack(Material.IRON_HELMET, 1));
            opponentInv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
            opponentInv.setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
            opponentInv.setBoots(new ItemStack(Material.IRON_BOOTS, 1));

            // Maintain experience on death
            if (plugin.keepXP) {
                event.setKeepLevel(true);
            }

            // Show all players again.
            for (Player arenaOccupant : plugin.duelArea.getPosOne().getWorld().getPlayers()) {
                dead.showPlayer(arenaOccupant);
                killer.showPlayer(arenaOccupant);
            }

            // If the player did not kill themself
            if (killer != dead) {
                // Display loss message.
                dead.sendMessage(ChatColor.GOLD + "You were defeated by " + killer.getDisplayName()
                        + "!\nThey had %" + floor((killer.getHealth() / killer.getMaxHealth()) * 100)
                        + " of their health left. (" + (killer.getHealth() / 2) + " hearts)");

                // Display win message.
                killer.sendMessage(ChatColor.GOLD + "You defeated " + dead.getDisplayName() + "!");

                // Broadcast duel message for this death
                //event.setDeathMessage(killer.getDisplayName() + " defeated " + dead.getDisplayName() + " in a duel!");
            } else {
                // Send a messge to the player that killed themself
                dead.sendMessage(ChatColor.GOLD + "Are you even trying?");

                // Send a message to the opponent.
                opponent.sendMessage(ChatColor.GOLD + "Your oponent killed themselves! You win!");
                opponent.setHealth(opponent.getMaxHealth());

                // Set a suicide death message
                //event.setDeathMessage(dead.getDisplayName() + " killed themselves while dueling " + opponent.getDisplayName());
            }

            // Refill the health of the combatants
            dead.setHealth(dead.getMaxHealth());
            opponent.setHealth(opponent.getMaxHealth());

            // Preload the chunk to prevent glitches
            Chunk lobbyChunk = plugin.lobbyLoc.getChunk();
            if (!(lobbyChunk.isLoaded())) {
                lobbyChunk.load();
            }

            // Teleport the combatants to the lobby
            dead.teleport(plugin.lobbyLoc);
            opponent.teleport(plugin.lobbyLoc);

            // Remove this duel from the manager.
            plugin.duelManager.removeDuel(duel);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.configured == false
                && player.hasPermission("duel.admin")) {
            player.sendMessage(ChatColor.RED + "The duel plugin must be set up!");
        }
    }
}

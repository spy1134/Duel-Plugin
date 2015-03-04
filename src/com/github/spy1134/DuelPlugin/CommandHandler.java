package com.github.spy1134.DuelPlugin;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private final Plugin plugin;

    public CommandHandler(Plugin parent) {
        plugin = parent;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // We caught a command we shouldn't have.
        // Someone has been tampering with the plugin yml.
        if (!(cmd.getName().equalsIgnoreCase("duel"))) {
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(
                        "Duel Plugin help:\n"
                        + "/duel <NAME> - Challenge another player to a duel\n"
                        + "/duel accept <NAME> - Accept a duel request from another player\n"
                        + "/duel deny <NAME> - Deny a duel request from another player\n");
                if (sender.hasPermission("duel.admin")) {
                    sender.sendMessage(
                            "Admin commands:\n"
                            + "/duel setpos1 - Set the position for the first dueler\n"
                            + "/duel setpos2 - Set the position for the second dueler\n"
                            + "/duel setlobby - Set the arena lobby");
                }
            } else if (args[0].equalsIgnoreCase("accept")) {
                if (!plugin.configured) {
                    sender.sendMessage(ChatColor.RED + "The duel plugin isn't set up yet!");
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                    return true;
                }
                Player player = (Player) sender;

                if (!(args.length > 1)) {
                    player.sendMessage(ChatColor.RED + "You must specify part of a player name!");
                    return true;
                }

                if (plugin.requests.containsKey(player)) {
                    ArrayList<Player> challengers = plugin.getRequests(player);
                    // Search through all the pending requests for this player
                    for (Player challenger : challengers) {
                        // If we have a match
                        if (challenger.getDisplayName().contains(args[1])) {
                            // If the player is already in a duel
                            if (plugin.duelManager.isPlayerDueling(challenger)) {
                                player.sendMessage(ChatColor.RED + "Please try later! That player is already dueling!");
                                return true;
                            }

                            challengers.remove(challenger);
                            plugin.requests.put(player, challengers);
                            startDuel(player, challenger);
                            return true;
                        }
                    }
                    player.sendMessage(ChatColor.RED + "Could not find a challenger with that name!");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You have no pending duel requests!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("deny")) {
                if (!(plugin.configured)) {
                    sender.sendMessage(ChatColor.RED + "The duel plugin isn't set up yet!");
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                    return true;
                }
                Player player = (Player) sender;

                ArrayList<Player> requests = plugin.getRequests(player);
                if (requests.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "You have no pending duel requests!");
                    return true;
                }

                // If they supplied a specific player to deny.
                if (args.length > 1) {
                    for (Player challenger : requests) {
                        if (challenger.getDisplayName().contains(args[1])) {
                            requests.remove(challenger);
                            challenger.sendMessage(ChatColor.RED + player.getDisplayName() + " denied your duel request!");
                            player.sendMessage(ChatColor.GREEN + "Request denied!");
                            break;
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.GREEN + "Denying all requests...");
                    // Send deny messages to all challengers.
                    for (Player challenger : requests) {
                        challenger.sendMessage(ChatColor.RED + player.getDisplayName() + " denied your duel request!");
                    }
                    requests = new ArrayList<>();
                }
                // Save the updated requests list for the player.
                plugin.requests.put(player, requests);
            } else if (args[0].equalsIgnoreCase("setlobby")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use that command!");
                    return true;
                }

                if (!(sender.hasPermission("duel.admin"))) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    return true;
                }
                Player player = (Player) sender;
                Location playerLoc = player.getLocation();

                plugin.lobbyLoc = playerLoc;
                player.sendMessage(ChatColor.GREEN + "Lobby location updated!");
                checkConfig();
            } else if (args[0].equalsIgnoreCase("setpos1")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use that command!");
                    return true;
                }

                if (!(sender.hasPermission("duel.admin"))) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    return true;
                }
                Player player = (Player) sender;
                Location playerLoc = player.getLocation();

                if (plugin.duelArea != null) {
                    plugin.duelArea.setPosOne(playerLoc);
                    checkConfig();
                } else {
                    plugin.duelArea = new Arena(playerLoc, null);
                    player.sendMessage(ChatColor.YELLOW + "Position 2 still needs to be set!");
                }
                player.sendMessage(ChatColor.GREEN + "Position one updated!");
                return true;
            } else if (args[0].equalsIgnoreCase("setpos2")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use that command!");
                    return true;
                }

                if (!(sender.hasPermission("duel.admin"))) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    return true;
                }
                Player player = (Player) sender;
                Location playerLoc = player.getLocation();

                if (plugin.duelArea != null) {
                    plugin.duelArea.setPosTwo(playerLoc);
                    checkConfig();
                } else {
                    plugin.duelArea = new Arena(null, playerLoc);
                    player.sendMessage(ChatColor.YELLOW + "Position 1 still needs to be set!");
                }
                player.sendMessage(ChatColor.GREEN + "Position two updated!");
            } else {
                if (!plugin.configured) {
                    sender.sendMessage(ChatColor.RED + "The duel plugin isn't set up yet!");
                    return true;
                }

                // This might be a player to challenge.
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to challenge others to a duel!");
                    return true;
                }
                Player challenger = (Player) sender;

                // If the requester is currently dueling
                if (plugin.duelManager.isPlayerDueling(challenger)) {
                    challenger.sendMessage(ChatColor.RED + "You're already dueling! Get back to the fight!");
                    return true;
                }

                ArrayList<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
                for (Player player : players) {
                    // If we found a match
                    if (player.getDisplayName().contains(args[0])) {
                        // If the match is the player who searched
                        if (player == challenger) {
                            challenger.sendMessage(ChatColor.RED + "You can't challenge yourself to a duel!");
                            return true;
                        }

                        ArrayList<Player> requests = plugin.getRequests(player);
                        if (requests.contains(challenger)) {
                            challenger.sendMessage(ChatColor.RED + "There is already a pending challenge for this player!");
                            return true;
                        }

                        player.sendMessage(ChatColor.GREEN + challenger.getDisplayName()
                                + " has challenged you to a duel!\n"
                                + "/duel accept <NAME> or /duel deny <NAME>");
                        requests.add(challenger);
                        plugin.requests.put(player, requests);
                        challenger.sendMessage(ChatColor.GREEN + "Challenge sent!");
                    }
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must specify an argument!");
            return false;
        }
        return true;
    }

    public void startDuel(Player challenged, Player challenger) {
        for (Player player : plugin.duelArea.getPosOne().getWorld().getPlayers()) {
            challenged.hidePlayer(player);
            challenger.hidePlayer(player);
        }
        Chunk arenaChunk = plugin.duelArea.getPosOne().getChunk();

        if (!(arenaChunk.isLoaded())) {
            arenaChunk.load();
        }

        challenged.setHealth(challenged.getMaxHealth());
        challenged.teleport(plugin.duelArea.getPosOne());
        challenger.setHealth(challenger.getMaxHealth());
        challenger.teleport(plugin.duelArea.getPosTwo());

        challenged.sendMessage("Fight!");
        challenger.sendMessage("Fight!");
        challenged.showPlayer(challenger);
        challenger.showPlayer(challenged);
        plugin.duelManager.addDuel(new Duel(challenger, challenged));
    }

    public boolean checkConfig() {
        if ((plugin.configured)) {
            return false;
        }

        if (plugin.lobbyLoc == null) {
            return false;
        }

        if (plugin.duelArea == null) {
            return false;
        }

        if (plugin.duelArea.getPosOne() == null) {
            return false;
        }

        if (plugin.duelArea.getPosTwo() == null) {
            return false;
        }

        plugin.configured = true;
        return true;
    }
}

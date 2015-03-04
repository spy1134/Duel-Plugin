package com.github.spy1134.duelplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    String PLUGIN_NAME;
    String CONFIG_PATH;
    FileConfiguration config;

    boolean keepInventory;
    boolean keepXP;
    boolean configured = false;
    DuelManager duelManager;
    Arena duelArea;
    Location lobbyLoc;

    HashMap<Player, ArrayList<Player>> requests;

    @Override
    public void onEnable() {
        PLUGIN_NAME = getDescription().getFullName();
        CONFIG_PATH = "plugins/" + getDescription().getName();
        loadConfig();
        getCommand("duel").setExecutor(new CommandHandler(this));
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        requests = new HashMap<>();
        getLogger().log(Level.INFO, "{0} enabled!", PLUGIN_NAME);
    }

    @Override
    public void onDisable() {
        saveConfig();
        getServer().broadcastMessage(ChatColor.RED + "Duel plugin is being disabled! Requests and duels will be forgotten!");
        getLogger().log(Level.INFO, "{0} disabled!", PLUGIN_NAME);
    }

    public void loadConfig() {
        // Load configuration.
        config = getConfig();
        config.options().copyDefaults(true);

        // Defaults
        config.addDefault("keepInventoryOnDeath", true);
        config.addDefault("keepExperienceOnDeath", true);

        // Initialize to fail safe values
        keepInventory = config.getBoolean("keepInventoryOnDeath");
        keepXP = config.getBoolean("keepExperienceOnDeath");
        duelManager = new DuelManager();
        duelArea = null;
        lobbyLoc = getServer().getWorlds().get(0).getSpawnLocation();
        configured = false;

        // Check if configuration values have been set.
        if (!config.isSet("lobby.world")) {
            getLogger().info("Configuration is not set up!");
            return;
        }

        String lobWorldName;
        double lobX, lobY, lobZ, lobYaw;
        World lobbyWorld;
        lobWorldName = config.getString("lobby.world");
        lobX = config.getDouble("lobby.xPos");
        lobY = config.getDouble("lobby.yPos");
        lobZ = config.getDouble("lobby.zPos");
        lobYaw = config.getDouble("lobby.yaw");
        lobbyWorld = getServer().getWorld(lobWorldName);
        if (lobbyWorld == null) {
            getLogger().log(Level.SEVERE, "Unable to get lobby world by name in config!\nWorld name: {0}", lobWorldName);
            return;
        }
        lobbyLoc = new Location(lobbyWorld, lobX, lobY, lobZ, ((float) lobYaw), 0);

        // Cube location and dimensions
        String arenaWorldName;
        double oneX, oneY, oneZ, oneYaw, twoX, twoY, twoZ, twoYaw;
        arenaWorldName = config.getString("arena.world");
        oneX = config.getDouble("arena.posOne.xPos");
        oneY = config.getDouble("arena.posOne.yPos");
        oneZ = config.getDouble("arena.posOne.zPos");
        oneYaw = config.getDouble("arena.posOne.yaw");
        twoX = config.getDouble("arena.posTwo.xPos");
        twoY = config.getDouble("arena.posTwo.yPos");
        twoZ = config.getDouble("arena.posTwo.zPos");
        twoYaw = config.getDouble("arena.posTwo.yaw");
        World arenaWorld = getServer().getWorld(arenaWorldName);
        if (arenaWorld == null) {
            getLogger().log(Level.SEVERE, "Unable to get arena world name from config!");
        }
        Location one = new Location(arenaWorld, oneX, oneY, oneZ, ((float) oneYaw), 0.0f);
        Location two = new Location(arenaWorld, twoX, twoY, twoZ, ((float) twoYaw), 0.0f);
        duelArea = new Arena(one, two);

        configured = true;
        getLogger().log(Level.INFO, "Configuration loaded!");
    }

    @Override
    public void saveConfig() {
        // Lobby
        if (lobbyLoc != null) {
            config.set("lobby.world", lobbyLoc.getWorld().getName());
            config.set("lobby.xPos", lobbyLoc.getX());
            config.set("lobby.yPos", lobbyLoc.getY());
            config.set("lobby.zPos", lobbyLoc.getZ());
            config.set("lobby.yaw", lobbyLoc.getYaw());
        } else {
            getLogger().log(Level.INFO, "No lobby location! Saving spawn...");
            // Save the spawn location from the first world
            Location worldSpawn = getServer().getWorlds().get(0).getSpawnLocation();
            config.set("lobby.world", worldSpawn.getWorld().getName());
            config.set("lobby.xPos", worldSpawn.getX());
            config.set("lobby.yPos", worldSpawn.getY());
            config.set("lobby.zPos", worldSpawn.getZ());
        }

        // Arena
        if (duelArea != null) {
            Location posOne = duelArea.getPosOne();
            Location posTwo = duelArea.getPosTwo();
            config.set("arena.world", duelArea.getPosOne().getWorld().getName());
            config.set("arena.posOne.xPos", posOne.getX());
            config.set("arena.posOne.yPos", posOne.getY());
            config.set("arena.posOne.zPos", posOne.getZ());
            config.set("arena.posOne.yaw", posOne.getYaw());
            config.set("arena.posTwo.xPos", posTwo.getX());
            config.set("arena.posTwo.yPos", posTwo.getY());
            config.set("arena.posTwo.zPos", posTwo.getZ());
            config.set("arena.posTwo.yaw", posTwo.getYaw());
        } else {
            // Save the spawn location from the first world
            Location worldSpawn = getServer().getWorlds().get(0).getSpawnLocation();
            config.set("arena.world", worldSpawn.getWorld().getName());
            config.set("arena.posOne.xPos", worldSpawn.getX());
            config.set("arena.posOne.yPos", worldSpawn.getY());
            config.set("arena.posOne.zPos", worldSpawn.getZ());
            config.set("arena.posTwo.xPos", worldSpawn.getX());
            config.set("arena.posTwo.yPos", worldSpawn.getY());
            config.set("arena.posTwo.zPos", worldSpawn.getZ());
        }

        try {
            config.save(CONFIG_PATH + "/config.yml");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "FAILED TO SAVE CONFIGURATION!!!");
        }
    }

    // Get all pending duel requests for the given player.
    // If there aren't any requests, an empty list is returned.
    public ArrayList<Player> getRequests(Player player) {
        if (requests.containsKey(player)) {
            return requests.get(player);
        } else {
            return new ArrayList<>();
        }
    }
}

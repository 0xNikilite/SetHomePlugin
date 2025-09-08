package xyz.nikilite.sethomeplugin;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SetHomePlugin extends JavaPlugin {

    private File homesFile;
    private FileConfiguration homesConfig;

    @Override
    public void onEnable() {
        getLogger().info("SetHomePlugin has been enabled!");

        // Create homes file
        homesFile = new File(getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            homesFile.getParentFile().mkdirs();
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create homes.yml!");
            }
        }

        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
    }

    @Override
    public void onDisable() {
        getLogger().info("SetHomePlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        String playerId = player.getUniqueId().toString();

        switch (command.getName().toLowerCase()) {
            case "sethome":
                return setHome(player, playerId);
            case "home":
                return goHome(player, playerId);
            case "delhome":
                return deleteHome(player, playerId);
        }
        return false;
    }

    private boolean setHome(Player player, String playerId) {
        Location location = player.getLocation();

        // Save home location
        homesConfig.set(playerId + ".world", location.getWorld().getName());
        homesConfig.set(playerId + ".x", location.getX());
        homesConfig.set(playerId + ".y", location.getY());
        homesConfig.set(playerId + ".z", location.getZ());
        homesConfig.set(playerId + ".yaw", location.getYaw());
        homesConfig.set(playerId + ".pitch", location.getPitch());

        try {
            homesConfig.save(homesFile);
            player.sendMessage("§aHome set successfully!");
            getLogger().info("Home set for player: " + player.getName());
        } catch (IOException e) {
            player.sendMessage("§cFailed to set home!");
            getLogger().severe("Failed to save home for player: " + player.getName());
        }

        return true;
    }

    private boolean goHome(Player player, String playerId) {
        if (!homesConfig.contains(playerId)) {
            player.sendMessage("§cYou haven't set a home yet! Use /sethome first.");
            return true;
        }

        try {
            String world = homesConfig.getString(playerId + ".world");
            double x = homesConfig.getDouble(playerId + ".x");
            double y = homesConfig.getDouble(playerId + ".y");
            double z = homesConfig.getDouble(playerId + ".z");
            float yaw = (float) homesConfig.getDouble(playerId + ".yaw");
            float pitch = (float) homesConfig.getDouble(playerId + ".pitch");

            Location homeLocation = new Location(getServer().getWorld(world), x, y, z, yaw, pitch);
            player.teleport(homeLocation);
            player.sendMessage("§aTeleported to your home!");
            getLogger().info("Player " + player.getName() + " teleported to home");
        } catch (Exception e) {
            player.sendMessage("§cFailed to teleport to home!");
            getLogger().severe("Failed to teleport player " + player.getName() + " to home");
        }

        return true;
    }

    private boolean deleteHome(Player player, String playerId) {
        if (!homesConfig.contains(playerId)) {
            player.sendMessage("§cYou don't have a home set!");
            return true;
        }

        homesConfig.set(playerId, null);

        try {
            homesConfig.save(homesFile);
            player.sendMessage("§aHome deleted successfully!");
            getLogger().info("Home deleted for player: " + player.getName());
        } catch (IOException e) {
            player.sendMessage("§cFailed to delete home!");
            getLogger().severe("Failed to delete home for player: " + player.getName());
        }

        return true;
    }
}

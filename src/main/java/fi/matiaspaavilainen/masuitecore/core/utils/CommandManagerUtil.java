package fi.matiaspaavilainen.masuitecore.core.utils;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import fi.matiaspaavilainen.masuitecore.bukkit.BukkitCooldownManager;
import fi.matiaspaavilainen.masuitecore.bukkit.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.core.models.MaSuitePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class CommandManagerUtil {

    /**
     * Registers {@link MaSuitePlayer} command completion for {@link PaperCommandManager}
     *
     * @param manager manager to use
     */
    public static void registerMaSuitePlayerCommandCompletion(PaperCommandManager manager) {
        manager.getCommandCompletions().registerCompletion("masuite_players", c -> MaSuiteCore.onlinePlayers);
    }


    /**
     * Register a cooldown condition for plugins to use
     *
     * @param manager manager to use
     */
    public void registerCooldownCondition(PaperCommandManager manager) {
        manager.getCommandConditions().addCondition("cooldown", c -> {
            BukkitCooldownManager cooldownManager = MaSuiteCore.cooldownManager;
            UUID uuid = c.getIssuer().getUniqueId();

            String cooldownType = c.getConfigValue("type", "");
            String byPassPermission = c.getConfigValue("bypass", "masuitecore.cooldown.bypass");

            if (!c.getIssuer().hasPermission(byPassPermission) && cooldownManager.hasCooldown(cooldownType, uuid)) {
                // TODO: Switch to config
                throw new ConditionFailedException("You are in cooldown.");
            }
        });
    }


    /**
     * Registers {@link Location} command context for {@link PaperCommandManager}
     *
     * @param manager manager to use
     */
    public static void registerLocationContext(PaperCommandManager manager) {
        manager.getCommandContexts().registerContext(Location.class, c -> {
            String worldName = c.popFirstArg();
            String stringX = c.popFirstArg();
            String stringY = c.popFirstArg();
            String stringZ = c.popFirstArg();
            if (worldName == null || stringX == null || stringY == null || stringZ == null) {
                throw new InvalidCommandArgument("Not enough parameter found.");
            }

            World bukkitWorld = Bukkit.getWorld(worldName);
            if (bukkitWorld == null) throw new InvalidCommandArgument(worldName + " is no valid world.");

            double x, y, z;
            try {
                x = Double.parseDouble(parseCoordinate(stringX, c.getPlayer().getLocation().getX()));
                y = Double.parseDouble(parseCoordinate(stringY, c.getPlayer().getLocation().getY()));
                z = Double.parseDouble(parseCoordinate(stringZ, c.getPlayer().getLocation().getZ()));
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Coordinates are invalid");
            }
            return new Location(bukkitWorld, x, y, z);
        });
    }

    private static String parseCoordinate(String coordinate, double currentCoordinate) {
        if (coordinate.startsWith("~")) {
            String parsedCoordinate = coordinate.replace("~", "");
            return !parsedCoordinate.isEmpty() ? parsedCoordinate + currentCoordinate : currentCoordinate + "";
        }
        return coordinate;
    }


}

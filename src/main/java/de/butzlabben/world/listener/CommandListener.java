package de.butzlabben.world.listener;

import de.butzlabben.world.config.MessageConfig;
import de.butzlabben.world.config.PluginConfig;
import de.butzlabben.world.config.WorldConfig;
import de.butzlabben.world.util.PlayerPositions;
import de.butzlabben.world.wrapper.SystemWorld;
import de.butzlabben.world.wrapper.WorldPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class CommandListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        World from = e.getFrom().getWorld();
        World to = e.getTo().getWorld();
        boolean fromIsSystemWorld = WorldConfig.exists(from.getName());
        boolean toIsSystemWorld = WorldConfig.exists(to.getName());

        if (!(from.equals(to)))
            SystemWorld.tryUnloadLater(from);

        WorldPlayer wpTo = new WorldPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), to.getName());

        if (e.getCause() == TeleportCause.SPECTATE) {
            if (checkTeleportPermissions(e, p, from, to, toIsSystemWorld, wpTo)) return;
        } else if (e.getCause() == TeleportCause.COMMAND) {
            if (checkTeleportPermissions(e, p, from, to, toIsSystemWorld, wpTo)) return;
        }

        // Fix for #18
        if (fromIsSystemWorld) {
            // Save location for #23
            WorldConfig config = WorldConfig.getWorldConfig(from.getName());
            PlayerPositions.instance.saveWorldsPlayerLocation(p, config);
            GameMode gameMode = PluginConfig.getSpawnGamemode();
            if (toIsSystemWorld) {
                if (PluginConfig.isSurvival()) {
                    gameMode = GameMode.SURVIVAL;
                } else {
                    gameMode = GameMode.CREATIVE;
                }
            }

            p.setGameMode(gameMode);
        }
    }

    private boolean checkTeleportPermissions(PlayerTeleportEvent e, Player p, World from, World to, boolean toIsSystemWorld, WorldPlayer wpto) {
        if(!wpto.isOnSystemWorld())
            return false;

        //close event
        if(!p.hasPermission("ws.tp.toother") && !wpto.isOwnerofWorld() && !wpto.canTeleport()){
            p.sendMessage(MessageConfig.getNoPermission());
            e.setCancelled(true);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCmd(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage().toLowerCase();
        Player p = e.getPlayer();
        WorldPlayer wp = new WorldPlayer(p);

        if (!wp.isOnSystemWorld())
            return;

        if (cmd.startsWith("/gamemode") || cmd.startsWith("/gm")) {
            if (!wp.isOnSystemWorld())
                return;
            if (p.hasPermission("ws.gamemode"))
                return;
            if (PluginConfig.isSurvival()) {
                e.setCancelled(true);
                p.sendMessage(MessageConfig.getNoPermission());
                return;
            }

            if (!wp.canChangeGamemode() && !wp.isOwnerofWorld()) {
                p.sendMessage(MessageConfig.getNoPermission());
                e.setCancelled(true);
            }
        } else if (cmd.startsWith("/tp") || cmd.startsWith("/teleport")) {
            String[] args = e.getMessage().split(" ");
            if (args.length == 2) {
                if (p.hasPermission("ws.tp.toother"))
                    return;
                if (PluginConfig.isSurvival()) {
                    e.setCancelled(true);
                    p.sendMessage(MessageConfig.getNoPermission());
                    return;
                }
                Player a = Bukkit.getPlayer(args[1]);
                if (a == null)
                    return;
                if (!(p.getWorld().equals(a.getWorld()))) {
                    e.setCancelled(true);
                    p.sendMessage(MessageConfig.getNoPermission());
                    return;
                }
                if (wp.isOwnerofWorld())
                    return;
                if (!wp.canTeleport()) {
                    p.sendMessage(MessageConfig.getNoPermission());
                    e.setCancelled(true);
                }
            } else if (args.length == 3) {
                if (!p.hasPermission("ws.tp.other")) {
                    p.sendMessage(MessageConfig.getNoPermission());
                    e.setCancelled(true);
                }

            } else if (args.length == 4) {
                if (p.hasPermission("ws.tp.toother"))
                    return;
                if (PluginConfig.isSurvival()) {
                    e.setCancelled(true);
                    p.sendMessage(MessageConfig.getNoPermission());
                    return;
                }
                if (wp.isOwnerofWorld())
                    return;
                if (!wp.canTeleport()) {
                    p.sendMessage(MessageConfig.getNoPermission());
                    e.setCancelled(true);
                }
            }
        }
    }
}

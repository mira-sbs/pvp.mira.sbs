package au.edu.swin.war.util;

import au.edu.swin.war.WarPlayerPlus;
import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.event.MatchEndEvent;
import au.edu.swin.war.framework.event.MatchPlayerDeathEvent;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.framework.util.WarMatch;
import au.edu.swin.war.framework.util.WarModule;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.stats.WarStats;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class handles player joins, statistics
 * manipulation, and other database-related things.
 *
 * @author ILavaYou
 * @version 1.0
 * @since 1.1
 */
public class StatsListener extends WarModule implements Listener {

    StatsListener(WarManager main) {
        super(main);
        main().plugin().getServer().getPluginManager().registerEvents(this, main().plugin());
    }

    /**
     * This event procedure handles pre-login
     * logic for statistics generation.
     *
     * @param event An event called by spigot.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return; // Don't create anything in the database if they can't get on.
        try {

            // Checks if the player has stats recorded already.
            PreparedStatement stats = ((Manager) main()).query().prepare("SELECT * FROM `WarStats` WHERE `player_uuid`=?");
            stats.setString(1, event.getUniqueId().toString());
            ResultSet check = stats.executeQuery(); // Execute the check and get our result.

            if (check.next()) {
                main().plugin().log(event.getName() + " had previous stats, retrieving...");
                ((Manager) main()).putTempStats(event.getUniqueId(), new WarStats((Manager) main(), event.getUniqueId(),
                        check.getInt("kills"), check.getInt("deaths"),
                        check.getInt("highestStreak"), check.getInt("matchesPlayed")));
            } else {
                main().plugin().log("Creating statistics record for " + event.getName());
                PreparedStatement newStats = ((Manager) main()).query().prepare("INSERT INTO `WarStats` (`player_uuid`) VALUES (?)");
                newStats.setString(1, event.getUniqueId().toString());
                newStats.executeUpdate(); // Execute our insertion query.
                newStats.close(); // Close the prepared statement.
            }
            stats.close(); // Close the prepared statement.
            check.close(); // Close this one too.
        } catch (SQLException e) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(main()._("prelogin.error"));
            main().plugin().log("Unable to generate statistics for " + event.getUniqueId() + "!");
            e.printStackTrace();
        }
    }

    /**
     * This event procedure handles high-priority logic
     * when a player first connects to the server.
     *
     * @param event An event called by Spigot.
     */
    @EventHandler(priority = EventPriority.HIGHEST) // Highest priority denoting this one needs to be executed first.
    public void onJoin(PlayerJoinEvent event) {
        Player target = event.getPlayer(); // Get the player who connected.
        target.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16); // 1.9 PVP
        WarPlayer wp = main().craftWarPlayer(target); // Creates their needed WarPlayer record.

        WarMatch.Status status = main().match().getStatus(); // Get the status of the match.
        // Clear the player's inventory and give them the spectator kit.
        main().items().clear(wp);
        main().giveSpectatorKit(wp);

        if (status == WarMatch.Status.STARTING || status == WarMatch.Status.PLAYING || status == WarMatch.Status.CYCLE)
            target.teleport(main().cache().getCurrentMap().getSpectatorSpawn()); // Spawn them in the current defined map.
        else if (status == WarMatch.Status.VOTING)
            target.teleport(((Map) main().cache().getMap(main().match().getPreviousMap())).getSpectatorSpawn_()); // Spawn them in the previous defined map.

        if (status != WarMatch.Status.PLAYING) {
            event.getPlayer().setScoreboard(((Match) main().match()).s()); // Show the default scoreboard.
            ((Match) main().match()).s().getTeam("PostSpectators").addEntry(event.getPlayer().getName()); // Add them to this scoreboard.
            //TODO: Add them as spectators???
        } else
            event.getPlayer().setScoreboard(main().match().getCurrentMode().s()); // Show the gamemode's scoreboard.
        target.setGameMode(GameMode.CREATIVE);
    }

    /**
     * This event procedure correctly handles what
     * happens when a player disconnects.
     *
     * @param event An event called by Spigot.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().performCommand("leave"); // Act as if they were using the leave command.
        main().destroyWarPlayer(event.getPlayer().getUniqueId()); // Remove their WarPlayer record.
    }

    /* War event handling */

    @EventHandler
    public void onDeath(MatchPlayerDeathEvent event) {
        WarStats dead = ((WarPlayerPlus) event.getPlayer()).stats();
        dead.addDeath();

        if (event.getKiller() != null) {
            WarStats killer = ((WarPlayerPlus) event.getKiller()).stats();
            killer.addKill();
            Player target = event.getKiller().getPlayer();
            if (killer.getCurrentStreak() % 5 == 0) {
                target.playSound(target.getLocation(), Sound.ENTITY_VEX_CHARGE, 1F, 1F);
                target.sendMessage(main()._("killstreaks.status", killer.getCurrentStreak()));
            }
            if (killer.getCurrentStreak() == 10) {
                target.playSound(target.getLocation(), Sound.ENTITY_PARROT_IMITATE_ENDERDRAGON, 1F, 1F);
                target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 5 * 20, 0));
                target.setFireTicks(100);
                target.sendMessage(main()._("killstreaks.onfire", target.getDisplayName()));
            }
            target.getWorld().spawnParticle(Particle.TOTEM, target.getLocation(), 70);
        }
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        for (WarPlayer pl : main().getWarPlayers().values())
            if (pl.isPlaying())
                ((WarPlayerPlus) pl).stats().addMatchPlayed();
    }
}
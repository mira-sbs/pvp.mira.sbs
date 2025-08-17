package sbs.mira.pvp.util;

import sbs.mira.pvp.MiraPvpMaster;
import sbs.mira.pvp.MiraPvpPlayer;
import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.event.MatchEndEvent;
import sbs.mira.pvp.framework.event.MatchPlayerDeathEvent;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.framework.util.WarMatch;
import sbs.mira.pvp.framework.MiraModule;
import sbs.mira.pvp.game.Map;
import sbs.mira.pvp.stats.WarStats;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

/**
 * This class handles player joins, statistics
 * manipulation, and other database-related things.
 *
 * @author ILavaYou
 * @version 1.0
 * @since 1.0
 */
public class StatsListener extends MiraModule implements Listener {

    StatsListener(MiraPulse main) {
        super(main);
        mira().plugin().getServer().getPluginManager().registerEvents(this, mira().plugin());
        this.votes = new HashMap<>();
    }

    @EventHandler
    public void onServerList(ServerListPingEvent event) {
        Map map = (Map) mira().cache().getCurrentMap();
        String state;
        switch (mira().match().getStatus()) {
            case CYCLE:
                state = map.getMapName() + GRAY + " (cycling)";
                break;
            case VOTING:
                state = map.getMapName() + GRAY + " (in a vote)";
                break;
            case STARTING:
                if (Bukkit.getOnlinePlayers().size() > 0)
                    state = map.getMapName() + GRAY + " (starting)";
                else
                    state = map.getMapName() + GRAY + " (waiting for players)";
                break;
            case PLAYING:
                state = map.getMapName() + GRAY + " (" + mira().strings().getDigitalTime((int) (map.getMatchDuration() - mira().match().getCurrentMode().getTimeElapsed())) + ") (" + mira().match().getCurrentMode().getName() + ")";
                break;
            default:
                state = RED + "Server is non-functional";
                break;
        }
        event.setMotd(GREEN + "[War]" + DARK_GRAY + " - " + WHITE + state + "\n" + RED + "https://rpg.solar/ ❤");
    }

    /**
     * This event procedure handles pre-login
     * logic for statistics generation.
     *
     * @param event An event called by spigot.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return; // Don't create anything in the database if they can't get on.
        try {

            // Checks if the player has stats recorded already.
            PreparedStatement stats = ((MiraPvpMaster) mira()).query().prepare("SELECT * FROM `WarStats` WHERE `player_uuid`=?");
            stats.setString(1, event.getUniqueId().toString());
            ResultSet check = stats.executeQuery(); // Execute the check and get our result.

            if (check.next()) {
                mira().plugin().log(event.getName() + " had previous stats, retrieving...");
                ((MiraPvpMaster) mira()).putTempStats(event.getUniqueId(), new WarStats((MiraPvpMaster) mira(), event.getUniqueId(),
                        check.getInt("kills"), check.getInt("deaths"),
                        check.getInt("highestStreak"), check.getInt("matchesPlayed"),
                        check.getInt("revives")));
            } else {
                mira().plugin().log("Creating statistics record for " + event.getName());
                PreparedStatement newStats = ((MiraPvpMaster) mira()).query().prepare("INSERT INTO `WarStats` (`player_uuid`) VALUES (?)");
                newStats.setString(1, event.getUniqueId().toString());
                newStats.executeUpdate(); // Execute our insertion query.
                newStats.close(); // Close the prepared statement.
            }
            stats.close(); // Close the prepared statement.
            check.close(); // Close this one too.
        } catch (SQLException e) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(mira().message("prelogin.error"));
            mira().plugin().log("Unable to generate statistics for " + event.getUniqueId() + "!");
            e.printStackTrace();
        }
    }

    /**
     * This event procedure handles high-priority logic
     * when a player first connects to the server.
     *
     * @param event An event called by Spigot.
     */
    @EventHandler(priority = EventPriority.LOWEST) // Highest priority denoting this one needs to be executed first.
    public void onJoin(PlayerJoinEvent event) {
        Player target = event.getPlayer(); // Get the player who connected.
        target.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16); // 1.9 PVP
        MiraPlayer wp = mira().craftWarPlayer(target); // Creates their needed WarPlayer record.
        wp.update(); // Update prefix n' shit.

        WarMatch.Status status = mira().match().getStatus(); // Get the status of the match.
        // Clear the player's inventory and give them the spectator kit.
        mira().items().clear(wp);
        mira().giveSpectatorKit(wp);

        if (status == WarMatch.Status.STARTING || status == WarMatch.Status.PLAYING || status == WarMatch.Status.CYCLE)
            target.teleport(mira().cache().getCurrentMap().getSpectatorSpawn()); // Spawn them in the current defined map.
        else if (status == WarMatch.Status.VOTING)
            target.teleport(((Map) mira().cache().getMap(mira().match().getPreviousMap())).getSpectatorSpawn_()); // Spawn them in the previous defined map.

        if (status != WarMatch.Status.PLAYING) {
            event.getPlayer().setScoreboard(((Match) mira().match()).s()); // Show the default scoreboard.
            ((Match) mira().match()).s().getTeam("PostSpectators").addEntry(event.getPlayer().getName()); // Add them to this scoreboard.
            //TODO: Add them as spectators???
        } else
            event.getPlayer().setScoreboard(mira().match().getCurrentMode().s()); // Show the gamemode's scoreboard.
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
        mira().destroyWarPlayer(event.getPlayer().getUniqueId()); // Remove their WarPlayer record.
    }

    /* War event handling */

    @EventHandler
    public void onDeath(MatchPlayerDeathEvent event) {
        WarStats dead = ((MiraPvpPlayer) event.getPlayer()).stats();
        dead.addDeath();

        if (event.getKiller() != null) {
            WarStats killer = ((MiraPvpPlayer) event.getKiller()).stats();
            killer.addKill();
            Player target = event.getKiller().crafter();
            if (killer.getCurrentStreak() % 5 == 0) {
                target.playSound(target.getLocation(), Sound.ENTITY_VEX_CHARGE, 1F, 1F);
                target.sendMessage(mira().message("killstreaks.status", killer.getCurrentStreak()));
            }
            if (killer.getCurrentStreak() == 10) {
                target.playSound(target.getLocation(), Sound.ENTITY_PARROT_IMITATE_ENDERDRAGON, 1F, 1F);
                target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 5 * 20, 0));
                target.setFireTicks(100);
                target.sendMessage(mira().message("killstreaks.onfire", target.getDisplayName()));
            }
            target.getWorld().spawnParticle(Particle.TOTEM, target.getLocation(), 70);
        }
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        for (MiraPlayer pl : mira().getWarPlayers().values())
            if (pl.is_member_of_team())
                ((MiraPvpPlayer) pl).stats().addMatchPlayed();
    }

    /* Voting storage and rewards. */

    private HashMap<UUID, Integer> votes; // Stores offline votes.

    @EventHandler
    public void onVote(VotifierEvent event) {
        Player target = Bukkit.getPlayer(event.getVote().getUsername()); // Determine the target.
        if (target != null)
            awardVote(target); // Give the reward if they're online.
        else {
            // Store the reward if they're offline.
            OfflinePlayer target2 = Bukkit.getOfflinePlayer(event.getVote().getServiceName());
            votes.put(target2.getUniqueId(), votes.getOrDefault(target2.getUniqueId(), 0) + 1); // Increment their votes.
        }
    }

    @EventHandler
    public void onVoteJoin(PlayerJoinEvent event) {
        if (votes.containsKey(event.getPlayer().getUniqueId()))
            // Delay the task so the MoTD runs first.
            Bukkit.getScheduler().runTaskLater(mira().plugin(), () -> {
                // Run the amount of times they voted.
                for (int i = 0; i < votes.get(event.getPlayer().getUniqueId()); i++)
                    awardVote(event.getPlayer()); // Perform voting task now.
                votes.remove(event.getPlayer().getUniqueId()); // Remove them from the list.
            }, 20L);
    }

    /**
     * Gives voting rewards to a player.
     */
    private void awardVote(Player target) {
        // Do the broadcast. Don't broadcast how many times they voted, though.
        for (Player online : Bukkit.getOnlinePlayers())
            if (!online.equals(target))
                online.sendMessage(mira().message("votifier.others", target.getDisplayName()));
            else
                online.sendMessage(mira().message("votifier.self"));

        // Spawn a congratulatory firework.
        ((MiraPvpMaster) mira()).entity().spawnFirework(target.getLocation());
        ((MiraPvpPlayer) mira().getWarPlayer(target)).stats().addRevive();
    }
}
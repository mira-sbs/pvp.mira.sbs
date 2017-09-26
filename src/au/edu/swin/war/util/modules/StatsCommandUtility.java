package au.edu.swin.war.util.modules;

import au.edu.swin.war.WarPlayerPlus;
import au.edu.swin.war.framework.util.WarModule;
import au.edu.swin.war.stats.WarStats;
import au.edu.swin.war.util.Manager;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Handles all statistics commands.
 *
 * @author s101601828 @ Swin.
 * @version 1.1
 * @see Command
 * @see org.bukkit.command.Command
 * <p>
 * Created by Josh on 26/09/2017.
 * @since 1.0
 */
public class StatsCommandUtility extends WarModule {

    @SuppressWarnings("unused") // This is used, just not directly.
    public StatsCommandUtility(Manager main) {
        super(main);
    }

    /**
     * Listens to the phrase '/stats'.
     * If these are said by the player, perform
     * the statistics check logic on them.
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"stats"},
            desc = "Check the statistics of a player", // Brief description of the command.
            usage = "<player>",
            max = 1)
    public void stats(CommandContext args, CommandSender sender) {
        String toTarget = sender.getName();
        if (args.argsLength() == 1) toTarget = args.getString(0);

        final OfflinePlayer finalTarget = Bukkit.getOfflinePlayer(toTarget);
        if (!finalTarget.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "No statistics were found for this player.");
            return;
        }

        if (finalTarget.isOnline()) {
            WarStats stats = ((WarPlayerPlus) main().getWarPlayer(finalTarget.getUniqueId())).stats();
            displayStats(sender, finalTarget.getName(), stats.getKills(), stats.getDeaths(), stats.getCurrentStreak(), stats.getHighestStreak(), stats.getMatchesPlayed());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(main().plugin(), () -> {
                try {
                    PreparedStatement stmt = ((Manager) main()).query().prepare("SELECT * FROM `war_stats` WHERE `player_uuid`=?");
                    stmt.setString(1, finalTarget.getUniqueId().toString());
                    ResultSet stats = stmt.executeQuery();
                    if (stats.next())
                        displayStats(sender, finalTarget.getName(), stats.getInt("kills"), stats.getInt("deaths"), -1, stats.getInt("highestStreak"), stats.getInt("matchesPlayed"));
                    else sender.sendMessage(ChatColor.RED + "No statistics were found for this player.");
                    stats.close();
                } catch (SQLException e) {
                    sender.sendMessage(ChatColor.RED + "An error occurred. Please try again later.");
                    main().plugin().log("Unable to retrieve stats for " + finalTarget.getUniqueId());
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Listens to the phrases '/leaderboard' and '/lb'.
     * If these are said by the player, perform
     * the leaderboard display logic on them.
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"leaderboard", "lb"},
            desc = "Displays the leaderboard",
            usage = "<page>",
            max = 1)
    public void leaderboard(CommandContext args, CommandSender sender) throws CommandNumberFormatException {
        if (waiting.contains(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "Try this command again in a few moments.");
            return;
        }
        waiting.add(sender.getName());
        int page = args.argsLength() == 1 ? args.getInteger(0) : 1;
        int offset = (page * 10) - 10;
        Bukkit.getScheduler().runTaskAsynchronously(main().plugin(), () -> {
            try {
                ResultSet lb = ((Manager) main()).query().prepare("SELECT * FROM `war_stats` ORDER BY `kills` DESC LIMIT 10 OFFSET " + offset).executeQuery();
                sender.sendMessage("--- Leaderboard Page " + page + " ---");
                for (int i = 0; i < 10; i++) {
                    if (!lb.next()) {
                        if (i != 9) sender.sendMessage(ChatColor.RED + "No more results to display.");
                        break;
                    }
                    OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(lb.getString("player_uuid")));
                    sender.sendMessage("#" + (offset + i + 1) + " " + target.getName() + ": Kills: " + ChatColor.RED + lb.getInt("kills") + ChatColor.WHITE + " - Deaths: " + ChatColor.BLUE + lb.getInt("deaths"));
                }
                sender.sendMessage("------------------------" + (page + "").replaceAll(".", "-"));
            } catch (SQLException e) {
                sender.sendMessage(ChatColor.RED + "An error occurred. Please try again later.");
                e.printStackTrace();
            }
            waiting.remove(sender.getName());
        });
    }

    private List<String> waiting = Collections.synchronizedList(new ArrayList<String>());

    /**
     * Display stats in a formatted way to a CommandSender.
     *
     * @param sender        To display to.
     * @param kills         Supplied.
     * @param deaths        Supplied.
     * @param currentStreak Supplied. (can be -1 to symbolize offline)
     * @param highestStreak Supplied.
     * @param matchesPlayed Supplied.
     */
    private void displayStats(CommandSender sender, String name, int kills, int deaths, int currentStreak, int highestStreak, int matchesPlayed) {
        sender.sendMessage("--- War statistics for " + name + " ---");
        sender.sendMessage("Kills: " + ChatColor.RED + kills);
        sender.sendMessage("Deaths: " + ChatColor.BLUE + deaths);
        sender.sendMessage("KD/R: " + ChatColor.GREEN + calculateKD(kills, deaths));
        sender.sendMessage("Killstreak: " + ChatColor.AQUA + (currentStreak != -1 ? currentStreak + "" + ChatColor.WHITE + " (" + highestStreak + " highest)" : highestStreak + "" + ChatColor.WHITE + " (highest)"));
        sender.sendMessage("Matches played: " + ChatColor.GOLD + matchesPlayed);
        sender.sendMessage("-----------------------" + name.replaceAll("(?s).", "-"));
    }

    /**
     * Calculates a simple 0.00 KD/R.
     *
     * @param kills  Supplied.
     * @param deaths Supplied.
     * @return Calculated kill/death ratio.
     */
    private String calculateKD(int kills, int deaths) {
        NumberFormat nf = new DecimalFormat("#.##");
        double result;
        if (deaths == 0) result = kills;
        else if (kills == 0) result = 0;
        else result = kills / deaths;
        return nf.format(result);
    }
}

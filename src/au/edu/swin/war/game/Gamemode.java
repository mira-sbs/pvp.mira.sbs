package au.edu.swin.war.game;

import au.edu.swin.war.event.MatchPlayerRespawnEvent;
import au.edu.swin.war.event.PostMatchPlayerRespawnEvent;
import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarMode;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.modes.*;
import au.edu.swin.war.util.Manager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map;

/**
 * An extension to WarMode.
 * <p>
 * This is the class that should be extended by
 * actual gamemode classes to provide a skeleton
 * and good accessibility + functionality.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarMode
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public abstract class Gamemode extends WarMode {


    protected String tempWinner; // The winner of the round, if any.

    /* Website Statistics Stuff */
    private HashMap<UUID, Integer> kills;
    private HashMap<UUID, Integer> deaths;
    private int environmentalKills;
    private ArrayList<String> eventLog;
    /*                          */

    /**
     * Logs an event. This can be anything.
     * A kill, a death, etc.
     * A flag capture, a flag drop, etc.
     * Log it all!
     *
     * @param toLog What to log.
     */
    public void logEvent(String toLog) {
        eventLog.add(ChatColor.GRAY + main.strings().getDigitalTime(getTimeElapsed()) + ChatColor.WHITE + " " + toLog);
    }

    public void initializeCommon() {
        kills = new HashMap<>(); // Initialize recorded kills.
        deaths = new HashMap<>(); // Initialize recorded deaths.
        environmentalKills = 0; // Reset unrecorded deaths.
        eventLog = new ArrayList<>(); // Create an array for event logs.
    }

    /**
     * This procedure also acts as a statistics pushing procdure.
     */
    @SuppressWarnings("unchecked")
    public void resetCommon() {
        if (((Manager) main).conf().WEBSTATS_ENABLED) {
            JSONObject stats = new JSONObject();
            if (getTimeElapsed() == 0)
                setTimeElapsed(1); // Elapse at least one second to prevent a divison by zero error.

            // Total Kills
            int totalKills = 0;
            for (int kill : kills.values())
                totalKills += kill;

            // Total Deaths
            int totalDeaths = 0;
            for (int death : deaths.values())
                totalDeaths += death;

            // Global Match Information
            stats.put("matchid", ((Manager) main).conf().WEBSTATS_POS + 1);
            stats.put("mapname", map().getMapName());
            stats.put("gamemode", getFullName());
            stats.put("duration", main.strings().getDigitalTime(getTimeElapsed()));
            stats.put("date", new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
            stats.put("winner", tempWinner);
            stats.put("totalkills", totalKills);
            stats.put("totaldeaths", totalDeaths);
            stats.put("envdeaths", environmentalKills);
            if (getTimeElapsed() < 60) // Prevent a divison by zero error.
                stats.put("kpm", totalKills);
            else
                stats.put("kpm", new DecimalFormat("0.00").format((double) totalKills / (double) (getTimeElapsed() / 60)));

            if (totalKills == 0 || getTimeElapsed() < 35) { // Too boring to record.
                Bukkit.broadcastMessage("This match has not been recorded.");
                return;
            }

            ((Manager) main).conf().incrementPosition(); // Increment to the next match ID.

            // Team Information
            JSONObject teams = new JSONObject(); // JSON Object to store teams.
            for (WarTeam team : getTeams()) {
                JSONObject jteam = new JSONObject(); // JSON Object for this team.
                jteam.put("color", team.getTeamColor().toString());

                int tKills = 0;
                for (Map.Entry<UUID, Integer> entry : kills.entrySet()) // For every player that participated...
                    if (team.getBukkitTeam().getPlayers().contains(Bukkit.getOfflinePlayer(entry.getKey()))) // Were they a part of this team?
                        tKills += entry.getValue(); // Add their total to the team's total.

                int tDeaths = 0;
                for (Map.Entry<UUID, Integer> entry : deaths.entrySet()) // For every player that participated...
                    if (team.getBukkitTeam().getPlayers().contains(Bukkit.getOfflinePlayer(entry.getKey()))) // Were they a part of this team?
                        tDeaths += entry.getValue(); // Add their total to the team's total.

                // Reflect the totals that were calculated.
                jteam.put("kills", tKills);
                jteam.put("deaths", tDeaths);
                jteam.put("players", team.getBukkitTeam().getPlayers().size());

                JSONObject extra = new JSONObject(); // Extra stuff.
                for (Map.Entry<String, Object> data : getExtraTeamData(team).entrySet()) // Assign all gamemode-specific data.
                    extra.put(data.getKey(), data.getValue());
                jteam.put("extra", extra);
                teams.put(team.getTeamName(), jteam); // Put this team into the teams array.
            }
            stats.put("teams", teams);

            // Event Information
            JSONArray events = new JSONArray();
            events.addAll(eventLog); // Bulk add events to JSON array.
            stats.put("events", events);

            // Run the HTTP request asynchronous to prevent lag..
            main.plugin().getServer().getScheduler().runTaskAsynchronously(main.plugin(), () -> {
                try {
                    URL url = new URL(((Manager) main).conf().WEBSTATS_ACTION);
                    URLConnection con = url.openConnection();
                    HttpURLConnection http = (HttpURLConnection) con;
                    http.setRequestMethod("POST");
                    http.setDoOutput(true);

                    Map<String, String> arguments = new HashMap<>();
                    // Our HTTP POST arguments.
                    arguments.put("secret", ((Manager) main).conf().WEBSTATS_SECRET);
                    arguments.put("stats", stats.toJSONString());

                    // Encodes our request.
                    StringJoiner sj = new StringJoiner("&");
                    for (Map.Entry<String, String> entry : arguments.entrySet())
                        sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                                + URLEncoder.encode(entry.getValue(), "UTF-8"));
                    byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
                    int length = out.length;

                    // Send the request!
                    http.setFixedLengthStreamingMode(length);
                    http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    http.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                    http.connect();
                    try (OutputStream os = http.getOutputStream()) {
                        os.write(out);
                    }
                    // Log the response to the request to ensure it was successful.
                    main.plugin().log("Stats HTTP POST request successful: " + IOUtils.toString(http.getInputStream(), StandardCharsets.UTF_8.name()));
                    Bukkit.broadcastMessage("Full Match statistics recorded @ " + ChatColor.UNDERLINE + "https://rpg.solar/match/" + ((Manager) main).conf().WEBSTATS_POS);
                } catch (IOException e) {
                    // If anything goes wrong, just raw print the stats.
                    e.printStackTrace();
                    main.plugin().log("Was unable to POST request to the stats script!");
                    main.plugin().log("Raw stats: " + stats.toString());
                }
            });
        }
        // Clear everything, now that it isn't needed.
        kills.clear();
        kills = null;
        deaths.clear();
        deaths = null;
        tempWinner = null;
        eventLog.clear();
        eventLog = null;
    }

    /**
     * This function should be extended and made to return a
     * team's extra data, such as how many flags they attempted
     * to capture, how long they held a flag for, etc.
     * <p>
     * This is used for statistics purposes.
     *
     * @return The extra team data, if applicable.
     */
    protected abstract HashMap<String, Object> getExtraTeamData(WarTeam team);

    public void onJoin(WarPlayer joined) {
        logEvent(joined.getName() + " joined " + joined.getCurrentTeam().getTeamColor() + joined.getCurrentTeam().getTeamName());
        kills.put(joined.getPlayer().getUniqueId(), 0);
        deaths.put(joined.getPlayer().getUniqueId(), 0);
        //TODO: Maybe make this display what the player needs to do?
    }

    @EventHandler
    public void playerDeathHandle(PlayerDeathEvent event) {
        WarPlayer dead = main.getWarPlayer(event.getEntity()); // Get the player who died.
        inc(deaths, dead.getPlayer().getUniqueId()); // Increment their deaths this match.
        WarPlayer killer = main.getWarPlayer(dead.getPlayer().getKiller()); // Get the player who killed the player.

        ((Manager) main).respawn().onDeath(main.getWarPlayer(dead.getPlayer().getUniqueId()));
        dead.getCurrentTeam().addDeath();
        // Handle respawning for this player.

        if (dead.equals(killer)) killer = null; // Did they kill themselves?
        if (killer == null) {
            event.setDeathMessage(event.getDeathMessage().replaceAll(dead.getName(), dead.getTeamName()));
            logEvent(event.getDeathMessage()); // Log the death.
            environmentalKills++;
            onDeath(dead);
            return;
        }

        // Play a sound effect.
        killer.getCurrentTeam().addKill();
        dead.getPlayer().getWorld().playSound(dead.getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 1L, 1L);

        // Format the death message to show display names instead.
        event.setDeathMessage(event.getDeathMessage()
                .replaceAll(dead.getName(), dead.getTeamName())
                .replaceAll(killer.getName(), killer.getTeamName()));

        // Call the onKill() procedure so the extended Gamemode can react to it.
        logEvent(event.getDeathMessage()); // Log the event first.
        inc(kills, killer.getPlayer().getUniqueId()); // Increment their kills this match.
        onKill(dead, killer);
    }

    /**
     * Increments a key in a UUID,Integer hashmap.
     * Used for incrementing deaths/kills.
     *
     * @param toAddTo The HashMap to add to.
     * @param key     The key to increment.
     */
    private void inc(HashMap<UUID, Integer> toAddTo, UUID key) {
        toAddTo.put(key, toAddTo.get(key) + 1);
    }

    /**
     * Broadcasts a winner after calculation.
     *
     * @param winners   The list of winners.
     * @param objective The objective. i.e. CTF = "captures"
     * @param highest   Used if there is a single winner.
     */
    protected void broadcastWinner(List<WarTeam> winners, String objective, int highest) {
        // Is there more than one winner?
        if (winners.size() > 1) {
            TextComponent comp = new TextComponent("It's a " + winners.size() + "-way tie! ");
            comp.addExtra(main.strings().winnerFormat(winners));
            comp.addExtra(" tied!");
            main.broadcastSpigotMessage(comp);

            tempWinner = main.strings().sentenceFormat(winners);
        } else if (winners.size() == 1) {
            WarTeam winner = winners.get(0); // Get the singleton winner!
            // ChatColor.stripColor() is used to remove the team's color from the String so it can be queried to get their points.
            TextComponent comp = winner.getHoverInformation();
            // WHY SO GRAMMAR?
            comp.addExtra((winner.getTeamName().charAt(winner.getTeamName().length() - 1) == 's' ? " are the winners" : " is the winner") + " with " + highest + " " + objective + "!");
            main.broadcastSpigotMessage(comp);
            tempWinner = main.strings().sentenceFormat(winners);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.getPlayer().setBedSpawnLocation(null); // No bed spawn locations.

        WarPlayer wp = main.getWarPlayer(event.getPlayer()); // Get their WarPlayer implement.

        // Change the respawn location to a random team spawn.
        event.setRespawnLocation(randomSpawnFrom(
                main.cache().getCurrentMap().getTeamSpawns(
                        wp.getCurrentTeam().getTeamName())).toLocation(
                main.match().getCurrentWorld(), true));

        // Apply the current map's inventory.
        main.cache().getCurrentMap().applyInv(wp);

        // They're back in the match again!
        event.getPlayer().setGameMode(GameMode.SURVIVAL);
    }

    @EventHandler
    public void onRespawn(MatchPlayerRespawnEvent event) {
        WarPlayer wp = event.getPlayer(); // Get their WarPlayer implement.

        if (!wp.isPlaying()) return; // Ignore this if they aren't playing.

        // Teleport them to their respawn location.
        wp.getPlayer().teleport(randomSpawnFrom(
                map().getTeamSpawns(
                        wp.getCurrentTeam().getTeamName())).toLocation(
                main.match().getCurrentWorld(), true));

        // Apply the current map's inventory.
        map().applyInv(wp);

        // They're back in the match again!
        wp.getPlayer().setGameMode(GameMode.SURVIVAL);
        Bukkit.getPluginManager().callEvent(new PostMatchPlayerRespawnEvent(wp));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(s());
    }

    /**
     * Returns the amount of WarPlayers that are
     * marked as joined. This is used by gamemodes
     * that require a certain amount of players.
     *
     * @return Amount of players marked as joined.
     */
    protected int getJoined() {
        int joined = 0;
        for (WarPlayer pl : main.getWarPlayers().values())
            if (pl.isJoined()) joined++;
        return joined;
    }

    /**
     * Returns an inputted team's opposition.
     * This method should only be used in a 2-team match.
     *
     * @param team The team to check for opposition.
     * @return The opposition, if any.
     */
    public String opposition(WarTeam team) {
        for (WarTeam teams : map().getTeams())
            if (!team.getTeamName().equals(teams.getTeamName()))
                return teams.getTeamColor() + teams.getTeamName();
        return ChatColor.WHITE + "Unknown";
    }

    /**
     * This is an enumeration of all available gamemodes.
     * When designating gamemodes for your map, use this.
     */
    public enum Mode {
        TDM("Team Death Match", "TDM"),
        FFA("Free For All", "FFA"),
        LTS("Last Team Standing", "LTS"),
        LMS("Last Man Standing", "LMS"),
        KOTH("King of The Hill", "KoTH"),
        DDM("District Death Match", "DDM"),
        CTF("Capture The Flag", "CTF"),
        LP("Lifepool", "LP"),
        DTM("Destroy The Monument", "DTM");

        final String fullName;
        final String shortName;

        Mode(String fullName, String shortName) {
            this.fullName = fullName;
            this.shortName = shortName;
        }

        /**
         * Formats this string, similar to StringUtility#sentenceFormat
         * but specifically for this enumerated type.
         *
         * @param array The array of modes to format.
         * @return The formatted string.
         */
        public static TextComponent format(Mode[] array, WarManager main) {
            if (array.length == 0) return new TextComponent("None");
            TextComponent result = new TextComponent();
            if (array.length == 1) return new TextComponent(array[0].getDescriptionComponent(main, true));
            int i = 1;
            while (i <= array.length) {
                if (i == array.length) {
                    result.addExtra(ChatColor.WHITE + " and ");
                    result.addExtra(array[i - 1].getDescriptionComponent(main, true));
                } else if (i == 1)
                    result = array[0].getDescriptionComponent(main, true);
                else {
                    result.addExtra(ChatColor.WHITE + ", ");
                    result.addExtra(array[i - 1].getDescriptionComponent(main, true));
                }
                i++;
            }
            return result;
        }

        /**
         * Converts an instance of a Gamemode into its enumerated counterpart.
         * Extremely ineffective in my opinion.
         * //TODO: Make each class store its own enumerated type?
         *
         * @param mode The mode to check.
         * @return The resulting enum.
         */
        public static Mode fromGamemode(WarMode mode) {
            if (mode instanceof CTF)
                return CTF;
            else if (mode instanceof DDM)
                return DDM;
            else if (mode instanceof DTM)
                return DTM;
            else if (mode instanceof FFA)
                return FFA;
            else if (mode instanceof KoTH)
                return KOTH;
            else if (mode instanceof LMS)
                return LMS;
            else if (mode instanceof LP)
                return LP;
            else if (mode instanceof LTS)
                return LTS;
            else if (mode instanceof TDM)
                return TDM;
            else return null;
        }

        /**
         * Returns the full name of the enumerated type.
         *
         * @return Full name.
         */
        public String getFullName() {
            return fullName;
        }

        /**
         * Returns the short name of the enumerated type.
         *
         * @return Short name.
         */
        public String getActualShortName() {
            return shortName;
        }

        /**
         * Generates an interactive gamemode hoverable.
         *
         * @param main          Supercontroller for gamemode access.
         * @param withVoteClick Enable click this to vote?
         * @return Resulting chat component.
         */
        public TextComponent getDescriptionComponent(WarManager main, boolean withVoteClick) {
            TextComponent msg = new TextComponent(ChatColor.GREEN + "[" + shortName + "]");
            WarMode assoc = main.cache().getGamemode(getFullName());
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.GREEN + "" + ChatColor.BOLD + getFullName() + "\n"
                            + ChatColor.RED + ChatColor.ITALIC + "When attacking:\n" + ChatColor.WHITE
                            + assoc.getOffensive() + ChatColor.BLUE + ChatColor.ITALIC + "\nWhen defending:\n"
                            + ChatColor.WHITE + assoc.getDefensive() + (withVoteClick ? "\n"
                            + ChatColor.UNDERLINE + "Click To Vote" : "")).create()));
            if (withVoteClick)
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote " + shortName));
            return msg;
        }
    }
}

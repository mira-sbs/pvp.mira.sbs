package au.edu.swin.war.util;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarMap;
import au.edu.swin.war.framework.util.WarMatch;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * An extension to WarMatch.
 * Acts as the mediator of match flow.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarMatch
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public class Match extends WarMatch {

    private final HashMap<Gamemode.Mode, Integer> votes; // Holds a key/value pair for a gamemode and the number of votes it has.
    private final ArrayList<UUID> voted; // Players who have voted during a vote.
    private Scoreboard gScore; // A global, temporary scoreboard which is kept during the STARTING state.
    private long previousID; // Holds the world identifier of the previous match world.
    private Gamemode.Mode winningVote; // Holds the winning vote during a vote.
    private String setNext; // Holds if a map has been set next.

    private final int voteTime; // Holds how long votes go for;
    private final int cycleTime; // Holds how long cycles go for;
    private final int startTime; // Holds how long startups go for;

    /**
     * This constructor calls the constructor of WarMatch.
     *
     * @param main Running instance of Manager.
     */
    Match(Manager main) {
        super(main);
        this.votes = new HashMap<>();
        this.voted = new ArrayList<>();
        this.previousID = 0; // Assign a default previous ID.
        this.gScore = main.plugin().getServer().getScoreboardManager().getNewScoreboard();
        this.voteTime = main.plugin().getConfig().getInt("settings.voteTime");
        this.cycleTime = main.plugin().getConfig().getInt("settings.cycleTime");
        this.startTime = main.plugin().getConfig().getInt("settings.startTime");
    }

    /**
     * Quick function to return the running map
     * without making an external call to cache.
     * <p>
     * This also automatically casts the running
     * instance to Map, so all extra functionality
     * is available.
     *
     * @return Map instance.
     */
    private Map getRunningMap() {
        return (Map) main().cache().getCurrentMap();
    }

    /**
     * Returns the world of the map that was
     * previously played. This should be used
     * for late-vote joins.
     *
     * @return Previous world.
     */
    public World getPreviousWorld() {
        return Bukkit.getWorld(previousID + "");
    }

    /**
     * This function returns the list of players
     * who have voted, by their UUID. This should
     * only be accessed during vote time to check
     * who has voted or not.
     *
     * @return The list of players who have voted.
     */
    public ArrayList<UUID> getVoted() {
        return voted;
    }

    /**
     * This function returns the key/value set of
     * gamemodes and the amount of votes they have
     * received. This should only be accessed during
     * vote time to increment the amount of votes
     * for a player.
     * <p>
     * The rest is handled internally.
     *
     * @return The key/value set of gamemodes and their scores.
     */
    public HashMap<Gamemode.Mode, Integer> getVotes() {
        return votes;
    }

    /**
     * This method forces a desired map to be played next.
     * Does not perform a null check, so be careful.
     *
     * @param mapToSet Map to set.
     */
    public void set(WarMap mapToSet) {
        setNext = mapToSet.getMapName();
        mapToSet.setWasSet(true);
    }

    /**
     * Returns the name of the map that was set
     * to be played next, if any.
     *
     * @return Set next map name.
     */
    public String getSetNext() {
        return setNext;
    }

    /**
     * Returns an instance of the global scoreboard.
     * Is used to give to the player in case there is
     * no match running and a placeholder scoreboard
     * is used instead.
     *
     * @return The global scoreboard.
     */
    Scoreboard s() {
        return gScore;
    }

    @Override
    public void endCycle() {
        new BukkitRunnable() {
            int timer = cycleTime;

            public void run() {
                timer--;
                if (timer % 3 == 0) {
                    // Make cool particles appear as a celebration for a good match.
                    if (getCurrentWorld().getPlayers().size() > 0) { // If there's people online...
                        // Spawn particles around a random player 8 times every 3 seconds.
                        int count2 = 8;
                        if (count2 > Bukkit.getOnlinePlayers().size()) count2 = Bukkit.getOnlinePlayers().size();
                        while (count2 > 0) {
                            ((Manager) main()).entity().spawnFirework(getCurrentWorld().getPlayers().get(
                                    new Random().nextInt(getCurrentWorld().getPlayers().size())).getLocation());
                            // Spawn a random firework at 8 people's locations.
                            // If there is less than 8 people online, then the amount of people online instead.
                            count2--;
                        }
                    }
                }
                if (timer == 0) {
                    // Move the rotation pointer to the next map.
                    if (!main().cache().getCurrentMap().wasSet()) {
                        if (rotationPoint == getRotationList().size() - 1) rotationPoint = 0;
                        else rotationPoint++;
                    } else main().cache().getCurrentMap().setWasSet(false);

                    // Back to the voting stage!
                    this.cancel();
                    preMatch();
                }
            }
        }.runTaskTimer(main().plugin(), 0L, 20L);
    }

    /**
     * The first match must be run differently
     * to the rest of the matches. Some key values
     * have not been set until a full match has
     * have not been set until a full match has
     * been played.
     * <p>
     * The vote is skipped and the match starts.
     */
    public void firstMatch() {
        setStatus(Status.VOTING); // Change match cycle state.
        setCurrentMap(getRotationList().get(rotationPoint)); // Get the next map on the rotation.
        setRoundID(main().strings().generateID()); // Generates a new match world ID..
        votes.put(getRunningMap().getGamemodes()[0], 1); // Give the map's first preferred gamemode 1 vote on startup.
        continuePreMatch(); // Continue the pre-match cycle once the time is up.
        gScore.registerNewTeam("PostSpectators").setPrefix(ChatColor.LIGHT_PURPLE + ""); // Create a post-spectator scoreboard team.
    }

    @Override
    public void preMatch() {
        setStatus(Status.VOTING); // Change match cycle state.
        previousID = getRawRoundID(); // Archive reference for the match world ID.
        setPreviousMap(getCurrentMap()); // Set the previous map's identifier.

        if (setNext == null)
            setCurrentMap(getRotationList().get(rotationPoint)); // Get the next map on the rotation.
        else {
            setCurrentMap(setNext);
            main().cache().getCurrentMap().setWasSet(true);
            setNext = null;
        }

        setRoundID(main().strings().generateID()); // Generates a new match world ID..
        for (Gamemode.Mode mode : getRunningMap().getGamemodes()) // Give the votable modes a default score of zero.
            votes.put(mode, 0);
        new BukkitRunnable() {
            int time = voteTime; // Timer starts at 26.

            public void run() {
                if (time == voteTime)
                    for (WarPlayer online : main().getWarPlayers().values()) {
                        online.getPlayer().spigot().sendMessage(new TextComponent("A vote is now being held.\nHover over a Gamemode for more information: "));
                        online.getPlayer().spigot().sendMessage(Gamemode.Mode.format(getRunningMap().getGamemodes(), main()));
                    }
                else if (time == 0) {
                    this.cancel();
                    continuePreMatch(); // Continue the pre-match cycle once the time is up.
                }
                time--;
            }
        }.runTaskTimer(main().plugin(), 0L, 20L);
    }

    /**
     * Runs the rest of the pre-match logic.
     * Once a gamemode has been decided, the
     * next map can be loaded and the countdown
     * will begin.
     * <p>
     * This is not a WarMatch method, therefore is has
     * private access since it is a continuation.
     */
    private void continuePreMatch() {
        // Calculate the winning vote.
        int highest = -1; // No gamemode has the highest value to begin.
        for (java.util.Map.Entry<Gamemode.Mode, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > highest) { // Is this the new highest voted gamemode?
                highest = entry.getValue();  // Record this as the highest amount of votes.
                winningVote = entry.getKey(); // Change the winning vote to this gamemode.
            }
        }

        // Announce, set and clean vote results.
        votes.clear();
        voted.clear();
        setCurrentMode(main().cache().getGamemode(winningVote.getFullName()));

        TextComponent comp = new TextComponent("The next match was " + getCurrentMode().getGrammar() + " ");
        comp.addExtra(Gamemode.Mode.fromGamemode(getCurrentMode()).getDescriptionComponent(main(), false));
        comp.addExtra(" at " + getCurrentMap() + "!");
        main().broadcastSpigotMessage(comp);

        // Set the state to starting and perform starting logic.
        main().world().loadMap(getCurrentMap(), getRawRoundID());
        setStatus(Status.STARTING);

        // Teleport all online WarPlayers into the map.
        for (WarPlayer online : main().getWarPlayers().values())
            online.getPlayer().teleport(getRunningMap().getSpectatorSpawn());

        // Pre-round attribute assignment
        if (main().cache().getCurrentMap().attr().containsKey("timeLock")) {
            World world = getCurrentWorld();
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setFullTime((Long) main().cache().getCurrentMap().attr().get("timeLockTime"));
        }

        // Completely eliminate all traces of the previous match world.
        main().world().restoreMap(previousID + "");

        // Create a temporary scoreboard for everyone.
        gScore = Bukkit.getScoreboardManager().getNewScoreboard();
        org.bukkit.scoreboard.Team temp = gScore.registerNewTeam("Waiting");
        temp.setCanSeeFriendlyInvisibles(true);
        temp.setAllowFriendlyFire(false);
        temp.setPrefix(ChatColor.LIGHT_PURPLE + "");

        // Add everyone to this new scoreboard.
        for (WarPlayer pl : main().getWarPlayers().values())
            temp.addEntry(pl.getPlayer().getName());

        // Create a scoreboard objective, which actually puts data on the scoreboard.
        final Objective obj = gScore.registerNewObjective("vote", "dummy");
        String dp = (getCurrentMap() + " (" + winningVote.toString() + ")"); // Display the next map and gamemode.
        if (dp.length() > 32) dp = dp.substring(0, 32); // If the string is longer than 32 characters, substring it.
        obj.setDisplayName(dp);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR); // Display it on the player's sidebar.
        new BukkitRunnable() {
            int time = startTime; // Wait some more time!

            public void run() {
                time--;
                for (Player online : Bukkit.getOnlinePlayers())
                    online.setScoreboard(gScore);
                if (time == 0) {
                    obj.setDisplaySlot(null);
                    matchStart(); // Start the match!
                    this.cancel();
                    return;
                }

                // Format the objective to display pretty scores.
                obj.getScore("  ").setScore(3);
                gScore.resetScores("     " + (time + 1) + " second" + main().strings().plural(time + 1));
                obj.getScore("     Starting in").setScore(2);
                obj.getScore("     " + time + " second" + main().strings().plural(time)).setScore(1);
                obj.getScore(" ").setScore(0);
            }
        }.runTaskTimer(main().plugin(), 0L, 20L);
    }

    @Override
    public void matchStart() {
        setStatus(Status.PLAYING); // Set the match state to active!
        getRunningMap().activate(); // Let 'er loose!
    }

    @Override
    public void matchEnd() {
        //TODO: Respawn dead players
        getRunningMap().deactivate();
        gScore = getCurrentMode().s();

        // Assign another temporary scoreboard for the beginning of the match cycle.
        org.bukkit.scoreboard.Team temp;
        if (gScore.getTeam("PostSpectators") != null)
            temp = gScore.getTeam("PostSpectators");
        else temp = gScore.registerNewTeam("PostSpectators");
        temp.setCanSeeFriendlyInvisibles(true);
        temp.setAllowFriendlyFire(false);
        temp.setPrefix(ChatColor.LIGHT_PURPLE + "");

        // Remove everyone from the current teams.
        for (Team team : gScore.getTeams())
            for (String entry : team.getEntries())
                team.removeEntry(entry);

        // Add all online players to the post spectator team.
        for (WarPlayer pl : main().getWarPlayers().values()) {
            pl.getPlayer().setScoreboard(gScore); // Let them see this scoreboard too!
            temp.addEntry(pl.getPlayer().getName());
        }

        startCycle(); // Start the cycle.
    }

    @Override
    public void startCycle() {
        // Set the match state to cycling.
        setStatus(Status.CYCLE);

        ((Manager) main()).respawn().clear();
        // Fix everyone back up.
        for (WarPlayer pl : main().getWarPlayers().values()) {
            // Force respawn everyone using the Spigot entity API.
            if (pl.getPlayer().isDead())
                pl.getPlayer().spigot().respawn();

            // Finalise match cycling for this player.
            pl.getPlayer().playSound(pl.getPlayer().getLocation(), Sound.ENTITY_WITHER_DEATH, 1L, 1L);
            pl.setCurrentTeam(null);
            pl.getPlayer().setGameMode(GameMode.CREATIVE);
            main().items().clear(pl);
        }
        Bukkit.getScheduler().runTaskLater(main().plugin(), () -> {
            for (WarPlayer pl : main().getWarPlayers().values())
                main().giveSpectatorKit(pl);
        }, 1L);
        endCycle(); // Start the match cycle countdown.
    }
}

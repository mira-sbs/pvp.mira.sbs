package au.edu.swin.war.stats;

import au.edu.swin.war.util.Manager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * An object designed to record, modify,
 * and retrieve stats for a designated player.
 *
 * @see au.edu.swin.war.framework.WarPlayer
 */
public class WarStats {

    private final Manager main;
    private final UUID owner;

    private int kills, deaths, highestStreak, currentStreak, matchesPlayed;

    /**
     * Constructor for a returning player.
     *
     * @param owner         Owner of this stats record.
     * @param kills         Kills.
     * @param deaths        Deaths.
     * @param highestStreak Highest killstreak.
     * @param matchesWon    Current killstreak.
     */
    public WarStats(Manager main, UUID owner, int kills, int deaths, int highestStreak, int matchesWon) {
        this.main = main;
        this.owner = owner;
        this.kills = kills;
        this.deaths = deaths;
        this.highestStreak = highestStreak;
        this.currentStreak = 0;
        this.matchesPlayed = matchesWon;
    }

    /**
     * Constructor for a new player.
     *
     * @param owner Owner of this stats record.
     */
    public WarStats(Manager main, UUID owner) {
        this.main = main;
        this.owner = owner;
        this.kills = 0;
        this.deaths = 0;
        this.highestStreak = 0;
        this.currentStreak = 0;
        this.matchesPlayed = 0;
    }

    /**
     * Adds a kill, and also increments killstreak.
     * Modifies the highest killstreak if applicable.
     */
    public void addKill() {
        kills++;
        currentStreak++;
        if (currentStreak > highestStreak) {
            highestStreak = currentStreak;
            updateQuery("`highestStreak`=" + currentStreak + ",`kills`=" + kills);
        } else updateQuery("`kills`=" + kills);
    }

    /**
     * Adds a death to this player's record.
     */
    public void addDeath() {
        deaths++;
        currentStreak = 0;
        updateQuery("`deaths`=" + deaths);
    }

    /**
     * Increments the match played counter by one.
     */
    public void addMatchPlayed() {
        matchesPlayed++;
        updateQuery("`matchesPlayed`=" + matchesPlayed);
    }

    /* Getters, do you really need javadoc? */

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    /**
     * Quick function to update rows in the `war_stats` table for this player.
     *
     * @param query SET ... WHERE, where ... is the query
     */
    private void updateQuery(String query) {
        String toExecute = "UPDATE `WarStats` SET " + query + " WHERE `player_uuid`='" + owner + "'";
        main.query().addQuery(() -> {
            try {
                PreparedStatement execute = main.query().prepare(toExecute);
                execute.executeUpdate();
                execute.close();
            } catch (SQLException e) {
                main.plugin().log("Unable to update statistics for " + owner + "!");
                main.plugin().log(toExecute);
                e.printStackTrace();
            }
        });
    }
}

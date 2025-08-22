package sbs.mira.pvp.game;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;


/**
 * This (non-extendable) class handles all
 * Team-based interactions by the Gamemode.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see org.bukkit.scoreboard.Scoreboard
 * @see Team
 * <p>
 * Created by Josh on 20/03/2017.
 * @since 1.0
 */
public final class WarTeam {

    /* BEGIN RECORD */
    private final String teamName;
    private final ChatColor teamColor;
    private final Integer maxTeamSize;
    private final String scoreboardName;
    private Team bukkitTeam;

    // Since the WarTeam is cloned upon usage, these variables reset.
    private int kills;
    private int deaths;
    /*  END RECORD  */

    /**
     * Creates an instance of this record,
     * With a team limit & its own unique identifier.
     * All separate methods will call this.
     *
     * @param teamName       The team's name.
     * @param teamColor      The team's color.
     * @param maxTeamSize    The maximum amount of players allowed on this team.
     * @param scoreboardName The team's scoreboard name.
     */
    private WarTeam(String teamName, ChatColor teamColor, Integer maxTeamSize, String scoreboardName) {
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.maxTeamSize = maxTeamSize;
        this.scoreboardName = scoreboardName;
        this.bukkitTeam = null;
        kills = 0;
        deaths = 0;
    }

    /**
     * Creates an instance of this record,
     * With no team limit & its own unique identifier.
     *
     * @param teamName       The team's name.
     * @param teamColor      The team's color.
     * @param scoreboardName The team's scoreboard name.
     */
    public WarTeam(String teamName, ChatColor teamColor, String scoreboardName) {
        this(teamName, teamColor, -1, scoreboardName);
    }

    /**
     * Creates an instance of this record,
     * With a team limit & no unique identifier.
     *
     * @param teamName    The team's name.
     * @param teamColor   The team's color.
     * @param maxTeamSize The maximum amount of players allowed on this team.
     */
    public WarTeam(String teamName, ChatColor teamColor, Integer maxTeamSize) {
        this(teamName, teamColor, maxTeamSize, teamName);
    }

    /**
     * Creates an instance of this record,
     * Without a team limit or a unique identifier.
     *
     * @param teamName  The team's name.
     * @param teamColor The team's color.
     */
    public WarTeam(String teamName, ChatColor teamColor) {
        this(teamName, teamColor, -1, teamName);
    }

    /**
     * Returns the name for the team that was
     * designated in the map configuration file.
     *
     * @return The Team's name.
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Returns the color for the team that was
     * designated in the map configuration file.
     *
     * @return The Team's designated color.
     */
    public ChatColor getTeamColor() {
        return teamColor;
    }

    /**
     * Returns the maximum number of players
     * allowed on this team at any given time.
     *
     * @return Maximum team size.
     */
    private Integer getMaxTeamSize() {
        return maxTeamSize;
    }

    /**
     * Returns the unique scoreboard identifier for this Team.
     *
     * @return The unique scoreboard identifier for this Team.
     */
    private String getScoreboardName() {
        return scoreboardName;
    }

    /**
     * Returns the Spigot Team object
     * used at runtime during a match.
     *
     * @return The Spgiot Team.
     */
    public Team getBukkitTeam() {
        return bukkitTeam;
    }

    /**
     * This method must be called on a cloned instantiation
     * of the class to implement Spigot's useful Team functions.
     *
     * @param bukkitTeam The Bukkit Team the gamemode will assign & control.
     */
    void setBukkitTeam(Team bukkitTeam) {
        this.bukkitTeam = bukkitTeam;
    }

    /**
     * Returns whether or not the team is accepting any more members.
     *
     * @return Whether team is full or not.
     */
    boolean isFull() {
        return bukkitTeam.getEntries().size() >= maxTeamSize;
    }

    /**
     * Returns useful information for those who hover over the team name.
     *
     * @return Hover information.
     */
    public TextComponent getHoverInformation() {
        TextComponent result = new TextComponent(getTeamColor() + "[" + getTeamName() + "]" + ChatColor.WHITE);
        result.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Members: " + getBukkitTeam().getEntries().size() + "/" + maxTeamSize + "\nKills: " + kills + "\nDeaths: " + deaths).create()));
        return result;
    }

    /**
     * Creates an instantiated copy of this class and
     * any relevant fields needed for manipulation.
     *
     * @return A clean, usable copy of this class for runtime.
     */
    @Override
    public WarTeam clone() {
        return new WarTeam(getTeamName(), getTeamColor(), getMaxTeamSize(), getScoreboardName());
    }

    /**
     * Makes toString() return the team's designated name.
     *
     * @return The team's name.
     */
    @Override
    public String toString() {
        return this.getTeamName();
    }

    /**
     * Returns a colored team name string.
     *
     * @return The colored team name.
     */
    public String getDisplayName() {
        return getTeamColor() + getTeamName() + ChatColor.WHITE;
    }

    /**
     * Adds a kill in favor of this team.
     */
    public void addKill() {
        kills++;
    }

    /**
     * Adds a death in spite of this team.
     */
    public void addDeath() {
        deaths++;
    }
}

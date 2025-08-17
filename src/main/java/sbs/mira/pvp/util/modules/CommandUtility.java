package sbs.mira.pvp.util.modules;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarMap;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.util.WarMatch;
import sbs.mira.pvp.framework.MiraModule;
import sbs.mira.pvp.game.Gamemode;
import sbs.mira.pvp.util.Cache;
import sbs.mira.pvp.MiraPvpMaster;
import sbs.mira.pvp.util.Match;
import com.sk89q.minecraft.util.commands.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles all player modules.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see com.sk89q.minecraft.util.commands.Command
 * @see org.bukkit.command.Command
 * <p>
 * Created by Josh on 21/04/2017.
 * @since 1.0
 */
public class CommandUtility extends MiraModule {

    @SuppressWarnings("unused") // This is used, just not directly.
    public CommandUtility(MiraPvpMaster main) {
        super(main);
    }

    /**
     * Listens to the phrases '/join' and '/j'.
     * If these are said by the player, perform
     * the join logic on them.
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"join", "j"},
            desc = "Join the match", // Brief description of the command.
            usage = "<preference>")
    public void join(CommandContext args, CommandSender sender) {
        if (!(sender instanceof Player)) return; // Only players can join. Console can also execute modules.
        MiraPlayer wp = mira().getWarPlayer(((Player) sender).getUniqueId());
        if (wp.isJoined()) {
            // Don't re-execute join logic if they are already joined.
            sender.sendMessage("You are already joined!");
            return;
        }

        // Does the player have a team preference?
        WarTeam preference = null;
        if (args.argsLength() > 0) {
            if (!mira().plugin().has_permission(sender, "war.preference")) {
                sender.sendMessage(ChatColor.RED + "You may not pick your team preference.");
                return;
            }
            preference = mira().cache().matchTeam(args.getJoinedStrings(0));
            if (preference == null) {
                sender.sendMessage(ChatColor.RED + "That team does not exist.");
                return;
            }
        }

        wp.setJoined(true); // Set them as joined.
        if (mira().match().getStatus() != WarMatch.Status.PLAYING)
            // If there is no match playing, notify the player that they will automatically join when there is
            sender.sendMessage("You will automatically join the next round.");
        else {
            if (preference == null)
                mira().match().getCurrentMode().entryHandle(wp); // Handle entry onto smallest team.
            else mira().match().getCurrentMode().entryHandle(wp, preference); // Otherwise handle entry of preference.
        }
    }

    /**
     * Listens to the phrases '/leave' and '/quit'.
     * If these are said by the player, perform
     * the leave logic on them.
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"leave", "quit"},
            desc = "Leave the match",
            max = 0)
    public void leave(CommandContext args, CommandSender sender) {
        if (!(sender instanceof Player)) return; // Only players can leave. Console can also execute modules.
        MiraPlayer wp = mira().getWarPlayer(((Player) sender).getUniqueId());
        if (!wp.isJoined()) {
            // Don't re-execute leave logic if they are not joined.
            sender.sendMessage("You are not marked as joined!");
            return;
        }
        wp.setJoined(false); // Set them as joined.
        if (mira().match().getStatus() != WarMatch.Status.PLAYING)
            // If there is no match playing, notify the player that they will automatically join when there is.
            sender.sendMessage("You will no longer automatically join the next round.");
        else {
            mira().match().getCurrentMode().entryHandle(wp); // If applicable, handle their entry.
            ((MiraPvpMaster) mira()).respawn().clearFor(wp); // Clear their respawning info if they have any.
        }
    }

    /**
     * Listens to the phrases '/rotation' and '/rot
     * If these are said by the player, show them
     * the currently loaded rotation.
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"rotation", "rot"},
            desc = "View the current rotation",
            max = 0)
    public void rotation(CommandContext args, CommandSender sender) {
        int currentPos = mira().match().rotationPoint;
        int nextPos = currentPos == mira().match().getRotationList().size() - 1 ? 0 : currentPos + 1;
        Match match = (Match) mira().match();

        sender.sendMessage("Current rotation:");
        for (int i = 0; i < mira().match().getRotationList().size(); i++) {
            if (currentPos == i) {
                // Is this the current map playing?
                if (match.wasSet()) {
                    // Is a /setnext map playing? Show that one instead.
                    sender.sendMessage((i + 1) + ". " + ChatColor.WHITE + match.getRotationList().get(i));
                    sender.sendMessage(ChatColor.YELLOW + "» " + ChatColor.WHITE + mira().cache().getCurrentMap().getMapName());
                } else
                    // Otherwise just show the regular rotation map playing.
                    sender.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". " + ChatColor.WHITE + match.getRotationList().get(i));
            } else if (nextPos == i) {
                // Is this the map next up?
                if (match.getSetNext() != null) {
                    // Is there a map set?
                    sender.sendMessage(ChatColor.GOLD + "» " + ChatColor.WHITE + match.getSetNext());
                    sender.sendMessage((i + 1) + ". " + ChatColor.WHITE + match.getRotationList().get(i));
                } else
                    // Otherwise just show the next map on the rotation.
                    sender.sendMessage(ChatColor.GOLD + "" + (i + 1) + ". " + ChatColor.WHITE + match.getRotationList().get(i));
            } else sender.sendMessage((i + 1) + ". " + ChatColor.WHITE + match.getRotationList().get(i));
        }
    }

    /**
     * Listens to the phrases '/vote' and '/v'.
     * If these are said by the player, perform
     * the vote logic on them.
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"vote", "v"},
            desc = "Vote for an available gamemode",
            usage = "<gamemode>", // Describes command arguments.
            min = 1,
            max = 1)
    public void vote(CommandContext args, CommandSender sender) {
        tryVote(sender, args.getString(0), false, false);
    }

    /**
     * Administrative command.
     * Listens to the phrase '/rig'
     * followed by the parameter(s).
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"rig"},
            desc = "Rig the vote for a gamemode",
            usage = "<gamemode>",
            flags = "s",
            min = 1)
    @CommandPermissions("war.mod")
    public void rig(CommandContext args, CommandSender sender) throws CommandNumberFormatException {
        tryVote(sender, args.getString(0), true, args.hasFlag('s'));
    }

    /**
     * Administrative command.
     * Listens to the phrases '/settime' and '/set',
     * followed by the parameter(s).
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"settime", "set"},
            desc = "Add, subtract, or set match time",
            usage = "<(+-)seconds>",
            min = 1)
    @CommandPermissions("war.admin")
    public void settime(CommandContext args, CommandSender sender) throws CommandNumberFormatException {
        if (mira().match().getStatus() != WarMatch.Status.PLAYING) {
            sender.sendMessage(ChatColor.RED + "There is no match playing.");
            return;
        }
        Gamemode currentMode = (Gamemode) mira().match().getCurrentMode();
        long duration = mira().cache().getCurrentMap().getMatchDuration();

        String time = args.getString(0);
        int result;
        switch (time.charAt(0)) {
            case '+':
                // Add time to the match.
                result = Integer.parseInt(time.substring(1, time.length()));
                break;
            case '-':
                // Subtract time.
                result = -Integer.parseInt(time.substring(1, time.length()));
                break;
            default:
                // Set the amount of time remaining.
                result = args.getInteger(0) - currentMode.getTimeElapsed();
                break;
        }

        currentMode.setTimeElapsed(currentMode.getTimeElapsed() + result);
        if (currentMode.getTimeElapsed() > duration) currentMode.setTimeElapsed((int) (duration - 0xA));
        else if (currentMode.getTimeElapsed() < 0) currentMode.setTimeElapsed(0x0);

        long minutes = ((duration - currentMode.getTimeElapsed()) / 0x3C); // Calculates number of minutes remaining.
        String s = (minutes == 1 ? "" : "s"); // Should it be 'minute' or 'minutes'?

        Bukkit.broadcastMessage(ChatColor.YELLOW + "There is now " + minutes + " minute" + s + " remaining!");
    }

    /**
     * Listens to the phrases '/endmatch' and '/endm'.
     * If these are said by the player, perform
     * the early end-match logic is called on them
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"endmatch", "endm"},
            desc = "Ends the match early",
            max = 0)
    @CommandPermissions("war.mod") // Give this a special permission so only administrators can use it
    public void end(CommandContext args, CommandSender sender) {
        if (mira().match().getStatus() == WarMatch.Status.PLAYING) {
            Bukkit.broadcastMessage(sender.getName() + " called an end to this match early");
            ((Gamemode) mira().match().getCurrentMode()).logEvent(sender.getName() + " ended this match early..");
            mira().match().getCurrentMode().onEnd(); // Calls onEnd() forcibly.
        }
    }

    /**
     * Listens to the phrases '/setnext' and '/sn'.
     * If these are said by the player, perform the
     * setnext logic with argument(s)
     *
     * @param args   The command context. Such as arguments, flags, etc.
     * @param sender The entity that sent the command. In this case, a player.
     * @see CommandContext
     * @see CommandSender
     */
    @Command(aliases = {"setnext", "sn"},
            desc = "Set the next map to play",
            usage = "<map>",
            min = 1)
    @CommandPermissions("war.mod")
    public void set(CommandContext args, CommandSender sender) {
        WarMap found = mira().cache().matchMap(args.getJoinedStrings(0));
        if (found == null) {
            sender.sendMessage(ChatColor.RED + "Error: Unknown map.");
            return;
        }
        ((Match) mira().match()).setNext(found);
        Bukkit.broadcastMessage(sender.getName() + " has set the next map to be " + found.getMapName());
    }

    /**
     * Tries to cast a vote.
     *
     * @param sender    Who is voting.
     * @param votingFor What they are voting for.
     * @param rig       Is it a rigged vote? (+1337 votes)
     * @param silent    If it is rigged, is it silent?
     */
    private void tryVote(CommandSender sender, String votingFor, boolean rig, boolean silent) {
        if (mira().match().getStatus() != WarMatch.Status.VOTING) {
            // Notify the player there is no vote being held and ignore all other logic.
            sender.sendMessage("There is no vote being held at the moment.");
            return;
        }

        Match match = (Match) mira().match(); // Since this procedure contains non-WarMatch functions, use Match instead.
        if (!rig && match.getVoted().contains(((Player) sender).getUniqueId())) {
            // Do not allow a player to vote twice. Das cheating. Kindof like real elections.
            sender.sendMessage("You have already voted!");
            return;
        }

        Gamemode.Mode selection = ((Cache) mira().cache()).matchMode(votingFor); // Match to the selected mode.
        if (selection != null) {
            // Increment the votes for this gamemode by 1, or 1337 if rigging.
            match.getVotes().put(selection, match.getVotes().get(selection) + (rig ? 1337 : 1));

            // Set this player as already voted, if not rigging.
            if (!rig) {
                match.getVoted().add(((Player) sender).getUniqueId());
                TextComponent comp = new TextComponent(((Player) sender).getName() + " voted for the gamemode ");
                comp.addExtra(selection.getDescriptionComponent(mira(), true));
                mira().broadcastSpigotMessage(comp);
            } else {
                // Warn staff and players(?) that vote was rigged.
                warnStaff(selection.getActualShortName() + " was rigged by " + sender.getName());
                if (!silent)
                    warnNonStaff("The vote was rigged to be " + selection.getActualShortName());
            }
        } else
            // Notify the player that their gamemode is not on the enum list, and is therefore not an option at all.
            sender.sendMessage("This gamemode is not valid.");
    }

    /**
     * Privately warns staff with a message.
     * Used by the admin command to alert other
     * staff when any sub command is used.
     *
     * @param message Warning message.
     */
    private void warnStaff(String message) {
        for (Player online : Bukkit.getOnlinePlayers())
            if (mira().plugin().has_permission(online, "war.staff"))
                online.sendMessage(ChatColor.YELLOW + "Staff: " + message);
        Bukkit.getConsoleSender().sendMessage(message);
    }

    /**
     * Publicly warns players with a message.
     * Used by the admin command to alert normal
     * players to the usage of an admin command
     * if the silent flag was not used.
     *
     * @param message Warning message.
     */
    private void warnNonStaff(String message) {
        for (Player online : Bukkit.getOnlinePlayers())
            if (!mira().plugin().has_permission(online, "war.staff"))
                online.sendMessage(ChatColor.YELLOW + "Warning: " + message);
        Bukkit.getConsoleSender().sendMessage(message); // Also writes message to console as well.
    }
}

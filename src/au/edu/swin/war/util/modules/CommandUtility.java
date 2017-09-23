package au.edu.swin.war.util.modules;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarMap;
import au.edu.swin.war.framework.util.WarMatch;
import au.edu.swin.war.framework.util.WarModule;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.util.Manager;
import au.edu.swin.war.util.Match;
import com.sk89q.minecraft.util.commands.ChatColor;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles all player commands.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see com.sk89q.minecraft.util.commands.Command
 * @see org.bukkit.command.Command
 * <p>
 * Created by Josh on 21/04/2017.
 * @since 1.0
 */
public class CommandUtility extends WarModule {

    @SuppressWarnings("unused") // This is used, just not directly.
    public CommandUtility(Manager main) {
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
            max = 0) // The maximum number of arguments allowed.
    public void join(CommandContext args, CommandSender sender) {
        if (!(sender instanceof Player)) return; // Only players can join. Console can also execute commands.
        WarPlayer wp = main().getWarPlayer(((Player) sender).getUniqueId());
        if (wp.isJoined()) {
            // Don't re-execute join logic if they are already joined.
            sender.sendMessage("You are already joined!");
            return;
        }
        wp.setJoined(true); // Set them as joined.
        if (main().match().getStatus() != WarMatch.Status.PLAYING)
            // If there is no match playing, notify the player that they will automatically join when there is.
            sender.sendMessage("You will automatically join the next round.");
        else
            main().match().getCurrentMode().entryHandle(wp); // If applicable, handle their entry.
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
        if (!(sender instanceof Player)) return; // Only players can leave. Console can also execute commands.
        WarPlayer wp = main().getWarPlayer(((Player) sender).getUniqueId());
        if (!wp.isJoined()) {
            // Don't re-execute leave logic if they are not joined.
            sender.sendMessage("You are not marked as joined!");
            return;
        }
        wp.setJoined(false); // Set them as joined.
        if (main().match().getStatus() != WarMatch.Status.PLAYING)
            // If there is no match playing, notify the player that they will automatically join when there is.
            sender.sendMessage("You will no longer automatically join the next round.");
        else {
            main().match().getCurrentMode().entryHandle(wp); // If applicable, handle their entry.
            ((Manager) main()).respawn().clearFor(wp); // Clear their respawning info if they have any.
        }
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
    @CommandPermissions("war.admin") // Give this a special permission so only administrators can use it
    public void end(CommandContext args, CommandSender sender) {
        if (main().match().getStatus() == WarMatch.Status.PLAYING) {
            Bukkit.broadcastMessage(sender.getName() + " called an end to this match early");
            ((Gamemode) main().match().getCurrentMode()).logEvent(sender.getName() + " ended this match early..");
            main().match().getCurrentMode().onEnd(); // Calls onEnd() forcibly.
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
        int currentPos = main().match().rotationPoint;
        int nextPos = currentPos == main().match().getRotationList().size() - 1 ? 0 : currentPos + 1;

        sender.sendMessage("Current rotation:");
        for (int i = 0; i < main().match().getRotationList().size(); i++) {
            // Show current map playing if the rotation is not 1 map long.
            if (currentPos != nextPos && i == currentPos) {
                if (((Match) main().match()).getSetNext() != null) {
                    // If a map is set to play next
                    sender.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". " + ChatColor.WHITE + main().match().getRotationList().get(i));
                    sender.sendMessage(ChatColor.GOLD + "?. " + ChatColor.WHITE + ((Match) main().match()).getSetNext());
                } else if (!main().cache().getCurrentMap().wasSet())
                    // Otherwise just show the map playing if it wasn't set
                    sender.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". " + ChatColor.WHITE + main().match().getRotationList().get(i));
                else
                    // Otherwise just show it as normal
                    sender.sendMessage((i + 1) + ". " + ChatColor.WHITE + main().match().getRotationList().get(i));
            } else if (i == nextPos && ((Match) main().match()).getSetNext() == null)
                // Show next map if one has not been set
                sender.sendMessage(ChatColor.GOLD + "" + (nextPos + 1) + ". " + ChatColor.WHITE + main().match().getRotationList().get(nextPos));
            else sender.sendMessage((i + 1) + ". " + ChatColor.WHITE + main().match().getRotationList().get(i));
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
    @CommandPermissions("war.admin")
    public void set(CommandContext args, CommandSender sender) {
        WarMap found = main().cache().matchMap(args.getJoinedStrings(0));
        if (found == null) {
            sender.sendMessage(ChatColor.RED + "Error: Unknown map.");
            return;
        }
        ((Match) main().match()).set(found);
        Bukkit.broadcastMessage(sender.getName() + " has set the next map to be " + found.getMapName());
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
        if (!(sender instanceof Player)) return; // Only players can leave. Console can also execute commands.
        if (main().match().getStatus() != WarMatch.Status.VOTING) {
            // Notify the player there is no vote being held and ignore all other logic.
            sender.sendMessage("There is no vote being held at the moment.");
            return;
        }
        Match match = (Match) main().match(); // Since this procedure contains non-WarMatch functions, use Match instead.
        if (match.getVoted().contains(((Player) sender).getUniqueId())) {
            // Do not allow a player to vote twice. Das cheating. Kindof like real elections.
            sender.sendMessage("You have already voted!");
            return;
        }
        List<Gamemode.Mode> available = new ArrayList<>(); // Create a list of gamemodes that are allowed to be voted for.
        Collections.addAll(available, ((Map) main().cache().getCurrentMap()).getGamemodes()); // Add the available gamemodes to the list.
        //TODO: Make case sensitive for KoTH?

        String votedFor = args.getString(0); // Returns the gamemode they voted for, as a String.
        try {
            Gamemode.Mode vote = Gamemode.Mode.valueOf(votedFor);
            if (available.contains(vote)) {
                // Increment the votes for this gamemode by 1.
                match.getVotes().put(vote, match.getVotes().get(vote) + 1);
                // Set this player as already voted.
                match.getVoted().add(((Player) sender).getUniqueId());
                TextComponent comp = new TextComponent(((Player) sender).getDisplayName() + " voted for the gamemode ");
                comp.addExtra(vote.getDescriptionComponent(main(), true));
                main().broadcastSpigotMessage(comp);
            } else {
                // Notify the player that they have entered a valid gamemode, but it is not available on this map.
                // Also show them what they can vote for because we're nice.. right?
                //TODO: Fix this?
                //sender.sendMessage("That gamemode is not available on this map.\n Available gamemodes: " + Gamemode.Mode.format(((Map) main().cache().getCurrentMap()).getGamemodes(), main()));
            }
        } catch (IllegalArgumentException ex) {
            // Notify the player that their gamemode is not on the enum list, and is therefore not an option at all.
            sender.sendMessage("Your input is not a valid gamemode.");
        }
    }
}

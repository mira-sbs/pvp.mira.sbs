package au.edu.swin.war.game;

import au.edu.swin.war.framework.game.WarMap;
import au.edu.swin.war.framework.stored.Activatable;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.util.Match;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An extension to WarMap.
 * <p>
 * This is the class that should be extended for map
 * configurations.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarMap
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public abstract class Map extends WarMap {

    protected Map() {
        super();
        attr().put("itemMerging", true); // Makes items merge by default.
    }

    private Gamemode.Mode[] modes; // The defined gamemodes for this map.

    /**
     * Returns the gamemodes that are allowed to
     * be played on this map. For internal usage.
     *
     * @return Available gamemodes.
     */
    public Gamemode.Mode[] getGamemodes() {
        return modes;
    }

    /**
     * Defines the gamemodes that are allowed to
     * be played on this map.
     *
     * @see Gamemode.Mode
     */
    protected void setGamemodes(Gamemode.Mode[] modes) {
        this.modes = modes;
    }

    /**
     * Sets at which block building should be allowed on
     * a 'plateau' map. Ask Josh about this if needed.
     *
     * @param y Plateau Y.
     */
    protected void setPlateauY(int y) {
        attr().put("plateau", y);
    }

    /**
     * Adds a CTF flag and automatically creates the
     * HashMap if it does not exist already.
     *
     * @param teamName Name of team who owns the flag.
     * @param location Location of this flag.
     */
    protected void addCTFFlag(String teamName, SerializedLocation location) {
        if (!attr().containsKey("flags"))
            attr().put("flags", new HashMap<String, SerializedLocation>());
        ((HashMap<String, SerializedLocation>) attr().get("flags")).put(teamName, location);
    }


    /**
     * Creates a map-specific gadget that should be declared final.
     *
     * @param material Material to make the gadget from.
     * @param data     ItemStack data, if any.
     * @param name     Name of gadget.
     * @param lore     Gadget description. (multiple lines allowed)
     */
    protected ItemStack createGadget(Material material, int amount, int data, String name, String... lore) {
        //TODO: pls make use item, but can't be used in constructors
        ArrayList<String> loreList = new ArrayList<>();
        for (Object st : lore)
            loreList.add(st.toString());
        ItemStack result = new ItemStack(material, amount, (short) data);
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + name);
        meta.setLore(loreList);
        result.setItemMeta(meta);
        result.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        return result;
    }

    /**
     * Tries to take an ItemStack from a player's inventory, and returns
     * true if this was successful.
     *
     * @param event  Interaction event.
     * @param toTake Item to take.
     * @return Was this successful?
     */
    protected boolean useGadget(PlayerEvent event, EquipmentSlot hand, ItemStack toTake, boolean autoTake) {
        PlayerInventory inv = event.getPlayer().getInventory();
        // Only take one of the gadget.
        toTake = toTake.clone();

        ItemStack inHand;
        switch (hand) {
            case HAND:
                inHand = inv.getItemInMainHand();
                break;
            case OFF_HAND:
                inHand = inv.getItemInOffHand();
                break;
            default:
                return false;
        }
        toTake.setAmount(inHand.getAmount()); // Set the comparison amount equal to the amount in their hand.

        if (inHand.equals(toTake)) {
            if (autoTake)
                inHand.setAmount(inHand.getAmount() - 1);
            return true;
        } else return false;
    }

    /**
     * Returns true if a desired action was taken.
     *
     * @param event          Event to analyse.
     * @param desiredActions Desired actions to take.
     * @return Whether or not desired action was taken.
     */
    protected boolean isAction(PlayerInteractEvent event, Action... desiredActions) {
        if (desiredActions != null)
            for (Action action : desiredActions)
                if (event.getAction() == action) return true;
        return false;
    }

    @Override
    public void activate() {
        // Register any map-specific events for the duration of the match.
        main.plugin().getServer().getPluginManager().registerEvents(this, main.plugin());
        // Activate the gamemode and begin the match!
        main.match().getCurrentMode().activate();

        setActive(true); // Sets the state of this map to active.
        postStart(); // Calls postStart(), see documentation.

        // Activates map objectives.
        for (Activatable obj : objectives())
            obj.activate();
    }

    @Override
    public void deactivate() {
        // Calls reset(), see documentation.
        reset();

        // De-activates the gamemode and begins the cycle.
        main.match().getCurrentMode().deactivate();

        // De-activates map objectives.
        for (Activatable obj : objectives())
            obj.deactivate();

        setActive(false); // Sets the state of this map to inactive.
        HandlerList.unregisterAll(this); // Unregister map-specific events.
    }

    /**
     * This method should spawn players around the spectator
     * spawn to avoid a giant clusterbomb of players.
     * <p>
     * For all intents and purposes, this is not needed yet.
     *
     * @return Spec spawn.
     * @since 1.0
     */
    public Location getSpectatorSpawn() {
        // Convert the serialized location to a Spigot location.
        return specSpawn.toLocation(main.match().getCurrentWorld(), true);
    }

    /**
     * This function is the same as above, except it
     * crafts the location from the previous match's
     * world.
     *
     * @return Spec spawn.
     * @since 1.0
     */
    public Location getSpectatorSpawn_() {
        // Convert the serialized location to a Spigot location.
        return specSpawn.toLocation(((Match) main.match()).getPreviousWorld(), true);
    }
}

package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarMap;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

/**
 * A template map configuration.
 * Feel free to adapt, or modify.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public class Template extends Map {

    // Initialise your creator array as UUIDs {}, from strings.
    private UUID[] creators = {UUID.fromString("cb211791-6ef6-483b-aa67-4ec1d0637715")};
    // Give your map a name.
    private String mapName = "Map Battle II";
    // Propagate your array with materials you don't want dropped on death.
    private Material[] disabledDrops = new Material[]{Material.AIR};;
    // Define what gamemodes will be on your map.
    private Gamemode.Mode[] gamemodes = {Gamemode.Mode.TDM};

    // Design your teams!
    private WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 15);
    private WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 15);

    // Private modifiers are not needed, but are good Java practice.

    /**
     * Attributes for your map should be called here.
     * For full procedures, see the following classes.
     *
     * @see WarMap
     * @see Map
     */
    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
    }

    /**
     * Spawns associates with teams should be added
     * here. Sample usages of the procedures are shown here.
     */
    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(x, y, z, yaw, pitch));
        addTeamSpawn(team2, new SerializedLocation(x, y, z, yaw, pitch));
        setSpectatorSpawn(new SerializedLocation(x, y, z, yaw, pitch));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        // Set armor.
        inv.setHelmet(new ItemStack(Material.IRON_HELMET));
        inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        inv.setBoots(new ItemStack(Material.IRON_BOOTS));

        // Set inventory contents. Here's a starter kit.
        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.STONE_PICKAXE));
        inv.setItem(3, new ItemStack(Material.ARROW, 16)); // This one contains an amount.
        inv.setItem(4, new ItemStack(Material.COOKED_BEEF, 4));
    }
}

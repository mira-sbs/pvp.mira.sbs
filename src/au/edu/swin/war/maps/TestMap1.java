package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

/**
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public class TestMap1 extends Map {

    private UUID[] creators = {UUID.fromString("1cb02f9e-4eee-479e-8df8-b375276eb7f6"), UUID.fromString("a40cdbc0-ce09-4c56-a1a8-7732394b6ad4")};
    private String mapName = "Test Map 1";
    private Material[] disabledDrops = defaultDisabledDrops();
    private Gamemode.Mode[] gamemodes = {Gamemode.Mode.KOTH, Gamemode.Mode.TDM};

    private WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 15);
    private WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 15);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(false, false);
        attr().put("kothFlag", new SerializedLocation(0, 61, 0));
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(11, 59, 0, 90, 0));
        addTeamSpawn(team2, new SerializedLocation(-11, 59, 0, 270, 0));
        setSpectatorSpawn(new SerializedLocation(0, 65, 14, 180, -10));
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

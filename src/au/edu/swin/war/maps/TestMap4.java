package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.modes.DTM;
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
public class TestMap4 extends Map {

    private final UUID[] creators = {UUID.fromString("1cb02f9e-4eee-479e-8df8-b375276eb7f6"), UUID.fromString("a40cdbc0-ce09-4c56-a1a8-7732394b6ad4")};
    private final String mapName = "Test Map 4";
    private final Material[] disabledDrops = defaultDisabledDrops();
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.DTM, Gamemode.Mode.TDM, Gamemode.Mode.LP, Gamemode.Mode.LTS};

    private final WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 9);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 9);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(-21.5, 100, -20.5, 270, 0));
        addTeamSpawn(team2, new SerializedLocation(22.5, 100, 21.5, 90, 0));
        setSpectatorSpawn(new SerializedLocation(0.5, 114, 0.5, 0, 90));

        objectives().add(new DTM.Monument(-2, 95, -23, 2, 98, -19, Material.MOSSY_COBBLESTONE, team1, main));
        objectives().add(new DTM.Monument(-2, 95, 19, 2, 98, 23, Material.MOSSY_COBBLESTONE, team2, main));
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
        inv.setItem(2, new ItemStack(Material.IRON_PICKAXE));
        inv.setItem(3, new ItemStack(Material.COOKED_BEEF, 4));
        inv.setItem(4, new ItemStack(Material.COBBLESTONE, 16));
        inv.setItem(9, new ItemStack(Material.ARROW, 16)); // This one contains an amount.
    }
}

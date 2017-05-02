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

import java.util.HashMap;
import java.util.UUID;

/**
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public class TestMap2 extends Map {

    private final UUID[] creators = {UUID.fromString("1cb02f9e-4eee-479e-8df8-b375276eb7f6")};
    private final String mapName = "Test Map 2";
    private final Material[] disabledDrops = defaultDisabledDrops();
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.CTF, Gamemode.Mode.TDM, Gamemode.Mode.LP, Gamemode.Mode.LTS};

    private final WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 8);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 8);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);

        attr().put("flags", new HashMap<String, SerializedLocation>());
        ((HashMap<String, SerializedLocation>) attr().get("flags")).put(team1.getTeamName(), new SerializedLocation(14, 109, 0));
        ((HashMap<String, SerializedLocation>) attr().get("flags")).put(team2.getTeamName(), new SerializedLocation(-14, 109, 0));
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(14.5, 110, -17.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(-14.5, 110, -17.5, 0, 0));
        setSpectatorSpawn(new SerializedLocation(0.5, 126, -40.5, 0, -10));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        inv.setHelmet(new ItemStack(Material.IRON_HELMET));
        inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        inv.setBoots(new ItemStack(Material.IRON_BOOTS));

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.STONE_PICKAXE));
        inv.setItem(3, new ItemStack(Material.COOKED_BEEF, 4));
        inv.setItem(4, new ItemStack(Material.LOG, 4));
        inv.setItem(9, new ItemStack(Material.ARROW, 16)); // This one contains an amount.
    }
}

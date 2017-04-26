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
public class TestMap3 extends Map {

    private final UUID[] creators = {UUID.fromString("a40cdbc0-ce09-4c56-a1a8-7732394b6ad4")};
    private final String mapName = "Test Map 3";
    private final Material[] disabledDrops = defaultDisabledDrops();
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.FFA, Gamemode.Mode.LMS};

    private final WarTeam team1 = new WarTeam("Green Team", ChatColor.GREEN, 32);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(13.5, 66, -13.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(0.5, 66, -23.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(-23.5, 66, 0.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(-13.5, 66, 13.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(0.5, 66, 23.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(13.6, 66, 13.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(23.5, 66, 0.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(-13.5, 66, -13.5, 0, 0));
        setSpectatorSpawn(new SerializedLocation(0.5, 81, 0.5, 0, 90));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        inv.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        inv.setBoots(new ItemStack(Material.IRON_BOOTS));

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.COOKED_BEEF, 4));
        inv.setItem(4, new ItemStack(Material.ARROW, 16)); // This one contains an amount.
    }
}

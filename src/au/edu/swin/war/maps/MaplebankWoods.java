package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.modes.DTM;
import au.edu.swin.war.game.modes.KoTH;
import au.edu.swin.war.game.util.SpawnArea;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class MaplebankWoods extends Map {

    private final UUID[] creators = {UUID.fromString("2cfb556d-55f9-4fa3-8043-199a15d11f40")};
    private final String mapName = "Maplebank Woods";
    private final Material[] disabledDrops = defaultDisabledDrops();
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.DTM, Gamemode.Mode.LP, Gamemode.Mode.LTS, Gamemode.Mode.KOTH};

    private final WarTeam team1 = new WarTeam("Forest Team", ChatColor.DARK_GREEN, 25);
    private final WarTeam team2 = new WarTeam("River Team", ChatColor.DARK_AQUA, 25);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(false, false);
        objectives().add(new DTM.Monument(-13, 102, 33, -11, 106, 35, team1, Material.OBSIDIAN, main));
        objectives().add(new DTM.Monument(42, 103, 35, 38, 108, 33, team2, Material.OBSIDIAN, main));
        objectives().add(new SpawnArea(main, -18, 85, -8, 94, false, false));
        objectives().add(new SpawnArea(main, 36, 85, 46, 94, false, false));
        attr().put("kothFlag", new SerializedLocation(14, 100, -7));
        setTimeLockTime(14000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(-13.5, 90, 90.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(41.5, 90, 90.5, 180, 0));
        setSpectatorSpawn(new SerializedLocation(14.5, 97, 114.5, 180, 0));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.LEATHER_HELMET, Material.IRON_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.IRON_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD, 1));
        inv.setItem(1, new ItemStack(Material.BOW, 1));
        inv.setItem(2, new ItemStack(Material.DIAMOND_PICKAXE, 1));
        inv.setItem(3, new ItemStack(Material.STONE_AXE, 1));
        inv.setItem(4, new ItemStack(Material.COOKED_BEEF, 6));
        inv.setItem(5, new ItemStack(Material.GOLDEN_APPLE, 2));
        inv.setItem(6, new ItemStack(Material.LOG, 16));
        inv.setItem(10, new ItemStack(Material.ARROW, 32));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Location equiv = event.getBlock().getLocation().clone();
        equiv.setY(59);
        if (equiv.getBlock().getType() != Material.BEDROCK) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Location equiv = event.getBlock().getLocation().clone();
        equiv.setY(59);
        if (equiv.getBlock().getType() != Material.BEDROCK) event.setCancelled(true);
    }
}

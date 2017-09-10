package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.modes.DTM;
import au.edu.swin.war.game.util.SpawnArea;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class TheRebellion extends Map {

    private final UUID[] creators = {UUID.fromString("2cfb556d-55f9-4fa3-8043-199a15d11f40")};
    private final String mapName = "The Rebellion";
    private final Material[] disabledDrops = defaultDisabledDrops();
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.DTM, Gamemode.Mode.LTS, Gamemode.Mode.TDM};

    private final WarTeam team1 = new WarTeam("Protectors", ChatColor.RED, 30);
    private final WarTeam team2 = new WarTeam("Invaders", ChatColor.BLUE, 30);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(true, true);
        objectives().add(new DTM.Monument(-96, 137, 73, -93, 139, 76, team1, Material.PRISMARINE, main));
        objectives().add(new DTM.Monument(-23, 89, 32, -21, 92, 34, team2, Material.OBSIDIAN, main));
        objectives().add(new SpawnArea(main, -28, 10, -25, 13, false, true));
        objectives().add(new SpawnArea(main, -15, 37, -12, 40, false, true));
        objectives().add(new SpawnArea(main, -72, 95, -62, 105, false, true));
        objectives().add(new SpawnArea(main, -117, 38, -107, 48, false, true));
        setTimeLockTime(12000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(-112.5, 146, 43.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(-67.5, 146, 100.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(-13, 89, 39, 135, 0));
        addTeamSpawn(team2, new SerializedLocation(-26, 89, 12, 45, 0));
        setSpectatorSpawn(new SerializedLocation(-12.5, 96, 19.5, 45, -15));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.DIAMOND_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.DIAMOND_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD, 1));
        inv.setItem(1, new ItemStack(Material.BOW, 1));
        switch (target.getCurrentTeam().getTeamColor()) {
            case RED:
                inv.setItem(2, new ItemStack(Material.DIAMOND_PICKAXE, 1));
                break;
            case BLUE:
                inv.setItem(2, new ItemStack(Material.IRON_PICKAXE, 1));
                break;
        }
        inv.setItem(3, new ItemStack(Material.IRON_AXE, 1));
        inv.setItem(4, new ItemStack(Material.COOKED_BEEF, 6));
        inv.setItem(5, main.items().createPotion(PotionEffectType.HEAL, 0, 1, 1));
        inv.setItem(6, new ItemStack(Material.LOG, 16));
        inv.setItem(9, new ItemStack(Material.ARROW, 32));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Location equiv = event.getBlock().getLocation().clone();
        equiv.setY(65);
        if (equiv.getBlock().getType() != Material.BEDROCK) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Location equiv = event.getBlock().getLocation().clone();
        equiv.setY(65);
        if (equiv.getBlock().getType() != Material.BEDROCK) event.setCancelled(true);
    }
}

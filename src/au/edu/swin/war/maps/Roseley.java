package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.util.RadialSpawnPoint;
import au.edu.swin.war.game.util.SpawnArea;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class Roseley extends Map {

    private final UUID[] creators = {id("2e1c067c-6f09-4db0-8cd7-defc12ce622e"), id("a40cdbc0-ce09-4c56-a1a8-7732394b6ad4")};
    private final String mapName = "Roseley";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.TDM, Gamemode.Mode.LTS, Gamemode.Mode.LP, Gamemode.Mode.CTF};

    private final WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 20);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 20);

    private final ItemStack GRAPPLE_HOOK = createGadget(Material.FISHING_ROD, 1, 0, "Grapple Hook", "Throw, and pull!");

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(false, false);
        objectives().add(new SpawnArea(main, -59, 56, -45, 59, true, true));
        objectives().add(new SpawnArea(main, -10, 1, -1, 10, true, true));
        addCTFFlag(team1.getTeamName(), new SerializedLocation(-30, 74, 10));
        addCTFFlag(team2.getTeamName(), new SerializedLocation(-34, 76, 54));
        setTimeLockTime(11000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new RadialSpawnPoint(main.rng, -5.5, 68, 5.5, 45, 0, 3, 3));
        addTeamSpawn(team2, new RadialSpawnPoint(main.rng, -52.5, 68, 58.5, 180, 0, 7, 1));
        setSpectatorSpawn(new RadialSpawnPoint(main.rng, -47.5, 74, 5.5, 315, 0, 1, 1));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.IRON_HELMET, Material.LEATHER_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.COOKED_CHICKEN, 5));
        inv.setItem(3, main.items().createPotion(PotionEffectType.HEAL, 0, 1, 1));
        inv.setItem(8, new ItemStack(Material.EXP_BOTTLE, 5));
        inv.setItem(27, new ItemStack(Material.ARROW, 28));
        inv.setItem(4, GRAPPLE_HOOK);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getCaught() != null) return;
        if (!useGadget(event, EquipmentSlot.HAND, GRAPPLE_HOOK, false) && !useGadget(event, EquipmentSlot.OFF_HAND, GRAPPLE_HOOK, false))
            return;
        Player pl = event.getPlayer();
        Location bobber = event.getHook().getLocation();
        if (event.getState() == PlayerFishEvent.State.IN_GROUND || (!bobber.getBlock().getType().isTransparent() && !bobber.getBlock().getRelative(BlockFace.DOWN).getType().isTransparent())) {
            pl.setFallDistance(0);
            pl.playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_SHOOT, 4, 4);
            pl.setVelocity(bobber.toVector().subtract(pl.getLocation().toVector()).multiply(0.1));
        }
    }
}

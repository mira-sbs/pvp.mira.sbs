package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.modes.DDM;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class ConvenienceWars extends Map {

    private final UUID[] creators = {UUID.fromString("2cfb556d-55f9-4fa3-8043-199a15d11f40")};
    private final String mapName = "Convenience Wars";
    private final Material[] disabledDrops = defaultDisabledDrops();
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.DDM, Gamemode.Mode.LTS};

    private final WarTeam team1 = new WarTeam("Coles Clerks", ChatColor.RED, 25);
    private final WarTeam team2 = new WarTeam("Aldi Clerks", ChatColor.DARK_GREEN, 25);

    private final ItemStack GADGET = createGadget(Material.SULPHUR, 0, "Emergency Exit", "Sends you flying backward");

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(false, false);
        objectives().add(new DDM.Territory(123, 20, -37, 123, 22, -35, team1, main));
        objectives().add(new DDM.Territory(26, 20, -34, 26, 22, -32, team2, main));
        setTimeLockTime(18000);
        setMatchDuration(450);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(86.5, 20, -53.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(102.5, 20, -53.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(110.5, 20, -53.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(94.5, 20, -43.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(86.5, 20, -16.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(102.5, 20, -16.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(118.5, 20, -16.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(126.5, 20, -16.5, 180, 0));

        addTeamSpawn(team2, new SerializedLocation(47.5, 20, -16.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(55.5, 20, -26.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(39.5, 20, -16.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(63.5, 20, -16.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(63.5, 20, -53.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(47.5, 20, -53.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(31.5, 20, -53.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(23.5, 20, -53.5, 0, 0));

        setSpectatorSpawn(new SerializedLocation(75, 23, -1.5, 180, 0));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.LEATHER_HELMET, Material.IRON_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.CHAINMAIL_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.PUMPKIN_PIE, 2));
        inv.setItem(3, main.items().createPotion(PotionEffectType.HEAL, 0, 1, 1));
        inv.setItem(4, GADGET);
        inv.setItem(27, main.items().createTippedArrow(PotionEffectType.SLOW, 11 * 20, 0, 6));
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (!isAction(event, Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
        if (!useGadget(event.getPlayer().getInventory(), GADGET)) return;
        Player pl = event.getPlayer();
        pl.setVelocity(pl.getLocation().getDirection().multiply(-2.5).setY(0.1));
    }
}

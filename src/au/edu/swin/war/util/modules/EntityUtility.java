package au.edu.swin.war.util.modules;


import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.framework.util.WarModule;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

/**
 * This class handles all procedures or functions
 * relating to Spigot entities, such as players,
 * monsters, animals, etc.
 * <p>
 * Created by Josh on 23/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see Entity
 * @since 1.0
 */
public class EntityUtility extends WarModule {

    /**
     * Entity utility constructor.
     * We need to link back to the manager and plugin.
     *
     * @param main The supercontroller.
     */
    public EntityUtility(WarManager main) {
        super(main);
        this.rng = new Random();
    }

    private final Random rng; // Provided random number generator.

    /**
     * Spawns a firework with a random shape and random color.
     * This will spawn at the location specified. This procedure
     * should be called when necessary to pretty up things.
     *
     * @param l The location for the firework to be spawned at.
     */
    public void spawnFirework(Location l) {
        spawnFirework(l, ChatColor.values()[rng.nextInt(ChatColor.values().length)]);
    }

    /**
     * Spawns a firework with a random shape, but defined color.
     * This will spawn at the location specified. This procedure
     * should be called when necessary to pretty up things.
     *
     * @param l     The location for the firework to be spawned at.
     * @param color The color of the firework.
     */
    @SuppressWarnings("UnusedAssignment")
    public void spawnFirework(Location l, ChatColor color) {
        Firework fw = (Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        FireworkEffect.Type type;
        switch (rng.nextInt(5) + 1) {
            case 1:
                type = FireworkEffect.Type.BALL;
            case 2:
                type = FireworkEffect.Type.BALL_LARGE;
            case 3:
                type = FireworkEffect.Type.BURST;
            case 4:
                type = FireworkEffect.Type.STAR;
            case 5:
                type = FireworkEffect.Type.CREEPER;
            default:
                type = FireworkEffect.Type.STAR;
        }
        Color fade = getColor(rng.nextInt(17) + 1);
        fwm.addEffect(FireworkEffect.builder().flicker(rng.nextBoolean()).withColor(main().strings().convertChatToDye(color)).withFade(fade).with(type).trail(rng.nextBoolean()).build());
        fwm.setPower(rng.nextInt(2) + 1);
        fw.setFireworkMeta(fwm);
    }

    /**
     * Gets a random firework color from an integer.
     * Given a number from 1-17, a color is returned.
     * Used with spawnFirework().
     *
     * @param r The integer to case.
     * @return The resulting color.
     */
    private Color getColor(int r) {
        switch (r) {
            case 1:
                return Color.AQUA;
            case 2:
                return Color.BLACK;
            case 3:
                return Color.BLUE;
            case 4:
                return Color.FUCHSIA;
            case 5:
                return Color.GRAY;
            case 6:
                return Color.GREEN;
            case 7:
                return Color.LIME;
            case 8:
                return Color.MAROON;
            case 9:
                return Color.NAVY;
            case 10:
                return Color.OLIVE;
            case 11:
                return Color.ORANGE;
            case 12:
                return Color.PURPLE;
            case 13:
                return Color.RED;
            case 14:
                return Color.SILVER;
            case 15:
                return Color.TEAL;
            case 16:
                return Color.WHITE;
            case 17:
                return Color.YELLOW;
            default:
                return Color.WHITE;
        }
    }
}
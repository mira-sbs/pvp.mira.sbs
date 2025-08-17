package sbs.mira.pvp.framework.stored;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * This class is fundamentally the same as Spigot's Location
 * class (or record, since it holds extra data). However, this
 * 'Serialized' location does not require you to store an apparent
 * world, and only stores the XYZ coordinates of the location, to
 * which you may translate back into a Spigot Location provided
 * you supply a valid World instance using the Spigot API.
 * <p>
 * To use this class, use any of the 3 constructors below to
 * initialize and make use of the object.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see org.bukkit.Location
 * <p>
 * Created by Josh on 27/03/2017.
 * @since 1.0
 */
public class SerializedLocation {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    /**
     * Main constructor for this class.
     *
     * @param x     The X coordinate.
     * @param y     The Y coordinate.
     * @param z     The Z coordinate.
     * @param yaw   Yaw is the player's head rotation. (-90 to 90 degrees)
     * @param pitch Pitch is the player's body rotation. (360 degrees)
     */
    public SerializedLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * This constructor allows for cleaner code where a
     * pitch and yaw are not defined. Defaults are 0.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param z The Z coordinate.
     */
    public SerializedLocation(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    /**
     * This function translates this basic extension of
     * an XYZ location back into Spigot's Location implementation.
     *
     * @param world The world the XYZ coordinate is in.
     * @param pitch Whether or not to include pitch.
     * @return The resultant Location object.
     * @see org.bukkit.Location
     */
    public Location toLocation(World world, boolean pitch) {
        if (pitch) return new Location(world, x, y, z, yaw, this.pitch);
        return new Location(world, x, y, z);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }
}

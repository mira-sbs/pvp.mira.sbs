package au.edu.swin.war.game.util;

import au.edu.swin.war.framework.stored.SerializedLocation;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Random;

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
 * @see Location
 * <p>
 * Created by Josh on 27/03/2017.
 * @since 1.0
 */
public class RadialSpawnPoint extends SerializedLocation {

    /* Getters and setters are not required for these variables. */
    private final int radiusX, radiusZ;

    private final Random rng;

    /**
     * Main constructor for this class.
     *
     * @param x       The X coordinate.
     * @param y       The Y coordinate.
     * @param z       The Z coordinate.
     * @param yaw     Yaw is the player's head rotation. (-90 to 90 degrees)
     * @param pitch   Pitch is the player's body rotation. (360 degrees)
     * @param radiusX How far the generated location can deviate on the X axis both ways.
     * @param radiusZ How far the generated location can deviate on the Z axis both ways.
     */
    public RadialSpawnPoint(Random rng, double x, double y, double z, float yaw, float pitch, int radiusX, int radiusZ) {
        super(x, y, z, yaw, pitch);
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
        this.rng = rng;
    }

    /**
     * This constructor allows for cleaner code where a
     * pitch and yaw are not defined. Defaults are 0.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param z The Z coordinate.
     */
    public RadialSpawnPoint(Random rng, double x, double y, double z, int radiusX, int radiusZ) {
        this(rng, x, y, z, 0, 0, radiusX, radiusZ);
    }

    /**
     * This function translates this basic extension of
     * an XYZ location back into Spigot's Location implementation.
     *
     * @param world The world the XYZ coordinate is in.
     * @param pitch Whether or not to include pitch.
     * @return The resultant Location object.
     * @see Location
     */
    @Override
    public Location toLocation(World world, boolean pitch) {
        double dx, dz;
        dx = rng.nextInt(radiusX + 1);
        if (rng.nextBoolean()) dx = -dx;
        dz = rng.nextInt(radiusZ + 1);
        if (rng.nextBoolean()) dz = -dz;
        if (pitch) return new Location(world, x() + dx, y(), z() + dz, yaw(), this.pitch());
        return new Location(world, x() + dx, y(), z() + dz);
    }
}

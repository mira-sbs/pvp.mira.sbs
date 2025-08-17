package sbs.mira.pvp.framework.stored;

/**
 * This interface adds an extra layer of selection when
 * needing to activate or de-activate classes in mass.
 * <p>
 * To use this class, implement it in another class and you
 * will be able to access the activate() and deactivate()
 * procedures from multiple but shared class types.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 *          <p>
 *          Created by Josh on 27/03/2017.
 * @since 1.0
 */
public interface Activatable {

    /**
     * Activates the object.
     */
    void activate();

    /**
     * Deactivates the object.
     */
    void deactivate();
}

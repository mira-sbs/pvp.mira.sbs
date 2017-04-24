package au.edu.swin.war;

import au.edu.swin.war.framework.WarPlugin;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.util.Manager;
import au.edu.swin.war.util.Match;
import au.edu.swin.war.util.modules.CommandUtility;

/**
 * An extension to WarPlugin.
 * Acts as Main class and Spigot link.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarPlugin
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public class Main extends WarPlugin {

    private Manager supercontroller; // Running instance of Manager.

    /**
     * Required method by WarPlugin.
     * Acts as the program's "Main()".
     */
    public void onEnable() {
        log("War program has awoken!");
        // The Manager handles most of the module initialisations.
        supercontroller = new Manager(this);
        registerCommandClass(CommandUtility.class); // Register the command class containing the @Commands.
        registerCommands(); // Finalise the registration of  all the classes we chose.
        ((Match) main().match()).firstMatch(); // Start the special first round procedure to kick off the cycle.
    }

    /**
     * Requires method by WarPlugin.
     * Called when this program is shut down.
     */
    public void onDisable() {
        main().world().restoreMap(main().match().getRoundID_() + "");
    }

    /**
     * Required method by WarPlugin.
     * Returns running instance of the Manager.
     *
     * @return The Manager.
     */
    public WarManager main() {
        return supercontroller;
    }
}

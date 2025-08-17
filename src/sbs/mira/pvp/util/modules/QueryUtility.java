package sbs.mira.pvp.util.modules;

import sbs.mira.pvp.framework.MiraPlugin;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.framework.MiraModule;
import sbs.mira.pvp.stats.Database;
import sbs.mira.pvp.stats.Preparable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Responsible for executing statements into
 * the database. They can be created by anything
 * for any reason, but are usually used for stats.
 * <p>
 * This should be used to insert or delete rows, not retrieve them.
 *
 * @author ILavaYou
 * @version 1.0
 * @see Database
 * @see Preparable
 * @since 1.1
 */
public class QueryUtility extends MiraModule {

    private final ConcurrentLinkedQueue<Preparable> queries;
    private final Database db;
    private BukkitTask thread;

    public QueryUtility(MiraPulse main, boolean enabled) {
        super(main);
        queries = new ConcurrentLinkedQueue<>();

        // Only create database connection if database is enabled.
        if (enabled) {
            db = new Database(mira().plugin());
            //Leave it open, or?
            saveThread(mira().plugin());
        } else db = null;
    }

    /**
     * Puts a query in a queue, that the thread will execute.
     *
     * @param query The query to execute.
     */
    public void addQuery(Preparable query) {
        queries.add(query);
    }

    /**
     * Craft a PreparedStatement.
     *
     * @param query The query to prepare.
     * @return The prepared statement.
     */
    public PreparedStatement prepare(String query) {
        return db.prepare(query);
    }

    /**
     * Stops the thread.
     */
    @SuppressWarnings("unused")
    public void stop() {
        thread.cancel();
    }

    /**
     * Polls the query queue and executes any polled query.
     * This is executed every 0.25 seconds. (maybe increase this?)
     *
     * @param plugin The plugin to execute the BukkitRunnable with.
     */
    private void saveThread(MiraPlugin plugin) {
        thread = new BukkitRunnable() {
            int counter = 2000;

            public void run() {
                // Refresh the database connection every 500 seconds.
                counter--;
                if (counter <= 0) {
                    counter = 2000;
                    db.close();
                    db.reopen();
                }

                // Prepare and execute one query, if any.
                Preparable poll = queries.poll();
                if (poll == null) return;
                poll.prepareAndRun();
            }
        }.runTaskTimerAsynchronously(plugin, 100L, 5L);
    }
}

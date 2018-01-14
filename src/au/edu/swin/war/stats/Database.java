package au.edu.swin.war.stats;

import au.edu.swin.war.framework.WarPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;

/**
 * Simple MySQL database connector.
 * Used by the QueryController.
 *
 * @author ILavaYou
 * @version 1.0
 * @since 1.1
 */
public class Database {
    private final String user;
    private final String pass;
    private final String url;
    private Connection connection;
    private final WarPlugin plugin;

    public Database(WarPlugin instance) {
        ConfigurationSection config = instance.getConfig().getConfigurationSection("database");
        this.user = config.getString("username");
        this.pass = config.getString("password");
        this.url = "jdbc:mysql://" + config.getString("hostname") + ":" + config.getString("port") + "/" + config.getString("database") + "";
        plugin = instance;
        connection = open();

        if (connection == null) {
            plugin.log("We were unable to create a connection to the database!");
            Bukkit.shutdown();
            return;
        }

        try {
            createTables();
        } catch (SQLException e) {
            plugin.log("Unable to create database tables!");
            e.printStackTrace();
            Bukkit.shutdown();
        }
    }

    /**
     * Creates the tables necessary for War functioning.
     */
    private void createTables() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();

        // Check if the `WarStats` table exists, and then create it if not.
        ResultSet check2 = meta.getTables(null, null, "WarStats", null);
        if (!check2.next()) {
            connection.prepareStatement(
                    "CREATE TABLE `WarStats` (" +
                            "player_uuid CHAR(36) NOT NULL," +
                            "kills INT(6) NOT NULL DEFAULT 0," +
                            "deaths INT(6) NOT NULL DEFAULT 0," +
                            "highestStreak INT(6) NOT NULL DEFAULT 0," +
                            "matchesPlayed INT(6) NOT NULL DEFAULT 0," +
                            "revives INT(6) NOT NULL DEFAULT 0," +
                            "PRIMARY KEY (player_uuid)," +
                            "FOREIGN KEY (player_uuid) REFERENCES `Players`(player_uuid));"
            ).executeUpdate();
        }
        check2.close();
    }

    /**
     * Try to initialize the MySQL driver.
     *
     * @return Whether or not this was successful.
     */
    private boolean initialize() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return true;
        } catch (Exception e) {
            plugin.log("MySQL driver class initialisation failed: " + e.getMessage() + ".");
            return false;
        }
    }

    /**
     * Reopen the connection if it was closed for any reason.
     */
    public void reopen() {
        close();
        connection = open();
    }

    /**
     * Try to open a connection to the database, and return
     * said connection as a field that can be stored or used.
     *
     * @return Connection to database.
     */
    private Connection open() {
        if (!initialize()) // Is there a MySQL driver?
            return null;
        try {
            if (connection == null) // Is there no connection?
                return DriverManager.getConnection(this.url, this.user, this.pass);
            else if (connection.isValid(3)) // Otherwise return valid connection.
                return connection;
            else // Or just return a new one!
                return DriverManager.getConnection(this.url, this.user, this.pass);
        } catch (SQLException e) {
            // Impossible! (if you're not stupid)
            plugin.getLogger().severe(this.url);
            plugin.log("Could not be resolved because of an SQL Exception: " + e.getMessage() + ".");
        }
        return null;
    }

    /**
     * Close the connection so it can be reopened.
     */
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (Exception e) {
            plugin.log("Failed to close database connection: " + e.getMessage());
        }
    }

    /**
     * Prepare a statement to be executed.
     * Also used for validation and to prevent injection.
     *
     * @param query Query to prepare.
     * @return PreparedStatement, ready to execute.
     */
    public PreparedStatement prepare(String query) {
        try {
            // Get the connection to prepare our statement.
            return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            // Debug
            if (!e.toString().contains("not return ResultSet")) {
                plugin.log("Error in MySQL prepare() query: " + e.getMessage());
                plugin.log(query);
            }
        }
        return null;
    }
}
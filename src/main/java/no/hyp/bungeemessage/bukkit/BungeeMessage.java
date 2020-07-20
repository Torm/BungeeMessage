package no.hyp.bungeemessage.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BungeeMessage extends JavaPlugin {

    /**
     * A unique name representing this Bukkit server.
     */
    private String serverName;

    /**
     * The connections to Bungee servers.
     */
    private Map<String, Connection> connections;

    @Override
    public void onEnable() {
        this.upgradeConfiguration();
        this.serverName = configurationName();
        try {
            this.connections = this.configurationReadConnections();
        } catch (IOException e) {
            e.printStackTrace();
            this.connections = new HashMap<>();
        }
        // Start the connection threads.
        for (Connection connection : this.connections.values()) {
            connection.start();
        }
    }

    public void onDisable() {
        for (Connection connection : this.connections.values()) {
            connection.interrupt();
        }
    }

    public void upgradeConfiguration() {
        this.saveDefaultConfig();
    }

    public String configurationName() {
        return this.getConfig().getString("name");
    }

    public int configurationVersion() {
        return this.getConfig().getInt("version");
    }

    public Map<String, Connection> configurationReadConnections() throws IOException {
        Map<String, Connection> connections = new HashMap<>();
        Set<String> keys = this.getConfig().getConfigurationSection("connections").getKeys(false);
        for (String key : keys) {
            String sAddress = this.getConfig().getString("connections." + key + ".address");
            int port = this.getConfig().getInt("connections." + key + ".port");
            InetSocketAddress address = new InetSocketAddress(sAddress, port);
            Connection connection = new Connection(this, key, address);
            connections.put(key, connection);
        }
        return connections;
    }

    public Connection getConnection(String label) {
        return this.connections.get(label);
    }

}

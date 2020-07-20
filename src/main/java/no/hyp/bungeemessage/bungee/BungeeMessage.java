package no.hyp.bungeemessage.bungee;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BungeeMessage extends Plugin implements Listener {

    /**
     * Certificate of this server.
     */
    //private Certificate certificate;

    /**
     * A thread listening for incoming Bukkit connections.
     */
    private ConnectionServer connectionServer;

    private Map<String, Connection> connections;

    @Override
    public void onEnable() {
        this.upgradeConfiguration();
        try {
            this.connectionServer = new ConnectionServer(this,25564);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {
        connectionServer.interrupt();
        Set<String> connectedServers = this.getConnectedServers();
        for (String serverName : connectedServers) {
            this.getConnection(serverName).interrupt();
        }
    }

    public void upgradeConfiguration() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream defaultConfiguration = getResourceAsStream("bungeeconfig.yml")) {
                Files.copy(defaultConfiguration, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Configuration getConfiguration() throws IOException {
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
    }

    public Connection getConnection(String serverName) {
        synchronized (this.connections) {
            return this.connections.get(serverName);
        }
    }

    public void addConnection(Connection connection) {
        synchronized (this.connections) {
            this.connections.put(connection.getName(), connection);
        }
    }

    public void removeConnection(String serverName) {
        synchronized (this.connections) {
            this.connections.remove(serverName);
        }
    }

    public Set<String> getConnectedServers() {
        synchronized (this.connections) {
            return new HashSet<>(this.connections.keySet());
        }
    }

}

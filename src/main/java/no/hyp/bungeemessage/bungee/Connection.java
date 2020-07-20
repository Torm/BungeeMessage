package no.hyp.bungeemessage.bungee;

import java.io.*;
import java.net.Socket;

public class Connection extends Thread {

    private final BungeeMessage plugin;

    private final Socket socket;

    private final DataInputStream inputStream;

    private final DataOutputStream outputStream;

    private String serverName;

    public Connection(BungeeMessage plugin, Socket socket) throws IOException {
        this.plugin = plugin;
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        // Add the connection to the plugin's set of connected servers.
        this.plugin.addConnection(this);
    }

    @Override
    public void run() {
        boolean listen = false;
        // The first thing a connected Bukkit server sends is its unique name.
        try {
            int serverNameLength = this.inputStream.readInt();
            byte[] bServerName = new byte[serverNameLength];
            this.inputStream.readFully(bServerName);
            this.serverName = new String(bServerName);
            listen = true;
            this.plugin.getLogger().info(String.format("%s connected.", this.serverName));
            ConnectEvent event = new ConnectEvent(this.serverName);
            this.plugin.getProxy().getPluginManager().callEvent(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Keep reading messages until the connection is broken or the thread is interrupted.
        while (listen) {
            try {
                // Read a message from the stream.
                int channelLength = inputStream.readInt();
                int dataLength = inputStream.readInt();
                byte[] bChannel = new byte[channelLength];
                String channel = new String(bChannel);
                inputStream.readFully(bChannel, 0, channelLength);
                byte[] data = new byte[dataLength];
                inputStream.readFully(data, 0, dataLength);
                // Send a MessageEvent with this data.
                MessageEvent event = new MessageEvent(this.serverName, channel, data);
                this.plugin.getProxy().getPluginManager().callEvent(event);
            }
            // The connection was broken before a new message could be read.
            // Discard the message and close this connection.
            catch (EOFException e) {
                this.plugin.getLogger().info(String.format("%s disconnected.", this.serverName));
                listen = false;
                DisconnectEvent event = new DisconnectEvent(this.serverName);
                this.plugin.getProxy().getPluginManager().callEvent(event);
            }
            // The stream was closed or an error occurred.
            catch (IOException e) {
                // The stream was closed by an interrupt. Discard the message and close the connection.
                if (this.isInterrupted()) {
                    listen = false;
                }
                // Error: print stacktrace and close.
                else {
                    this.plugin.getLogger().severe(String.format("Error in connection to %s.", this.serverName));
                    e.printStackTrace();
                    listen = false;
                }
            }
        }
        // Remove the connection from the plugin list.
        this.plugin.removeConnection(this.serverName);
        try {
            this.inputStream.close();
            this.outputStream.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Interrupting a Connection closes its input and output streams and socket.
     */
    @Override
    public void interrupt() {
        super.interrupt();
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a string message to this server.
     */
    public void message(String channel, String message) throws IOException {
        this.message(channel, message.getBytes());
    }

    /**
     * Send a message to this server.
     * @param channel
     * @param data
     */
    public void message(String channel, byte[] data) throws IOException {
        byte[] bChannel = channel.getBytes();
        int channelLength = bChannel.length;
        int dataLength = data.length;
        synchronized (outputStream) {
            this.outputStream.writeInt(channelLength);
            this.outputStream.writeInt(dataLength);
            this.outputStream.write(bChannel);
            this.outputStream.write(data);
        }
    }

}

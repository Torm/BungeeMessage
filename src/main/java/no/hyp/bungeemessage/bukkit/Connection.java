package no.hyp.bungeemessage.bukkit;

import java.io.*;
import java.net.*;

/**
 * A connection from a Bukkit message client to a Bungee message server.
 */
public class Connection extends Thread {

    private final BungeeMessage plugin;

    /**
     * A local only label identifying the Bungeecord server.
     */
    private final String label;

    private final InetSocketAddress address;

    private final Socket socket;

    private final DataInputStream inputStream;

    private final DataOutputStream outputStream;

    public Connection(BungeeMessage plugin, String label, InetSocketAddress address) throws IOException {
        this.plugin = plugin;
        this.label = label;
        this.address = address;
        this.socket = new Socket();
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        // Keep reading messages until the connection is broken or the thread is interrupted.
        boolean run = true;
        while (run) {
            // If the socket is connected, attempt to read a message.
            if (this.socket.isConnected()) {
                try {
                    // Read a message from the stream.
                    // A packet has a fixed header of 2 integers that give the byte lengths of the channel and the data.
                    int channelLength = inputStream.readInt();
                    int dataLength = inputStream.readInt();
                    byte[] bChannel = new byte[channelLength];
                    String channel = new String(bChannel);
                    inputStream.readFully(bChannel, 0, channelLength);
                    byte[] data = new byte[dataLength];
                    inputStream.readFully(data, 0, dataLength);

                    // Send a MessageEvent with this data.
                    AsyncMessageEvent event = new AsyncMessageEvent(true, this.label, channel, data);
                    this.plugin.getServer().getPluginManager().callEvent(event);
                }
                // The connection was broken before a new message could be fully read.
                // Send a DisconnectEvent, discard the message and try to reconnect.
                catch (EOFException e) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        this.plugin.getLogger().warning(String.format("Connection to %s was broken. Attempting to reconnect.", this.label));
                    });
                    AsyncDisconnectEvent event = new AsyncDisconnectEvent(true, this.label);
                    this.plugin.getServer().getPluginManager().callEvent(event);
                } catch (IOException e) {
                    // The stream was closed by an interrupt.
                    // Send a DisconnectEvent, discard the message and close the connection.
                    if (this.isInterrupted()) {
                        run = false;
                    }
                    // Error: print stacktrace and close.
                    else {
                        e.printStackTrace();
                        run = false;
                    }
                }
            }
            // If the socket is not connected, attempt to connect to the server.
            else {
                // Attempt to connect to the server.
                try {
                    socket.connect(this.address, 15000);
                    // The name of this server is the first message sent.
                    byte[] bServerName = this.plugin.getName().getBytes();
                    int serverNameLength = bServerName.length;
                    synchronized (this.outputStream) {
                        this.outputStream.writeInt(serverNameLength);
                        this.outputStream.write(bServerName);
                    }
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        this.plugin.getLogger().info(String.format("Connected to %s.", this.label));
                    });
                    // Send an AsyncConnectEvent on successful connection.
                    AsyncConnectEvent event = new AsyncConnectEvent(true, this.label);
                    this.plugin.getServer().getPluginManager().callEvent(event);
                }
                // On connection timeout.
                catch (SocketTimeoutException e) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        this.plugin.getLogger().warning(String.format("Attempt to connect to %s timed out.", this.label));
                    });
                }
                // On connection error.
                catch (IOException e) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        this.plugin.getLogger().severe(String.format("Error while connecting to %s.", this.label));
                        e.printStackTrace();
                    });
                }
            }
        }
        try {
            this.inputStream.close();
            this.outputStream.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

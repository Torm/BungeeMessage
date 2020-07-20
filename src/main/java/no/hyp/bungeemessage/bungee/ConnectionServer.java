package no.hyp.bungeemessage.bungee;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionServer extends Thread {

    private final BungeeMessage plugin;

    private final ServerSocket serverSocket;

    public ConnectionServer(BungeeMessage plugin, int port) throws IOException {
        this.plugin = plugin;
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        // Listen for incoming connections.
        boolean listen = true;
        while (listen) {
            try {
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(plugin, socket);
                connection.start();
            } catch (SocketException e) {
                if (this.isInterrupted()) {
                    listen = false;
                } else {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Interrupting the listener thread means we do not want more connections.
    @Override
    public void interrupt() {
        // Close the socket. The thread is blocked on this method, and will throw a SocketException
        // when it is closed. Use this to stop the thread.
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Super sets interrupted flag.
        super.interrupt();
    }

}

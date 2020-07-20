package no.hyp.bungeemessage.bungee;

import net.md_5.bungee.api.plugin.Event;

/**
 * An event called when a server disconnects.
 */
public class DisconnectEvent extends Event {

    private final String serverName;

    public DisconnectEvent(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

}

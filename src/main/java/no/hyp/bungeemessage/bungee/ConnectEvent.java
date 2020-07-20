package no.hyp.bungeemessage.bungee;

import net.md_5.bungee.api.plugin.Event;

public class ConnectEvent extends Event {

    private final String serverName;

    public ConnectEvent(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

}

package no.hyp.bungeemessage.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncConnectEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final String label;

    public AsyncConnectEvent(boolean async, String label) {
        super(async);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}

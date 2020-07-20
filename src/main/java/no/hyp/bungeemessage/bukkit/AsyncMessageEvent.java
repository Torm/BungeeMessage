package no.hyp.bungeemessage.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Arrays;

/**
 * Represents a message received from Bungee.
 */
public class AsyncMessageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final String label;

    private final String channel;

    private final byte[] data;

    public AsyncMessageEvent(boolean async, String label, String channel, byte[] data) {
        super(async);
        this.label = label;
        this.channel = channel;
        this.data = data;
    }

    public String getLabel() {
        return label;
    }

    public String getChannel() {
        return this.channel;
    }

    public byte[] getData() {
        return this.data;
    }

    /**
     * Convert the data into the corresponding String.
     * @return
     */
    public String getDataAsString() {
        return Arrays.toString(data);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}

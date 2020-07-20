package no.hyp.bungeemessage.bungee;

import net.md_5.bungee.api.plugin.Event;

/**
 * Represents a message received from Bukkit.
 */
public class MessageEvent extends Event {

    private final String origin;

    private final String channel;

    private final byte[] data;

    public MessageEvent(String origin, String channel, byte[] data) {
        this.origin = origin;
        this.channel = channel;
        this.data = data;
    }

    /**
     * The origin server of this message.
     */
    public String getOrigin() {
        return this.origin;
    }

    /**
     * The channel indicates who the message is for.
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * Data sent in the message.
     */
    public byte[] getData() {
        return this.data;
    }

}

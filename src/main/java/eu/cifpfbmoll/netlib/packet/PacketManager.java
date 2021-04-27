package eu.cifpfbmoll.netlib.packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * Manage incoming Packets and process them using PacketHandler.
 *
 * <p>PacketManager associates Packet type (4B string) with a PacketHandler.
 * Only one PacketHandler can be specified for a certain Packet type.</p>
 *
 * @see PacketHandler
 */
public class PacketManager {
    private static final Logger log = LoggerFactory.getLogger(PacketManager.class);
    private final Hashtable<String, PacketHandler> handlerTable = new Hashtable<>();

    /**
     * Add a new Packet Handler for Packet type.
     *
     * @param type packet type to handle
     * @param handler packet handler to handle a Packet type
     * @see PacketHandler
     */
    public void add(String type, PacketHandler handler) {
        if (handler == null) return;
        String packetType = Packet.formatType(type);
        if (!this.handlerTable.containsKey(packetType))
            this.handlerTable.put(packetType, handler);
    }

    /**
     * Removed Packet Handler for Packet type.
     *
     * @param type packet type to remove
     */
    public void remove(String type) {
        String packetType = Packet.formatType(type);
        this.handlerTable.remove(packetType);
    }

    /**
     * Process a packet using its Packet type handler.
     *
     * @param packet packet to process
     */
    public void process(Packet packet) {
        if (packet == null) return;
        PacketHandler handler = this.handlerTable.get(packet.type);
        if (handler != null)
            handler.handle(packet);
    }
}
package eu.cifpfbmoll.netlib.packet;

import eu.cifpfbmoll.netlib.annotation.PacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, PacketHandler> handlers = new HashMap<>();
    private final Map<String, Class<? extends PacketObject>> types = new HashMap<>();

    /**
     * Add a new Packet Handler for Packet type.
     *
     * @param objectClass object class to handle
     * @param handler     packet handler to handle a Packet type
     * @see PacketHandler
     */
    public void add(Class<? extends PacketObject> objectClass, PacketHandler handler) {
        if (objectClass == null || handler == null) return;
        PacketType packetType = objectClass.getAnnotation(PacketType.class);
        if (packetType == null) return;
        String type = Packet.formatType(packetType.value());
        if (!this.handlers.containsKey(type) && !this.types.containsKey(type)) {
            this.handlers.put(type, handler);
            this.types.put(type, objectClass);
        }
    }

    /**
     * Removed Packet Handler for Packet type.
     *
     * @param type packet type to remove
     */
    public void remove(String type) {
        String packetType = Packet.formatType(type);
        this.handlers.remove(packetType);
    }

    /**
     * Process a packet using its Packet type handler.
     *
     * @param packet packet to process
     */
    public void process(Packet packet) {
        if (packet == null) return;
        Class<? extends PacketObject> type = this.types.get(packet.type);
        PacketHandler handler = this.handlers.get(packet.type);
        if (handler != null) {
            try {
                PacketObject object = type.getConstructor().newInstance();
                object.load(packet.data);
                handler.handle(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
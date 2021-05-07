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
    private final Map<String, PacketHandler<?>> handlers = new HashMap<>();
    private final Map<String, Class<?>> types = new HashMap<>();

    /**
     * Add a new Packet Handler for Packet type.
     *
     * @param clazz   object class to handle
     * @param handler packet handler to handle a Packet type
     * @throws NullPointerException     if object's class or handler are null
     * @throws IllegalArgumentException if object's class does not have the PacketType annotation or packet type is already registered
     * @see PacketHandler
     */
    public <T> void add(Class<T> clazz, PacketHandler<T> handler) throws NullPointerException, IllegalArgumentException {
        if (clazz == null || handler == null)
            throw new NullPointerException("Object's class and PacketHandler cannot be null.");
        PacketType packetType = clazz.getAnnotation(PacketType.class);
        if (packetType == null)
            throw new IllegalArgumentException(String.format("Missing @PacketType annotation in class '%s'.", clazz.getSimpleName()));
        String type = Packet.formatType(packetType.value());
        if (this.handlers.containsKey(type) || this.types.containsKey(type))
            throw new IllegalArgumentException(String.format("PacketType '%s' is already registered.", type));
        this.handlers.put(type, handler);
        this.types.put(type, clazz);
    }

    /**
     * Removed Packet Handler for Packet type.
     *
     * @param type packet type to remove
     */
    public void remove(String type) {
        String packetType = Packet.formatType(type);
        this.handlers.remove(packetType);
        this.types.remove(packetType);
    }

    /**
     * Process a packet using its Packet type handler.
     *
     * @param packet packet to process
     */
    public void process(Packet packet) {
        if (packet == null) return;
        Class<?> type = this.types.get(packet.type);
        PacketHandler handler = this.handlers.get(packet.type);
        if (handler != null) {
            try {
                Object object = type.getConstructor().newInstance();
                PacketParser parser = PacketParser.getInstance();
                parser.deserialize(object, packet.data);
                handler.handle(packet.src, object);
            } catch (Exception e) {
                log.error("failed to process packet: ", e);
            }
        }
    }
}
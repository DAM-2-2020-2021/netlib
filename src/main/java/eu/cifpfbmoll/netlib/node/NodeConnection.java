package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.annotation.PacketType;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.packet.PacketParser;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NodeConnection Class manages a single connection with another node on the network.
 *
 * <p>Uses NodeSocket to send/receive data from a node.</p>
 *
 * @see Node
 * @see NodeSocket
 */
public class NodeConnection extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeConnection.class);
    private final Integer id;
    private final Node node;
    private final NodeSocket socket;
    private final PacketManager manager;

    protected NodeConnection(Integer id, Node node, NodeSocket socket, PacketManager manager) {
        this.id = id;
        this.node = node;
        this.socket = socket;
        this.manager = manager;
    }

    /**
     * Get Node.
     *
     * @return current Node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Get PacketManager.
     *
     * @return current PacketManager
     */
    public PacketManager getManager() {
        return manager;
    }

    /**
     * Send a PacketObject to the connected node.
     *
     * @param object PacketObject to send
     * @return true if send was successful, false otherwise
     */
    public boolean send(Object object) {
        if (object == null) return false;
        Class<?> clazz = object.getClass();
        try {
            PacketType packetType = clazz.getAnnotation(PacketType.class);
            if (packetType == null) return false;
            String type = Packet.formatType(packetType.value());
            PacketParser parser = PacketParser.getInstance();
            byte[] data = parser.serialize(object);
            if (data == null) return false;
            Packet packet = Packet.create(type, this.id, this.node.getId(), data);
            this.socket.write(packet.dump());
            return true;
        } catch (Exception e) {
            log.error(String.format("failed to send object of type '%s': ", clazz.getSimpleName()), e);
            return false;
        }
    }

    @Override
    public void run() {
    }
}

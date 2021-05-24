package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.annotation.PacketType;
import eu.cifpfbmoll.netlib.packet.Packet;
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
    private final Node node;
    private final NodeSocket socket;
    private final NodeManager manager;
    private final NodeChannel nodeChannel = new NodeChannel(this);

    public NodeConnection(Node node, NodeSocket socket, NodeManager manager) {
        this.node = node;
        this.socket = socket;
        this.manager = manager;
        log.info("New NodeConnection created with " + this.node.getIp());
        this.start();
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
     * Get NodeSocket.
     *
     * @return NodeConnection's NodeSocket
     */
    public NodeSocket getNodeSocket() {
        return socket;
    }

    /**
     * Get NodeManager.
     *
     * @return current NodeManager
     */
    public NodeManager getManager() {
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
            Packet packet = Packet.create(type, this.manager.getId(), this.node.getId(), data);
            this.socket.write(packet.dump());
            return true;
        } catch (Exception e) {
            log.error(String.format("failed to send object of type '%s': ", clazz.getSimpleName()), e);
            return false;
        }
    }

    @Override
    public void run() {
        // TODO: thread routine (rebre packets)
        while (this.run && !this.socket.isClosed()) {
            try {
                byte[] data = new byte[1024];
                int size = this.socket.read(data);
                Packet packet = Packet.load(data);
                this.manager.getPacketManager().process(packet);
            } catch (Exception e) {
                log.error("NodeConnection channel failed: ", e);
            }
        }
        this.manager.removeNodeConnection(this);
    }
}

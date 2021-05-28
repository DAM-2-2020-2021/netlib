package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.annotation.PacketType;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketParser;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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
    //private final NodeChannel nodeChannel = new NodeChannel(this);

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
            int size = packet.size();
            if (size > Packet.MAX_PACKET_SIZE)
                throw new IllegalArgumentException(String.format("Object %s passed maximum size: %d/%d", clazz.getSimpleName(), packet.size(), Packet.MAX_PACKET_SIZE));
            this.socket.write(packet.dump());
            return true;
        } catch (Exception e) {
            log.error(String.format("failed to send object of type '%s': ", clazz.getSimpleName()), e);
            return false;
        }
    }

    /**
     * Send a Packet to the connected node.
     *
     * @param packet Packet to send
     * @return true if send was successful, false otherwise
     */
    public boolean send(Packet packet) {
        if (packet == null) return false;
        try {
            this.socket.write(packet.dump());
            return true;
        } catch (Exception e) {
            log.error("failed to send packet", e);
            return false;
        }
    }

    /**
     * Close NodeSocket and finish thread.
     */
    public void close() {
        try {
            this.getNodeSocket().close();
        } catch (Exception ignored) {
        }
        this.run = false;
    }

    @Override
    public void run() {
        while (this.run && !this.socket.isClosed()) {
            try {
                byte[] data = new byte[1024];
                int size = this.socket.read(data);
                if (size < 0) continue;
                Packet packet = Packet.load(data);
                if (!Objects.equals(packet.getDestinationId(), this.manager.getId())) {
                    packet.addResender(this.manager.getId());
                    this.manager.send(packet.getDestinationId(), packet);
                } else {
                    this.manager.getPacketManager().process(packet);
                }
            } catch (Exception e) {
                log.error("NodeConnection thread failed: ", e);
            }
        }
        this.manager.removeNodeConnection(this);
    }
}

package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
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
    private final NodeChannel channel = new NodeChannel(this);

    public NodeConnection(Node node, NodeSocket socket, NodeManager manager) {
        this.node = node;
        this.socket = socket;
        this.manager = manager;
        this.start();
        this.channel.start();
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
     * Send a Packet to the connected node.
     *
     * @param object PacketObject to send
     * @return true if send was successful, false otherwise
     */
    public boolean send(Object object) {
        if (object == null) return false;
        try {
            if (object instanceof Packet) {
                this.socket.write(((Packet) object).dump());
                return true;
            } else {
                return this.socket.send(object, this.manager.getId(), this.node.getId());
            }
        } catch (Exception e) {
            log.error("failed to send packet", e);
            return false;
        }
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
                    packet.decreaseTTL();
                    this.manager.send(packet.getDestinationId(), packet);
                } else {
                    if (!this.manager.getPacketManager().process(packet))
                        this.channel.getPacketManager().process(packet);
                }
            } catch (SocketException ignored) {
                this.socket.safeClose();
            } catch (Exception e) {
                log.error("NodeConnection thread failed: ", e);
                this.socket.safeClose();
            }
        }
        this.manager.removeNodeConnection(this);
    }
}

package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.packet.PacketObject;

import java.io.IOException;

/**
 * The NodeConnection Class manages a single connection with another node on the network.
 *
 * <p>Uses NodeSocket to send/receive data from a node.</p>
 *
 * @see Node
 * @see NodeSocket
 */
public class NodeConnection {
    private final Node node;
    private final NodeSocket socket;
    private final PacketManager manager;

    protected NodeConnection(Node node, NodeSocket socket, PacketManager manager) {
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
     * @param packet PacketObject to send
     * @return true if send was successful, false otherwise
     */
    public boolean send(PacketObject packet) {
        if (packet == null) return false;
        try {
            this.socket.write(packet.dump());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

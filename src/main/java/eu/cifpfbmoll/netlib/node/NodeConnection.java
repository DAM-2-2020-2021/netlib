package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.PacketManager;

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

    public Node getNode() {
        return node;
    }

    public NodeSocket getSocket() {
        return socket;
    }

    public PacketManager getManager() {
        return manager;
    }
}

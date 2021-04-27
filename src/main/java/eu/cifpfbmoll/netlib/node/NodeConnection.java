package eu.cifpfbmoll.netlib.node;

/**
 * The NodeConnection Class manages a single connection with another node on the network.
 *
 * <p>Uses NodeSocket to send/receive data from a node.</p>
 *
 * @see Node
 * @see NodeSocket
 */
public class NodeConnection {
    private Node node;
    private NodeSocket socket;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public NodeSocket getSocket() {
        return socket;
    }

    public void setSocket(NodeSocket socket) {
        this.socket = socket;
    }

    public NodeConnection(Node node, NodeSocket socket) {
        this.node = node;
        this.socket = socket;
    }
}

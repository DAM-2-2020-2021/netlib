package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.annotation.PacketType;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.packet.PacketObject;
import eu.cifpfbmoll.netlib.util.Threaded;

import java.io.IOException;

/**
 * The NodeConnection Class manages a single connection with another node on the network.
 *
 * <p>Uses NodeSocket to send/receive data from a node.</p>
 *
 * @see Node
 * @see NodeSocket
 */
public class NodeConnection extends Threaded {
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

    public NodeSocket getNodeSocket(){
        return socket;
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
    public boolean send(PacketObject object) {
        if (object == null) return false;
        try {
            PacketType packetType = object.getClass().getAnnotation(PacketType.class);
            if (packetType == null) return false;
            String type = Packet.formatType(packetType.value());

            // TODO: implement packet src and dst ids
            Packet packet = Packet.create(type, 0, 0, object.dump());
            this.socket.write(packet.dump());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void run() {
    }
}

package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.annotation.PacketType;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.packet.PacketParser;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
    private final NodeHealthConnection nodeHealthConnection = new NodeHealthConnection();
    private final PacketManager packetManager;

    // TODO: assign packet manager
    public NodeConnection(Node node, NodeSocket socket, PacketManager packetManager) {
        // TODO: Create nodehealthconnection automatically
        this.node = node;
        this.socket = socket;
        this.packetManager = packetManager;
        System.out.println("new nodeConnection created!");
        //this.start();
    }

    /**
     * Get Node.
     *
     * @return current Node
     */
    public Node getNode() {
        return node;
    }

    public NodeSocket getNodeSocket() {
        return socket;
    }

    public PacketManager getManager() {
        return this.packetManager;
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

            // TODO: implement packet src and dst ids
            PacketParser parser = PacketParser.getInstance();
            Packet packet = Packet.create(type, 0, this.node.getId(), parser.serialize(object));
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
    }
}

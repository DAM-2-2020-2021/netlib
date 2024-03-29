package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * The NodeServer Class listens for incoming Node connections and assigns them a new NodeConnection.
 *
 * <p>This is a {@link Threaded} Class so it can run on its own thread.</p>
 * <p>{@link ServerSocket} is used to listen for new connections.</p>
 *
 * @see Threaded
 * @see ServerSocket
 */
public class NodeServer extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeServer.class);
    public static final int DEFAULT_PORT = 3000;
    private ServerSocket socket;
    private final NodeManager manager;

    /**
     * Creates a new NodeServer with an instance of NodeManager and PacketManager.
     *
     * @param manager NodeManager instance.
     */
    public NodeServer(NodeManager manager) {
        this(manager, DEFAULT_PORT);
    }

    public NodeServer(NodeManager manager, int port) {
        this.manager = manager;
        try {
            this.socket = new ServerSocket(port);
            this.start();
        } catch (IOException e) {
            log.error("Error while creating ServerSocket", e);
        }
    }

    @Override
    public void run() {
        while (this.run) {
            try {
                NodeSocket nodeSocket = new NodeSocket(this.socket.accept());
                if (this.manager.nodeInHash(nodeSocket.getIp())) {
                    Integer nodeID = this.manager.getNodeIdByIP(nodeSocket.getIp());
                    NodeConnection nodeConnection = new NodeConnection(new Node(nodeID, nodeSocket.getIp()), nodeSocket, this.manager);
                    this.manager.addNodeConnection(nodeConnection);
                } else {
                    new NodeIdentification(nodeSocket, this.manager);
                }
            } catch (Exception e) {
                log.error("Error in NodeServer run", e);
            }
        }
        try {
            this.socket.close();
        } catch (Exception e) {
            log.error("failed to close ServerSocket: ", e);
        }
    }
}

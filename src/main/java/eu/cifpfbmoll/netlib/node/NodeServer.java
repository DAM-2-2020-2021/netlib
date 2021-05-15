package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.PacketManager;
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
    public static final int DEFAULT_PORT = 420;
    private ServerSocket socket;
    private NodeManager nodeManager;
    private PacketManager packetManager;

    /**
     * Creates a new NodeServer with an instance of NodeManager and PacketManager.
     *
     * @param nodeManager   NodeManager instance.
     * @param packetManager PacketManager instance.
     */
    public NodeServer(NodeManager nodeManager, PacketManager packetManager) {
        this.nodeManager = nodeManager;
        this.packetManager = packetManager;
        try {
            this.socket = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            log.error("Error while creating ServerSocket", e);
        }
        this.start();
    }

    /**
     * Create a new NodeServer from an existing ServerSocket.
     *
     * @param socket ServerSocket to create NodeServer from
     * @throws NullPointerException if socket is null
     */
    protected NodeServer(ServerSocket socket) throws NullPointerException {
        if (socket == null) throw new NullPointerException("ServerSocket must not be null");
        this.socket = socket;
    }

    @Override
    public void run() {
        while (this.run) {
            try {
                // TODO: create node connection on client connection
                NodeSocket nodeSocket = new NodeSocket(this.socket.accept());
                if (!this.nodeManager.nodeInHash(nodeSocket.getIp())) {
                    log.info(String.format("Identifying connection with ip: %s", nodeSocket.getIp()));
                    new NodeIdentification(nodeSocket, this.nodeManager);
                } else {
                    NodeConnection nodeConnection = new NodeConnection(new Node(0+NodeManager.counter, nodeSocket.getIp()), nodeSocket, this.packetManager);
                    NodeManager.counter++;
                    this.nodeManager.addNewConnection(nodeConnection);
                    log.info(String.format("New NodeConnection added! %s"), nodeSocket.getIp());
                }
                // TODO: use internal packets to check for nodes
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

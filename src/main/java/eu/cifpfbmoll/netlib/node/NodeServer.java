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
    private static final int DEFAULT_PORT = 420;
    private final ServerSocket socket;
    private final NodeManager nodeManager;

    /**
     * Create a new NodeServer with a port to listen on.
     *
     * @throws IOException if port binding fails
     */
    protected NodeServer(NodeManager nodeManager) throws IOException {
        this(nodeManager, new ServerSocket(DEFAULT_PORT));
    }

    /**
     * Create a new NodeServer with a port to listen on.
     *
     * @param port port number to bound socket to
     * @throws IOException if port binding fails
     */
    protected NodeServer(NodeManager nodeManager, int port) throws IOException {
        this(nodeManager, new ServerSocket(port));
    }

    /**
     * Create a new NodeServer from an existing ServerSocket.
     *
     * @param socket ServerSocket to create NodeServer from
     * @throws NullPointerException if socket is null
     */
    protected NodeServer(NodeManager nodeManager, ServerSocket socket) throws NullPointerException {
        if (socket == null || nodeManager == null) throw new NullPointerException("ServerSocket must not be null");
        this.nodeManager = nodeManager;
        this.socket = socket;
        this.start();
    }

    @Override
    public void run() {
        try {
            while (this.run) {
                log.info("listening for connections on port " + DEFAULT_PORT);
                NodeSocket nodeSocket = new NodeSocket(this.socket.accept());
                log.info("new connection");
                NodeConnection nodeConnection = new NodeConnection(this.nodeManager.getId(), new Node(-1, null), nodeSocket, this.nodeManager.getPacketManager());
                this.nodeManager.addNodeConnection(nodeConnection);
            }
        } catch (Exception e) {
            log.error("ServerSocket thread crashed: ", e);
        } finally {
            try {
                this.socket.close();
            } catch (Exception e) {
                log.error("failed to close ServerSocket: ", e);
            }
        }
    }
}

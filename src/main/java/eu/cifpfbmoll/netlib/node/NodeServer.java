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

    /**
     * Create a new NodeServer with a port to listen on.
     *
     * @throws IOException if port binding fails
     */
    private NodeServer() throws IOException {
        this(new ServerSocket(DEFAULT_PORT));
    }

    /**
     * Create a new NodeServer with a port to listen on.
     *
     * @param port port number to bound socket to
     * @throws IOException if port binding fails
     */
    private NodeServer(int port) throws IOException {
        this(new ServerSocket(port));
    }

    /**
     * Create a new NodeServer from an existing ServerSocket.
     *
     * @param socket ServerSocket to create NodeServer from
     * @throws NullPointerException if socket is null
     */
    private NodeServer(ServerSocket socket) throws NullPointerException {
        if (socket == null) throw new NullPointerException("ServerSocket must not be null");
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
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

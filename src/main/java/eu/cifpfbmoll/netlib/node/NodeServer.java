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
    public static final int DEFAULT_PORT = 420;
    private ServerSocket socket;
    private final NodeManager nodeManager;
    private String ip;

    /**
     * Creates a new NodeServer with an instance of NodeManager and PacketManager.
     *
     * @param manager NodeManager instance.
     */
    public NodeServer(NodeManager manager, String ip) {
        this.ip=ip;
        this.nodeManager = manager;
        try {
            this.socket = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            log.error("Error while creating ServerSocket", e);
        }
        this.start();
    }

    @Override
    public void run() {
        while (this.run) {
            try {
                NodeSocket nodeSocket = new NodeSocket(this.socket.accept());
                if (!this.nodeManager.nodeInHash(nodeSocket.getIp()) && !this.ip.equals(nodeSocket.getIp())) {
                    log.info("Creating new connection with: " + nodeSocket.getIp());
                    new NodeIdentification(this.nodeManager,nodeSocket);
                }else{
                    //TODO: send Nodesocket to NodeConnection in order to manage packets. If NodeConnection is dead, we have to create another one.
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

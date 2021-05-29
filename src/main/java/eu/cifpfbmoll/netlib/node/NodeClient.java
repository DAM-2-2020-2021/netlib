package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends messages until connects with another pc.
 */
public class NodeClient extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeClient.class);
    private static final int ATTEMPTS = 10;
    private static final int DELAY = 300;
    private final NodeSocket nodeSocket;
    private final NodeManager nodeManager;

    /**
     * Creates NodeClient instance with given parameters.
     *
     * @param nodeSocket  new NodeSocket.
     * @param nodeManager NodeManager's instance.
     */
    public NodeClient(NodeSocket nodeSocket, NodeManager nodeManager) {
        this.nodeManager = nodeManager;
        this.nodeSocket = nodeSocket;
        this.start();
    }

    /**
     * Close NodeSocket and finish thread.
     */
    public void close() {
        try {
            this.nodeSocket.close();
        } catch (Exception ignored) {
        }
        this.run = false;
    }

    @Override
    public void run() {
        Packet packet = Packet.create("HELO", 0, 0);
        while (this.run && !this.nodeManager.nodeInHash(this.nodeSocket.getIp()) && !this.nodeSocket.isClosed()) {
            try {
                for (int i = 0; i < ATTEMPTS; i++) {
                    log.info("attempt: " + i);
                    this.nodeSocket.write(packet.dump());
                    Thread.sleep(DELAY);
                }
            } catch (Exception e) {
                log.error("NodeClient's thread failed: ", e);
            } finally {
                close();
            }
        }
        this.nodeManager.removeNodeClient(this);
    }
}

package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Sends messages until connects with another pc.
 */
public class NodeClient extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeClient.class);
    private static final int ATTEMPTS = 10;
    private static final int DELAY = 300;
    private final String ip;
    private final NodeSocket nodeSocket;
    private final NodeManager nodeManager;
    private boolean identifiedPlayer = false;

    /**
     * Creates NodeClient instance with given parameters.
     *
     * @param ip          discovered ip.
     * @param nodeSocket  new NodeSocket.
     * @param nodeManager NodeManager's instance.
     */
    public NodeClient(String ip, NodeSocket nodeSocket, NodeManager nodeManager) {
        this.ip = ip;
        this.nodeManager = nodeManager;
        this.nodeSocket = nodeSocket;
        this.start();
    }

    /**
     * Sends a "hello" message in order to be identified as a Damn player.
     */
    private void tryFeedback() {
        try {
            Packet hello = Packet.create("HELO", this.nodeManager.getId(), 0);
            this.nodeSocket.write(hello.dump());
        } catch (IOException e) {
            log.error("Problem sending hello message", e);
        }
    }

    @Override
    public void run() {
        while (!this.identifiedPlayer) {
            for (int i = 0; i < ATTEMPTS; i++) {
                this.tryFeedback();
                this.sleep(DELAY);
                this.identifiedPlayer = this.nodeManager.nodeInHash(this.ip);
            }
            this.identifiedPlayer = true;
        }
    }
}

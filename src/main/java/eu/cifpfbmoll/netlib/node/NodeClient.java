package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Sends messages until connects with another pc.
 */
public class NodeClient extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeClient.class);
    private boolean identifiedPlayer = false;
    private final int ATTEMPTS = 10;
    private String ip;
    private final int DELAY = 300;
    private NodeSocket nodeSocket;
    private NodeManager nodeManager;

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
            DataOutputStream outputStream = new DataOutputStream(this.nodeSocket.getSocket().getOutputStream());
            outputStream.writeUTF("I am Damn player");
            outputStream.flush();
        } catch (IOException e) {
            log.error("Problem sending hello message", e);
        }
    }

    @Override
    public void run() {
        while (!this.identifiedPlayer) {
            for (int i = 0; i < ATTEMPTS; i++) {
                this.tryFeedback();
                this.sleep(this.DELAY);
                this.identifiedPlayer = this.nodeManager.nodeInHash(this.ip);
            }
            this.identifiedPlayer = true;
        }
    }
}

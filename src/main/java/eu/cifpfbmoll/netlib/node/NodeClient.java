package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Sends messages until ip is registered inside nodes HashMap.
 */
public class NodeClient extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeClient.class);
    private static final int DELAY = 300;
    private String ip;
    private final NodeSocket nodeSocket;
    private final NodeManager nodeManager;

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

    public String getIp() {
        return ip;
    }

    /**
     * Sends a "hello" message in order to be identified as a Damn player.
     */
    private void sendHello() {
        try {
            DataOutputStream outputStream = new DataOutputStream(this.nodeSocket.getSocket().getOutputStream());
            outputStream.writeUTF("I am Damn player");
            outputStream.flush();
        } catch (IOException e) {
            log.error("Error in NodeClient",e);
        }
    }

    @Override
    public void run() {
        while (!this.nodeManager.nodeInHash(ip)) {
            this.sendHello();
            this.sleep(DELAY);
        }
        this.nodeManager.removeNodeClient(this);
    }
}

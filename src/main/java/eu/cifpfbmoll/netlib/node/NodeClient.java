package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
            DataOutputStream outputStream = new DataOutputStream(this.nodeSocket.getSocket().getOutputStream());
            outputStream.writeUTF("I am Damn player");
            outputStream.flush();
            DataInputStream inputStream = new DataInputStream(this.nodeSocket.getSocket().getInputStream());
            String message = inputStream.readUTF();
            if (message.equals("Welcome")) {
                log.info("NodeSocket " + this.nodeSocket.getIp() + " has been identified successfully!");
                int id = NodeManager.getMyId(this.nodeSocket.getIp());
                this.nodeManager.putNodeId(id, this.nodeSocket.getIp());
                NodeConnection nodeConnection = new NodeConnection(new Node(id, this.nodeSocket.getIp()), this.nodeSocket, this.nodeManager);
                this.nodeManager.addNewConnection(nodeConnection);
                this.identifiedPlayer = true;
            }
        } catch (IOException e) {
            log.error("Problem sending hello message", e);
        }
    }

    @Override
    public void run() {
        while (!this.identifiedPlayer) {
            this.tryFeedback();
            this.sleep(DELAY);
        }
    }
}

package eu.cifpfbmoll.netlib.node;


import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Identifies if this user is a Damn user.
 */
public class NodeIdentification extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeIdentification.class);
    private final NodeSocket nodeSocket;
    private final NodeManager nodeManager;

    //TODO: give NodeIdentification the id of the new connection
    public NodeIdentification(NodeSocket nodeSocket, NodeManager nodeManager) {
        this.nodeManager = nodeManager;
        this.nodeSocket = nodeSocket;
        this.start();
    }

    @Override
    public void run() {
        while (this.run) {
            try {
                DataInputStream inputStream = new DataInputStream(this.nodeSocket.getSocket().getInputStream());
                String message = inputStream.readUTF();
                if ("I am Damn player".equals(message)) {
                    log.info(String.format("Received HelloMessage from %s", nodeSocket.getIp()));
                    this.nodeManager.putNodeId(0 + NodeManager.counter, this.nodeSocket.getIp());
                    NodeManager.counter++;
                } else {
                    log.info(String.format("%s is not a Damn player", nodeSocket.getIp()));
                }
                this.run = false;
            } catch (IOException e) {
                log.error("NodeIdentification's thread failed: ", e);
            }
        }
    }
}

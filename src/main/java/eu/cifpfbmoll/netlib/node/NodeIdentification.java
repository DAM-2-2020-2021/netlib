package eu.cifpfbmoll.netlib.node;


import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Identifies if this user is a Damn user.
 */
public class NodeIdentification extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeIdentification.class);
    private final NodeSocket nodeSocket;
    private final NodeManager nodeManager;

    public NodeIdentification(NodeSocket nodeSocket, NodeManager nodeManager) {
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
        while (this.run && !this.nodeSocket.isClosed()) {
            try {
                byte[] data = new byte[1024];
                int size = this.nodeSocket.read(data);
                if (size < 0) continue;
                Packet packet = Packet.load(data);
                if (StringUtils.equals(packet.getType(), "HELO")) {
                    log.info(String.format("received Hello packet from %s", this.nodeSocket.getIp()));
                    this.nodeManager.putNodeId(packet.getSourceId(), this.nodeSocket.getIp());
                } else {
                    log.info(String.format("%s is not a netlib node", this.nodeSocket.getIp()));
                }
            } catch (IOException e) {
                log.error("NodeIdentification's thread failed: ", e);
            } finally {
                close();
            }
        }
        log.info("NodeIndentification thread finished");
    }
}

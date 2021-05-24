package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;

public class NodeHealthConnection extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeHealthConnection.class);
    private static final int COMMUNICATION_ATTEMPTS = 5;
    private static final int DELAY = 300;
    private final NodeChannel nodeChannel;
    private final NodeConnection nodeConnection;
    private boolean acknowledgmentReceived;

    /**
     * Creates nodeHealhConnection instance.
     *
     * @param nodeChannel NodeChannel instance.
     */
    public NodeHealthConnection(NodeChannel nodeChannel, NodeConnection nodeConnection) {
        this.nodeConnection = nodeConnection;
        this.nodeChannel = nodeChannel;
        log.info("NodeHealthConnection created for: "+this.nodeConnection.getNode().getIp());
        this.start();
    }

    /**
     * Sets Acknowledgment received attribute true;
     */
    public void setAcknowledgmentReceived() {
        this.acknowledgmentReceived = true;
    }

    /**
     * Send an inputStream in order to verify if connection is OK.
     */
    private void sendAcknowledgment() {
        try {
            this.acknowledgmentReceived = false;
            DataOutputStream outputStream = new DataOutputStream(this.nodeConnection.getNodeSocket().getSocket().getOutputStream());
            outputStream.writeUTF("Can you hear me?");
            log.info("Sending Acknowledgement");
            outputStream.flush();
        } catch (IOException e) {
            log.error("Problem sending Acknowledgement");
        }
    }

    /**
     * Send an acknowledgment in order to check communication state. If acknowledgment does not arrive in 5 retries
     * it will remove the socket of Channel class.
     */
    private void tryFeedback() {
        for (int i = 0; i < COMMUNICATION_ATTEMPTS && !this.acknowledgmentReceived; i++) {
            this.sleep(DELAY);
            log.info("Trying feedback attemp: " + i);
        }
        if (!this.acknowledgmentReceived) {
            log.info("Missing acknowledgement. Removing");
            this.nodeChannel.quitSocket();
        }
    }

    @Override
    public void run() {
        while (this.nodeChannel.isChannelOk()) {
            this.sleep(DELAY);
            this.sendAcknowledgment();
            this.tryFeedback();
        }
        this.nodeChannel.quitSocket();
    }
}

package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NodeHealthConnection extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeHealthConnection.class);
    private boolean healthyConnection;
    private boolean acknowledgmentReceived;
    private int communicationAttempts = 5;
    private final int DELAY = 300;

    public NodeHealthConnection() {
        this.healthyConnection=false;
    }

    private void setAcknowledgmentReceived() {
    }

    public boolean isConnectionOK(){return this.healthyConnection;}

    /**
     * Send an inputStream in order to verify if connection is OK.
     */
    public void sendAcknowledgment() {

    }

    /**
     * Send an acknowledgment in order to check communication state. If acknowledgment does not arrive in 5 retries
     * it will close the Sockets.
     */
    /*private void tryFeedback() {
        try {
            acknowledgmentReceived = false;
            this.sendAcknowledgment();
            for (int i = 0; i < communicationAttempts && !acknowledgmentReceived; i++) {
                Thread.sleep(DELAY);
            }
            if (!acknowledgmentReceived) {
               nodeConnection.getNodeSocket().close();
            }
        } catch (IOException e) {
            log.error("IOException closing NodeSocket");
        } catch (InterruptedException e) {
            log.error("InterruptedException closing NodeSocket");
        }
    }*/

    @Override
    public void run() {

    }
}

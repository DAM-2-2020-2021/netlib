package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Checks if Acknowledgment is received. Otherwise it will restart socket connections.
 */
public class NodeChannel extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeChannel.class);
    private final NodeConnection nodeConnection;
    //private final NodeHealthConnection nodeHealthConnection;
    private boolean healthyChannel;
    private final int COMMUNICATION_ATTEMPTS = 5;

    public NodeChannel(NodeConnection nodeConnection) {
        this.nodeConnection = nodeConnection;
        this.healthyChannel = true;
        //this.nodeHealthConnection = new NodeHealthConnection(this, this.nodeConnection);
        log.info("NodeChannel created!");
        this.start();
    }

    /**
     * Informs if channel's health is Ok.
     *
     * @return True if acknowledgment is received, False otherwise.
     */
    public boolean isChannelOk() {
        return this.healthyChannel;
    }

    /**
     * Closes and removes established socket.
     */
    public synchronized void quitSocket() {
        try {
            this.nodeConnection.getNodeSocket().close();
            this.healthyChannel = false;
            log.info("Removing Socket");
        } catch (IOException e) {
            System.out.println("Problemas quitando el socket de Channel");
            e.printStackTrace();
        }
        notifyAll();
    }

    /**
     * Receives acknowledgments and answer them. Sets healthyChannel to false otherwise.
     */
    private void managingAcknowledgment() {
        try {
            DataInputStream inputStream = new DataInputStream(this.nodeConnection.getNodeSocket().getSocket().getInputStream());
            String message = inputStream.readUTF();
            if ("Can you hear me?".equals(message)) {
                DataOutputStream outputStream = new DataOutputStream(this.nodeConnection.getNodeSocket().getSocket().getOutputStream());
                outputStream.writeUTF("Yes I do");
                outputStream.flush();
                log.info("Answering Acknowledgement");
            } else if ("Yes I do".equals(message)) {
                //this.nodeHealthConnection.setAcknowledgmentReceived();
                log.info("Acknowledgement received!");
            } else if (message == null) {
                log.info("Message content null, establishing new connexion");
                this.healthyChannel = false;
                this.quitSocket();
            }
        } catch (IOException e) {
            log.error("Error getting dataInputStream from socket", e);
            this.healthyChannel = false;
            this.quitSocket();
        }
    }

    public void setHealthyChannel(){
        this.healthyChannel=true;
    }

    @Override
    public void run() {
        while (this.healthyChannel) {
            for (int i = 0; i < COMMUNICATION_ATTEMPTS && !this.healthyChannel; i++) {
                this.sleep(500);
                log.info("Trying feedback attemp: " + i);
            }
            if (!this.healthyChannel) {
                log.info("Missing acknowledgement. Removing socket.");
                this.quitSocket();
            }
        }
    }
}

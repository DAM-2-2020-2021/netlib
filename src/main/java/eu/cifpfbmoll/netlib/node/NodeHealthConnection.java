package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NodeHealthConnection extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeHealthConnection.class);
    private boolean healthyConnection;
    private boolean acknowledgmentReceived;
    private int communicationAttempts = 5;
    private final int DELAY = 300;
    private NodeConnection nodeConnection;

    public NodeHealthConnection(NodeConnection nodeConnection) {
        this.healthyConnection=false;
        this.nodeConnection=nodeConnection;
        this.start();
    }

    private void setAcknowledgmentReceived() {
    }

    public boolean isConnectionOK(){return this.healthyConnection;}

    /**
     * Send an inputStream in order to verify if connection is OK.
     */
    private void sendAcknowledgment() {
        try {
            DataOutputStream outputStream = new DataOutputStream(this.nodeConnection.getNodeSocket().getSocket().getOutputStream());
            outputStream.writeUTF("Can you hear me?");
            outputStream.flush();
            }catch (IOException e){
            System.out.println("IOException sending acknowledgment");
        }
    }

    /**
     * Send an acknowledgment in order to check communication state. If acknowledgment does not arrive in 5 retries
     * it will close the Sockets.
     */
    private void getAcknowledgement(){
        try{
            DataInputStream inputStream=new DataInputStream(this.nodeConnection.getNodeSocket().getSocket().getInputStream());
            String message=inputStream.readUTF();
            if("Can you hear me?".equals(message)){
                this.acknowledgmentReceived=true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }
}

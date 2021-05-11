package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Sends the first message until connects with another pc.
 */
public class NodeClient extends Threaded {
    private boolean identifiedPlayer = false;
    private final int ATTEMPTS=10;
    private String ip;
    private final int DELAY = 300;
    private NodeSocket nodeSocket;
    private NodeManager nodeManager;

    public NodeClient(String ip, NodeSocket nodeSocket, NodeManager nodeManager) {
        this.ip = ip;
        this.nodeManager=nodeManager;
        this.nodeSocket = nodeSocket;
        this.start();
    }

    public void setIdentifiedPlayer(boolean identifiedPlayer) {
        this.identifiedPlayer = identifiedPlayer;
    }

    private void tryFeedback() {
        try {
            DataOutputStream outputStream = new DataOutputStream(this.nodeSocket.getSocket().getOutputStream());
            outputStream.writeUTF("I am Damn player");
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Issue sending outputStream");;
        }
    }

    @Override
    public void run() {
        while (!this.identifiedPlayer) {
            for (int i = 0; i < ATTEMPTS; i++) {
                this.tryFeedback();
                this.sleep(this.DELAY);
                this.identifiedPlayer=this.nodeManager.nodeInHash(this.ip);
            }
            this.identifiedPlayer=true;
        }
    }
}

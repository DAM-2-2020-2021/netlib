package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Sends messages until ip is registered inside nodes HashMap.
 */
public class NodeClient implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(NodeClient.class);
    private static final int DELAY = 300;
    private String ip;
    public Thread t;
    private boolean run;
    private final NodeSocket nodeSocket;
    private final NodeManager nodeManager;

   /* public NodeClient(String ip, NodeSocket nodeSocket, NodeManager nodeManager) {
        this.ip = ip;
        this.nodeManager = nodeManager;
        this.nodeSocket = nodeSocket;
        this.run=true;
        this.t=new Thread(this);
        t.start();
    }*/

    public NodeClient(String ip, NodeManager nodeManager) throws IOException {
        this.ip = ip;
        this.nodeManager = nodeManager;
        Socket socket = new Socket(ip, 9999);
        this.nodeSocket = new NodeSocket(socket);
        this.run=true;
        this.t=new Thread(this);
        //this.t.start();
        //this.start();
    }

    public String getIp() {
        return ip;
    }

    /**
     * Sends a "hello" message in order to be identified as a Damn player.
     */
    private void sendHello() {
        try {
            DataOutputStream outputStream = new DataOutputStream(this.nodeSocket.getOutputStream());
            outputStream.writeUTF("I am Damn player");
            outputStream.flush();
        } catch (IOException e) {
            log.error("Error in NodeClient", e);
        }
    }

    @Override
    public void run() {
        log.info("Cliente modo on");
        while (this.run) {
            this.sendHello();
            try {
                this.t.sleep(DELAY);
            } catch (InterruptedException e) {
                log.error("Error en el sleep");
            }
            if (this.nodeManager.nodeInHash(this.ip)) {
                this.run=false;
            }
        }
        this.nodeManager.removeNodeClient(this);
    }
}

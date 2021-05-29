package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Identifies Damn player and adds it to nodes HashMap
 */
public class NodeIdentification implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(NodeIdentification.class);
    private NodeManager nodeManager;
    private NodeSocket nodeSocket;
    private String ip;
    private Thread t;
    private boolean run;

    public NodeIdentification(NodeManager nodeManager, NodeSocket nodeSocket) {
        this.nodeManager = nodeManager;
        this.nodeSocket = nodeSocket;
        this.ip = nodeSocket.getIp();
        this.run=true;
        this.t=new Thread(this);
        this.t.start();
        //this.start();
    }

    @Override
    public void run() {
        while (this.run) {
            try {
                DataInputStream inputStream = new DataInputStream(this.nodeSocket.getInputStream());
                String message = inputStream.readUTF();
                if (message.equals("I am Damn player")) {
                    log.info("New Damn player identified! " + this.ip);
                    this.nodeManager.putNodeId(NodeManager.getIdFromIp(this.ip), this.ip);
                    this.run=false;
                } else if (message == null) {
                    log.info("Message from " + this.ip + " null.");
                    this.run=false;
                }else{
                    this.run=false;
                }
            } catch (IOException e) {
                log.error("Error in NodeIdentification", e.getMessage());
            }
        }
    }
}

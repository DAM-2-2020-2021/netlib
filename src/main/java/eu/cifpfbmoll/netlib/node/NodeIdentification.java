package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Identifies Damn player and adds it to nodes HashMap
 */
public class NodeIdentification extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeIdentification.class);
    private NodeManager nodeManager;
    private NodeSocket nodeSocket;
    private String ip;

    public NodeIdentification(NodeManager nodeManager, NodeSocket nodeSocket) {
        this.nodeManager = nodeManager;
        this.nodeSocket = nodeSocket;
        this.ip = nodeSocket.getIp();
        this.start();
    }

    @Override
    public void run() {
        while (this.run) {
            try {
                DataInputStream inputStream = new DataInputStream(this.nodeSocket.getSocket().getInputStream());
                String message = inputStream.readUTF();
                if (message.equals("I am Damn player")) {
                    log.info("New Damn player identified! " + this.ip);
                    this.nodeManager.putNodeId(NodeManager.getIdFromIp(this.ip), this.ip);
                    //TODO: here we create a NodeConnection in order to test acknowledgment.
                    //Node node =new  Node(NodeManager.getIdFromIp(this.ip),this.ip);
                    //NodeConnection nodeConnection=new NodeConnection(node,this.nodeSocket,this.nodeManager);
                   // this.nodeManager.addNewConnection(nodeConnection);
                    this.stop();
                } else if (message == null) {
                    log.info("Message from " + this.ip + " null.");
                    this.stop();
                }
            } catch (IOException e) {
                log.error("Error in NodeIdentification", e.getMessage());
            }
        }
    }
}

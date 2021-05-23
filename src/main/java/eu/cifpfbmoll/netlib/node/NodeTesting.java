package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.graalvm.compiler.code.DataSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NodeTesting extends Threaded {

    private static final Logger log = LoggerFactory.getLogger(NodeTesting.class);
    private NodeManager nodeManager;
    private String serverIp;
    private Socket socket;
    private int port;

    public NodeTesting(String ip, int port, NodeManager nodeManager){
        this.serverIp=ip;
        this.nodeManager=nodeManager;
        this.port=port;
    }

    private void sendAcknowledgment(){
        try{
            this.socket=new Socket(serverIp,port);
            DataOutputStream outputStream=new DataOutputStream(this.socket.getOutputStream());
            outputStream.writeUTF("Hellou!");
            outputStream.flush();
            DataInputStream inputStream=new DataInputStream(this.socket.getInputStream());
            String message=inputStream.readUTF();
            if(message.equals("Hi there!")){
                log.info("Mensaje recibido!");
            }else if(message==null){
                log.info("Mensaje nulo. Reiniciando socket");
                this.socket.close();
                this.socket=new Socket(serverIp,port);
            }else{
                log.info("Mensaje no recibido. Reiniciando socket");
                this.socket.close();
                this.socket=new Socket(serverIp,port);
            }
        }catch (IOException e){
            log.error("Error creating socket",e.getMessage());
        }
    }

    @Override
    public void run() {
        while (this.run){
            this.sendAcknowledgment();
            this.sleep(500);
        }
    }
}

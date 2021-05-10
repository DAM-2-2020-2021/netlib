package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The NodeServer Class listens for incoming Node connections and assigns them a new NodeConnection.
 *
 * <p>This is a {@link Threaded} Class so it can run on its own thread.</p>
 * <p>{@link ServerSocket} is used to listen for new connections.</p>
 *
 * @see Threaded
 * @see ServerSocket
 */
public class NodeServer extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeServer.class);
    public static final int DEFAULT_PORT = 420;
    private ServerSocket socket;
    private NodeManager nodeManager;

    public NodeServer(NodeManager nodeManager) {
        this.nodeManager=nodeManager;
        try {
            socket = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            System.out.println("IOException handled from ServerSocket");
        }
        this.start();
    }

    /**
     * Create a new NodeServer from an existing ServerSocket.
     *
     * @param socket ServerSocket to create NodeServer from
     * @throws NullPointerException if socket is null
     */
    protected NodeServer(ServerSocket socket) throws NullPointerException {
        if (socket == null) throw new NullPointerException("ServerSocket must not be null");
        this.socket = socket;
    }

    /**
     * Put to listen in order to connect with the client.
     */
    private void startConnection() {

    }

    @Override
    public void run() {
        while (this.run) {
            try {
                // TODO: use node socket instead
                // TODO: create node connection on client connection
                Socket socket = this.socket.accept();
                System.out.println("Creando conexión con: " + socket.getInetAddress().getHostAddress());
                DataInputStream flujoEntrada = new DataInputStream(socket.getInputStream());
                String message = flujoEntrada.readUTF();
                String clientIp = socket.getInetAddress().getHostAddress();
                // TODO: use internal packets to check for nodes
                if("Yes".equals(message)){
                    if(!this.nodeManager.nodeInHash(clientIp)) {
                        System.out.println("Identificado cliente DummyTask con la ip: " + clientIp);
                        this.nodeManager.addNewPlayer(clientIp); // nodeconnection
                    }
                }else if("DummyTask maybe".equals(message)){
                    DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF("Yes");
                    dataOutputStream.flush();
                }
                flujoEntrada.close();
                socket.close();
            } catch (Exception e) {
                System.out.println("Problem in NodeServer run()");
                e.printStackTrace();
            }
        }
        try {
            this.socket.close();
        } catch (Exception e) {
            log.error("failed to close ServerSocket: ", e);
        }
    }
}

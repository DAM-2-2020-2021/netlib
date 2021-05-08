package eu.cifpfbmoll.netlib.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discover and manage nodes in the network and register
 */
public class NodeManager {
    private static int id=0;
    private final Map<Integer, String> nodes = new HashMap<>();
    //private final PacketManager manager;
    private String ip;
    private String subnet;
    private static final int CALL_TIMEOUT = 1000;
    private NodeServer nodeServer;
    private NodeHealthConnection nodeHealthConnection;
    public List<NodeConnection> nodeConnectionsList=new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(NodeManager.class);
    private List<String> ips;

   /* public NodeManager(PacketManager manager) {
        this.manager = manager;
    }*/

    public NodeManager(String ip) {
        this.ip = ip;
        this.getCurrentSubnet();
        this.discover();
        //this.assignNodes();
        this.nodeServer=new NodeServer();
        this.identifyConnections();
        //this.nodeHealthConnection=new NodeHealthConnection();
        //this.nodeServer=new NodeServer(this.nodeHealthConnection);
    }

    public boolean nodeInHash(String ip){
        return nodes.containsValue(ip);
    }

    public void addNewPlayer(String ip){
        try {
            this.nodeConnectionsList.add(new NodeConnection(new Node(id,ip),new NodeSocket(ip, NodeServer.DEFAULT_PORT),new NodeHealthConnection()));
            this.put(id,ip);
        } catch (IOException e) {
            System.out.println("Error creating socket in NodeManager");
        }
        System.out.println("new player added");
        id++;
    }

    private void identifyConnections() {
        for (int i = 0; i < ips.size(); i++) {
            try {
                Socket socket = new Socket(ips.get(i),NodeServer.DEFAULT_PORT);
                DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("DummyTask maybe");
                dataOutputStream.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("IOException creating a Socket.");
            }
        }
    }

    /**
     * Connect to a node with the specified ID.
     *
     * @param id node ID
     * @return NodeConnection if connection was successful, null otherwise.
     */
    public static NodeConnection connect(Integer id) {
        // get ip for id
        // call connect(ip, port)
        return null;
    }

    /**
     * Connect to a node with the specified IP address and port.
     *
     * @param ip   IP to connect to
     * @param port port to connect to
     * @return NodeConnection if connection was successful, null otherwise.
     */
    public static NodeConnection connect(String ip, Integer port) {
        // TODO: add nodeconnection to list
        return null;
    }


    /*public PacketManager getManager() {
        return manager;
    }*/

    /**
     * Get an IP address for a node ID.
     *
     * @param id node ID
     * @return node IP address or null if no entry was found for ID
     */
    public String getIP(Integer id) {
        return this.nodes.get(id);
    }

    /**
     * Add a node to the table.
     *
     * @param id node ID
     * @param ip node IP address
     */
    public void put(Integer id, String ip) {
        this.nodes.put(id, ip);
    }

    /**
     * Remove Node from the table.
     *
     * @param id node ID
     */
    public void remove(Integer id) {
        this.nodes.remove(id);
    }

    /**
     * Scan for devices connected to the network.
     *
     * <p>If a device is found and responds with their ID,
     * it will be added to the nodes table with its ID.</p>
     */
    public void discover() {
        ips = new ArrayList<>();
        // TODO: Implement with Runner class
        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;
            try {
                if (InetAddress.getByName(host).isReachable(CALL_TIMEOUT)) {
                    ips.add(host);
                    System.out.println(host + " is reachable");
                    ips.add(host);
                    // TODO: Handshake protocol
                    // Create NodeConnection
                    // Send HELLO packet
                    // Receive HELLO response with node id
                    // Add node id with ip to hashmap
                    // Close NodeConnection
                }
            } catch (UnknownHostException e) {
                log.error("UnknownHostException when calling a device.");
            } catch (IOException e) {
                log.error("IOException when calling a device.");
            }
        }
    }

    public void getCurrentSubnet() {
        //ip = InetAddress.getLocalHost().getHostAddress();
        String[] splitIp = ip.split("\\.");
        subnet = String.format("%s.%s.%s", splitIp[0], splitIp[1], splitIp[2]);
    }
}

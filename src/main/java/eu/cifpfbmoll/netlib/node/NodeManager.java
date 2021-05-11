package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.internal.ACKPacket;
import eu.cifpfbmoll.netlib.packet.PacketHandler;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discover and manage nodes in the network and register
 */
public class NodeManager {
    private static final Logger log = LoggerFactory.getLogger(NodeManager.class);
    private static final int CALL_TIMEOUT = 700;
    private final Map<Integer, String> nodes = new HashMap<>();
    private final Map<String, NodeClient> nodeClients = new HashMap<>();
    private final PacketManager packetManager;
    private final int id = 0;
    public static int counter = 0;
    private String ip;
    private String subnet;
    private NodeServer nodeServer;
    private NodeHealthConnection nodeHealthConnection;
    private List<String> ips;
    private List<NodeConnection> nodeConnectionsList = new ArrayList<>();

    public NodeManager(String ip) {
        this.ip = ip;
        this.getCurrentSubnet();
        this.packetManager = new PacketManager();
        this.nodeServer = new NodeServer(this, this.packetManager);
        this.discover();


        // ADD PACKET TYPES
        add(ACKPacket.class, (id, ack) -> {
        });
    }

    public List<String> getIps() {
        return ips;
    }

    public void addNewConnection(NodeConnection nodeConnection) {
        this.nodeConnectionsList.add(nodeConnection);
    }

    public void identifyPlayer(NodeSocket nodeSocket) {
        try {
            DataInputStream inputStream = new DataInputStream(nodeSocket.getSocket().getInputStream());
            String message = inputStream.readUTF();
            if ("I am Damn player".equals(message) && !this.nodeInHash(nodeSocket.getIp())) {
                this.addNewPlayer(nodeSocket);
                this.nodeClients.get(nodeSocket.getIp()).setIdentifiedPlayer(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean nodeInHash(String ip) {
        return nodes.containsValue(ip);
    }

    // TODO: change parameter to NodeConnection
    public void addNewPlayer(NodeSocket nodeSocket) {
        this.nodeConnectionsList.add(new NodeConnection(new Node(counter, nodeSocket.getIp()), nodeSocket, this.packetManager));
        this.nodes.put(counter, nodeSocket.getIp());
        System.out.println("New player: " + nodeSocket.getIp() + " added!");
        counter++;
    }

    private void createNodeClient(String ip) {
        try {
            nodeClients.put(ip, new NodeClient(ip, new NodeSocket(ip, NodeServer.DEFAULT_PORT), this));
        } catch (IOException e) {
            System.out.println("IOException creating a Socket.");
        }
    }

    /**
     * Create a new NodeServer and start listening for connections.
     *
     * @param port listening port
     * @return NodeServer instance
     */
    public static NodeServer startServer(Integer port) {
        return null;
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
     * Add a new Packet Handler for Packet type.
     *
     * @param clazz   object class to handle
     * @param handler packet handler to handle a Packet type
     * @throws NullPointerException     if object's class or handler are null
     * @throws IllegalArgumentException if object's class does not have the PacketType annotation or packet type is already registered
     * @see PacketHandler
     */
    public <T> void add(Class<T> clazz, PacketHandler<T> handler) throws
            NullPointerException, IllegalArgumentException {
        this.packetManager.add(clazz, handler);
    }

    /**
     * Removed Packet Handler for Packet type.
     *
     * @param type packet type to remove
     */
    public void remove(String type) {
        this.packetManager.remove(type);
    }

    /**
     * Removed Packet Handler for Packet type.
     *
     * @param clazz class of the packet type to remove
     */
    public void remove(Class<?> clazz) {
        this.packetManager.remove(clazz);
    }

    /**
     * Scan for devices connected to the network.
     *
     * <p>If a device is found and responds with their ID,
     * it will be added to the nodes table with its ID.</p>
     */
    public void discover() {
        //ips = new ArrayList<>();
        // TODO: Implement with Runner class
        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;
            try {
                if (!host.equals(this.ip)) {
                    if (InetAddress.getByName(host).isReachable(CALL_TIMEOUT)) {
                        //ips.add(host);
                        System.out.println(host + " is reachable");
                        // TODO: Handshake protocol
                        this.createNodeClient(host);
                        // Create NodeConnection
                        //Testing if is dummyTask player
                        // Send HELLO packet
                        // Receive HELLO response with node id
                        // Add node id with ip to hashmap
                        // Close NodeConnection
                    }
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

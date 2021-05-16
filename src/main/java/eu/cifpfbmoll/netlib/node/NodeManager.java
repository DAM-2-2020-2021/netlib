package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.internal.ACKPacket;
import eu.cifpfbmoll.netlib.internal.HelloPacket;
import eu.cifpfbmoll.netlib.packet.PacketHandler;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discover, connect and manage nodes in the network.
 */
public class NodeManager {
    private static final Logger log = LoggerFactory.getLogger(NodeManager.class);
    private final Integer id;
    private final PacketManager manager;
    private final NodeServer nodeServer;
    private final Map<Integer, String> nodes = new HashMap<>();
    private final List<NodeConnection> nodeConnections = new ArrayList<>();

    /**
     * Create a NodeManager with a PacketManager.
     *
     * @param manager PacketManager
     */
    public NodeManager(Integer nodeId, PacketManager manager, boolean server) throws IOException {
        this.id = nodeId;
        this.manager = manager;
        this.nodeServer = server ? new NodeServer(this) : null;

        register(ACKPacket.class, (id, ack) -> {
            System.out.println("received ACK packet from id: " + id);
            System.out.println("ok, this is fucking cool...");
        });

        register(HelloPacket.class, (id, hello) -> {
            System.out.println("received hello packet with ip: " + hello.ip);
        });
    }

    /**
     * Create a NodeManager with ID and specify if the server should be started.
     *
     * @param id     NodeManager id
     * @param server boolean value to create NodeServer or not
     * @throws IOException if NodeServer initialization fails
     */
    public NodeManager(Integer id, boolean server) throws IOException {
        this(id, new PacketManager(), server);
    }

    /**
     * Create a NodeManager with ID.
     *
     * @param id NodeManager id
     * @throws IOException if NodeServer initialization fails
     */
    public NodeManager(Integer id) throws IOException {
        this(id, new PacketManager(), true);
    }

    /**
     * Send a Packet object to an other node with id.
     *
     * @param id     target node id
     * @param packet packet object to send
     * @return true if send was successful, false otherwise
     */
    public boolean send(Integer id, Object packet) {
        NodeConnection conn = nodeConnectionById(id);
        if (conn == null) {
            String ip = this.nodes.get(id);
            if (ip == null) return false;
            try {
                NodeSocket socket = new NodeSocket(ip, NodeServer.DEFAULT_PORT);
                conn = new NodeConnection(new Node(id, ip), socket, this);
            } catch (IOException e) {
                log.error("failed to create connection with ", e);
                return false;
            }
        }
        return conn.send(packet);
    }

    /**
     * Connect and send a Packet object to an other node with ip.
     *
     * <p>This method does not check if the target machine
     * is running the same program and won't be added to the
     * NodeConnection's list, this might cause issues.
     * Use at your own risk.</p>
     *
     * @param ip     target node ip
     * @param port   target node port
     * @param packet packet object to send
     * @return NodeConnection or null if the operation failed
     */
    public NodeConnection send(String ip, int port, Object packet) {
        NodeConnection conn = null;
        try {
            NodeSocket socket = new NodeSocket(ip, port);
            conn = new NodeConnection(new Node(-1, ip), socket, this);
            conn.send(packet);
        } catch (Exception e) {
            log.error("failed to create connection with ", e);
            return null;
        }
        return conn;
    }

    /**
     * Get PacketManager.
     *
     * @return current PacketManager
     */
    public PacketManager getPacketManager() {
        return manager;
    }

    /**
     * Get NodeServer.
     *
     * @return current NodeServer
     */
    public NodeServer getNodeServer() {
        return nodeServer;
    }

    /**
     * Get Node's id
     *
     * @return current node's id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Get an IP address for a node ID.
     *
     * @param id node ID
     * @return node IP address or null if no entry was found for ID
     */
    public String getNodeIPById(Integer id) {
        return this.nodes.get(id);
    }

    /**
     * Check if an IP is present inside the node's list.
     *
     * @param ip Node's ip to check
     * @return true if ip is present, false otherwise
     */
    public boolean isIpPresent(String ip) {
        return this.nodes.containsValue(ip);
    }

    /**
     * Add a node to the table.
     *
     * @param id node ID
     * @param ip node IP address
     */
    public void putNodeId(Integer id, String ip) {
        this.nodes.put(id, ip);
    }

    /**
     * Remove Node from the table.
     *
     * @param id node ID
     */
    public void removeNodeId(Integer id) {
        this.nodes.remove(id);
    }

    /**
     * Get NodeConnection from NodeConnections list with matching node id.
     *
     * @param id node id to look for
     * @return matching NodeConnection
     */
    public NodeConnection nodeConnectionById(Integer id) {
        NodeConnection nodeConnection = null;
        for (NodeConnection conn : this.nodeConnections) {
            if (conn.getNode().getId().equals(id)) {
                nodeConnection = conn;
                break;
            }
        }
        return nodeConnection;
    }

    /**
     * Remove NodeConnection from NodeConnections list with matching node id.
     *
     * @param id node id to remove
     */
    public synchronized void removeNodeConnectionById(Integer id) {
        for (int i = 0; i < this.nodeConnections.size(); i++) {
            NodeConnection conn = this.nodeConnections.get(i);
            if (conn.getNode().getId().equals(id)) {
                this.nodeConnections.remove(i);
                break;
            }
        }
        notifyAll();
    }

    /**
     * Add NodeConnection to NodeConnections list if not present.
     *
     * @param nodeConnection NodeConnection to add
     */
    public synchronized void addNodeConnection(NodeConnection nodeConnection) {
        Integer id = nodeConnection.getNode().getId();
        NodeConnection conn = nodeConnectionById(id);
        if (conn == null)
            this.nodeConnections.add(nodeConnection);
        log.info("added node connection with id: " + id);
        notifyAll();
    }

    /**
     * Remove NodeConnection from NodeConnections list.
     *
     * @param nodeConnection NodeConnection to remove
     */
    public void removeNodeConnection(NodeConnection nodeConnection) {
        removeNodeConnectionById(nodeConnection.getNode().getId());
    }

    /**
     * Register a Packet Handler for Packet type.
     *
     * @param clazz   object class to handle
     * @param handler packet handler to handle a Packet type
     * @throws NullPointerException     if object's class or handler are null
     * @throws IllegalArgumentException if object's class does not have the PacketType annotation or packet type is already registered
     * @see PacketHandler
     */
    public <T> void register(Class<T> clazz, PacketHandler<T> handler) throws NullPointerException, IllegalArgumentException {
        this.manager.add(clazz, handler);
    }

    /**
     * Remove registered Packet Handler for Packet type.
     *
     * @param type packet type to remove
     */
    public void unregister(String type) {
        this.manager.remove(type);
    }

    /**
     * Remove registered Packet Handler for Packet type.
     *
     * @param clazz class of the packet type to remove
     */
    public void unregister(Class<?> clazz) {
        this.manager.remove(clazz);
    }

    /**
     * Scan for devices connected to the network.
     *
     * <p>If a device is found and responds with their ID,
     * it will be added to the nodes table with its ID.</p>
     *
     * @param ips IP list
     */
    public void discover(List<String> ips) {
    }
}

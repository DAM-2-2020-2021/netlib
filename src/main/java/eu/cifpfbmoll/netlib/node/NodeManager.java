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
            System.out.println("received ACK packet");
            System.out.println("ok, this is fucking cool...");
        });

        register(HelloPacket.class, (id, hello) -> {
            System.out.println("received hello packet with ip: " + hello.ip);
        });
    }

    /**
     * Create a Node
     *
     * @param id NodeManager id
     * @param server boolean value to create NodeServer or not
     * @throws IOException if NodeServer initialization fails
     */
    public NodeManager(Integer id, boolean server) throws IOException {
        this(id, new PacketManager(), server);
    }

    /**
     * Create a NodeManager with default values.
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
    public String getIP(Integer id) {
        return this.nodes.get(id);
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
    public void removeNodeConnectionById(Integer id) {
        for (int i = 0; i < this.nodeConnections.size(); i++) {
            NodeConnection conn = this.nodeConnections.get(i);
            if (conn.getNode().getId().equals(id)) {
                this.nodeConnections.remove(i);
                break;
            }
        }
    }

    /**
     * Add NodeConnection to NodeConnections list if not present.
     *
     * @param nodeConnection NodeConnection to add
     */
    public void addNodeConnection(NodeConnection nodeConnection) {
        Integer id = nodeConnection.getNode().getId();
        NodeConnection conn = nodeConnectionById(id);
        if (conn == null)
            this.nodeConnections.add(nodeConnection);
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

package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.PacketHandler;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * Discover, connect and manage nodes in the network.
 */
public class NodeManager {
    private static final Logger log = LoggerFactory.getLogger(NodeManager.class);
    private final List<NodeConnection> nodeConnections = new ArrayList<>();
    private final List<NodeClient> clientList = new ArrayList<>();
    private final Map<Integer, String> nodes = new HashMap<>();
    private final PacketManager packetManager;
    private final NodeServer nodeServer;
    private final Integer id;
    private final String ip;

    /**
     * Get ID for an IP.
     *
     * @param ip IP to get ID from
     * @return IP's ID or 0 if the operation fails
     */
    public static Integer getIdForIp(String ip) {
        try {
            String[] splitIp = ip.split("\\.");
            return Integer.valueOf(splitIp[splitIp.length - 1]);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get all network interfaces.
     *
     * @return List with all network interfaces
     */
    public static List<NetworkInterface> getNetworkInterfaces() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    /**
     * Get network interface by name.
     *
     * @param name name of the interface
     * @return NetworkInterface with matching name or
     * null if no NetworkInterface was found
     */
    public static NetworkInterface getInterfaceByName(String name) {
        try {
            List<NetworkInterface> interfaces = getNetworkInterfaces();
            for (NetworkInterface netint : interfaces) {
                if (StringUtils.equals(netint.getName(), name))
                    return netint;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Get IP for a network interface.
     *
     * @param networkInterface network interface to get network from
     * @return IP of network interface
     */
    public static String getIPForInterface(NetworkInterface networkInterface) {
        try {
            for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                if (address instanceof Inet4Address)
                    return address.getHostAddress();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Creates a NodeManager instance with the user's given ip.
     *
     * @param ip user's ip.
     */
    public NodeManager(String ip) {
        this(ip, true, NodeServer.DEFAULT_PORT);
    }

    public NodeManager(String ip, int serverPort) {
        this(ip, true, serverPort);
    }

    /**
     * Create a NodeManager instance with an IP and specify if
     * NodeServer should be creted.
     *
     * @param ip     user's IP
     * @param server boolean value if server should be created
     */
    public NodeManager(String ip, boolean server, int serverPort) {
        this.id = getIdForIp(ip);
        this.ip = ip;
        this.packetManager = new PacketManager();
        this.nodeServer = server ? new NodeServer(this, serverPort) : null;
    }

    /**
     * Verifies if an specific node's ip is registered in Map.
     *
     * @param ip IP to check in nodes HashMap
     * @return True if is in HashMap, False otherwise.
     */
    public boolean nodeInHash(String ip) {
        return nodes.containsValue(ip);
    }

    /**
     * Connect to a Node by ID.
     *
     * @param id Node ID to connect to
     * @return new NodeConnection with Node or null if connection failed
     */
    public NodeConnection connect(Integer id) {
        NodeConnection conn = nodeConnectionById(id);
        if (conn != null) return conn;
        String ip = this.nodes.get(id);
        if (ip == null) return null;
        try {
            NodeSocket socket = new NodeSocket(ip, NodeServer.DEFAULT_PORT);
            conn = new NodeConnection(new Node(id, ip), socket, this);
            addNodeConnection(conn);
        } catch (IOException e) {
            log.error("failed to create connection with ", e);
        }
        return conn;
    }

    /**
     * Check if we are connected to a specific Node.
     *
     * @param id Node's id
     * @return true if we are connected to the specified node, false otherwise
     */
    public boolean isConnected(Integer id) {
        return nodeConnectionById(id) != null;
    }

    /**
     * Send a Packet object to an other node with id.
     *
     * @param id     target node id
     * @param packet packet object to send
     * @return true if send was successful, false otherwise
     */
    public boolean send(Integer id, Object packet) {
        NodeConnection conn = connect(id);
        if (conn != null) return conn.send(packet);
        if (this.nodes.get(id) == null)
            broadcast(packet);
        return false;
    }

    /**
     * Send a single packet to a node and disconnect.
     *
     * @param id     node id to send packet to
     * @param object packet object to send
     * @return true if send was successful, false otherwise
     */
    public boolean sendAndDisconnect(Integer id, Object object) {
        boolean result = false;
        NodeConnection conn = connect(id);
        if (conn != null) {
            result = conn.send(object);
            conn.getNodeSocket().safeClose();
        }
        return result;
    }

    /**
     * Broadcast a packet object to all known nodes.
     *
     * @param object packet object to send
     */
    public void broadcast(Object object) {
        for (Integer id : this.nodes.keySet()) {
            NodeConnection conn = nodeConnectionById(id);
            if (conn != null) {
                conn.send(object);
            } else {
                sendAndDisconnect(id, object);
            }
        }
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
        return this.packetManager;
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
     * Get Node's ID by its IP.
     *
     * @param ip Node's IP
     * @return Node's ID or null if no matching IP was found
     */
    public Integer getNodeIdByIP(String ip) {
        for (Integer id : this.nodes.keySet())
            if (StringUtils.equals(this.nodes.get(id), ip))
                return id;
        return null;
    }

    /**
     * Add a node to the table.
     *
     * @param id node ID
     * @param ip node IP address
     */
    public synchronized void addNode(Integer id, String ip) {
        this.nodes.put(id, ip);
        this.removeNodeClientByIp(ip);
        log.info(String.format("added node: %d - %s", id, ip));
        notifyAll();
    }

    /**
     * Get nodes HashMap.
     *
     * @return nodes hashmap
     */
    public Map<Integer, String> getNodes() {
        return this.nodes;
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
        if (conn != null) {
            conn.getNodeSocket().safeClose();
            this.removeNodeConnectionById(conn.getNode().getId());
        }
        this.nodeConnections.add(nodeConnection);
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
     * Add NodeClient to NodeClients list.
     *
     * @param nodeClient NodeClient to add
     */
    public synchronized void addNodeClient(NodeClient nodeClient) {
        this.clientList.add(nodeClient);
        notifyAll();
    }

    /**
     * Remove NodeClient from NodeClients list.
     *
     * @param ip NodeClient's IP to remove
     */
    public synchronized void removeNodeClientByIp(String ip) {
        for (NodeClient nc : this.clientList) {
            if (StringUtils.equals(nc.getIp(), ip)) {
                nc.stop();
                this.clientList.remove(nc);
                break;
            }
        }
        notifyAll();
    }

    /**
     * Remove NodeClient from NodeClients list.
     *
     * @param nodeClient NodeClient to remove
     */
    public synchronized void removeNodeClient(NodeClient nodeClient) {
        nodeClient.stop();
        this.clientList.remove(nodeClient);
        notifyAll();
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
        this.packetManager.add(clazz, handler);
    }

    /**
     * Remove registered Packet Handler for Packet type.
     *
     * @param type packet type to remove
     */
    public void unregister(String type) {
        this.packetManager.remove(type);
    }

    /**
     * Remove registered Packet Handler for Packet type.
     *
     * @param clazz class of the packet type to remove
     */
    public void unregister(Class<?> clazz) {
        this.packetManager.remove(clazz);
    }

    /**
     * Check if a certain IP is a Node.
     *
     * @param ip IP to check
     */
    public void discover(String ip) {
        try {
            if (nodeInHash(ip)) return;
            NodeClient nodeClient = new NodeClient(ip, this);
            this.clientList.add(nodeClient);
        } catch (Exception e) {
            log.error("failed to create NodeClient: ", e);
        }
    }

    /**
     * Start Node scan with the specified IPs.
     *
     * @param ips IPs to scan
     */
    public void startScan(List<String> ips) {
        for (String ip : ips) {
            if (StringUtils.equals(this.ip, ip) || nodeInHash(ip)) continue;
            discover(ip);
        }
    }

    /**
     * Stop Nodes scan.
     */
    public void stopScan() {
        for (NodeClient client : this.clientList) {
            client.stop();
        }
    }

    /**
     * Get all of the IP's for a given subnet.
     *
     * @param subnet subnet to calculate IPs from
     * @return list of IPs in subnet
     */
    public List<String> getIpsForSubnet(String subnet) {
        List<String> ips = new ArrayList<>();
        for (int i = 1; i < 255; i++)
            ips.add(String.format("%s.%d", subnet, i));
        return ips;
    }

    /**
     * Get subnet for a given IP.
     *
     * @param ip IP to take subnet from
     * @return IP's subnet
     */
    public String getSubnet(String ip) {
        String[] splitIp = ip.split("\\.");
        return String.format("%s.%s.%s", splitIp[0], splitIp[1], splitIp[2]);
    }
}

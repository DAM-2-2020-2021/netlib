package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketHandler;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Runner;
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
    private static final int CALL_TIMEOUT = 1500;
    private final Map<Integer, String> nodes = new HashMap<>();
    private final List<NodeConnection> nodeConnections = new ArrayList<>();
    private final Integer id;
    private final NodeServer nodeServer;
    private final PacketManager packetManager;
    private final String ip;
    private String subnet;
    private final List<NodeClient> clientList = new ArrayList<>();

    public static int counter = 0;

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
        this(ip, true);
    }

    /**
     * Create a NodeManager instance with an IP and specify if
     * NodeServer should be creted.
     *
     * @param ip     user's IP
     * @param server boolean value if server should be created
     */
    public NodeManager(String ip, boolean server) {
        this.id = getIdForIp(ip);
        this.ip = ip;
        this.getCurrentSubnet();
        this.packetManager = new PacketManager();
        this.nodeServer = server ? new NodeServer(this) : null;
    }

    /**
     * Restarts communications between 2 players.
     *
     * @param nodeConnection NodeConnection to reset
     */
    public synchronized void setUpConnection(NodeConnection nodeConnection) {
        //TODO Ask Serafi if this method is necessary.
        int id = nodeConnection.getNode().getId();
        String ip = this.nodes.get(id);
        this.nodeConnections.remove(nodeConnection);
        try {
            this.nodeConnections.add(new NodeConnection(new Node(id, ip), new NodeSocket(ip, NodeServer.DEFAULT_PORT), this));
        } catch (IOException e) {
            log.error("Problem creating new NoseSocket", e);
        }
        notifyAll();
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
     * Creates a new NodeClient instance from a discovered ip.
     *
     * @param ip Node destination IP
     */
    private void createNodeClient(String ip) {
        try {
            this.clientList.add(new NodeClient(new NodeSocket(ip, NodeServer.DEFAULT_PORT), this));
        } catch (IOException e) {
            log.error("Error creating a socket for NodeClient");
        }
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
            log.info("created new NodeConnection with ip: " + ip);
        } catch (IOException e) {
            log.error("failed to create connection with ", e);
        }
        return conn;
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
        if (conn == null) return false;
        return conn.send(packet);
    }

    /**
     * Send a Packet to an other node with id.
     *
     * @param id     target node id
     * @param packet packet object to send
     * @return true if send was successful, false otherwise
     */
    public boolean send(Integer id, Packet packet) {
        NodeConnection conn = connect(id);
        if (conn == null) return false;
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
    public void putNodeId(Integer id, String ip) {
        this.nodes.put(id, ip);
        log.info(String.format("added node: %d - %s", id, ip));
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
            conn.close();
            this.removeNodeConnectionById(conn.getNode().getId());
        }
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
     * @param nodeClient NodeClient to remove
     */
    public synchronized void removeNodeClient(NodeClient nodeClient) {
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
     * Scan for devices connected to the network.
     *
     * <p>If a device is found and responds with their ID,
     * it will be added to the nodes table with its ID.</p>
     */
    public void discover() {
        // TODO: Find out why runners do not find reachable IP's
        /*List<Runner<String>> runners = new ArrayList<>();
        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;
            Runner<String> runner = new Runner<>(host, ip -> {
                try {
                    if (!ip.equals(this.ip)) {
                        if (InetAddress.getByName(ip).isReachable(CALL_TIMEOUT)) {
                            log.info(String.format("%s is reachable", ip));
                            this.createNodeClient(ip);
                        }
                    }
                } catch (UnknownHostException e) {
                    log.error("UnknownHostException when calling a device.");
                } catch (IOException e) {
                    log.error("IOException when calling a device.");
                }
            });
            runner.start();
            runners.add(runner);
        }
        runners.forEach(runner -> {
            runner.join(CALL_TIMEOUT);
        });*/
        for (int i = 0; i < 255; i++) {
            String host = subnet + "." + i;
            if (!host.equals(this.ip)) {
                this.createNodeClient(host);
            }
        }
    }

    public void discover(String ip) {
        try {
            if (nodeInHash(ip)) return;
            NodeClient nodeClient = new NodeClient(new NodeSocket(ip, NodeServer.DEFAULT_PORT), this);
            this.clientList.add(nodeClient);
        } catch (Exception e) {
            log.error("failed to create NodeClient: ", e);
        }
    }

    private void startScan(String ip, Runner runner) {
        // TODO: comprovar totes les ips que no estiguin dins del node HashMap
        // TODO: Crear List<NodeClient> global per a poder iniciar i aturar els threads
        // TODO: És neccesari emplear els Runners?
        for (int i = 0; i < 255; i++) {
            String host = subnet + "." + i;
            if (!host.equals(this.ip) && !this.nodes.containsKey(host)) {
                this.createNodeClient(host);
            }
        }
    }

    private void stopScan() {
        // TODO: aturar threads NodeClient
        for (NodeClient client : this.clientList) {
            client.stop();
        }
    }

    /**
     * Retrieves subnet from user's ip.
     */
    public void getCurrentSubnet() {
        String[] splitIp = ip.split("\\.");
        subnet = String.format("%s.%s.%s", splitIp[0], splitIp[1], splitIp[2]);
    }
}

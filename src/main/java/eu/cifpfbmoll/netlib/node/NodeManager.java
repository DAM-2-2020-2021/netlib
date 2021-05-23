package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.PacketHandler;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Implement list of NodeConnections and helper functions (add, remove, getById...)

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

    //Aquest counter me serveix per fer s'id de moment, s'ha de llevar quan implementem els packets.
    public static int counter = 1;

    /**
     * Creates a NodeManager instance with the user's given ip.
     *
     * @param ip user's ip.
     */
    public NodeManager(Integer id, String ip) {
        this.id = id;
        this.ip = ip;
        this.getCurrentSubnet();
        this.packetManager = new PacketManager();
        this.nodeServer = new NodeServer(this);
        new NodeTesting("192.168.1.102",9999,this);
        //this.discover();
    }

    /**
     * Restarts communications between 2 players.
     *
     * @param nodeConnection NodeConnection to reset
     */
    public synchronized void setUpConnection(NodeConnection nodeConnection) {
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
     * Adds new NodeConnection to ArrayList.
     *
     * @param nodeConnection new NodeConnection.
     */
    public synchronized void addNewConnection(NodeConnection nodeConnection) {
        this.nodeConnections.add(nodeConnection);
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
            new NodeClient(ip, new NodeSocket(ip, NodeServer.DEFAULT_PORT), this);
        } catch (IOException e) {
            log.error("Error creating a socket for NodeClient", e);
        }
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
        List<Runner<String>> runners = new ArrayList<>();
        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;
            Runner<String> runner = new Runner<>(host, ip -> {
                try {
                    //log.info("trying: " + ip);
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
        });
    }

    private void startScan(String ip, Runner runner){

    }

    private void stopScan(){

    }

    /**
     * Retrieves subnet from user's ip.
     */
    public void getCurrentSubnet() {
        String[] splitIp = ip.split("\\.");
        subnet = String.format("%s.%s.%s", splitIp[0], splitIp[1], splitIp[2]);
    }
}

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
 * Discover and manage nodes in the network and register
 */
public class NodeManager {
    private static final Logger log = LoggerFactory.getLogger(NodeManager.class);
    private static final int CALL_TIMEOUT = 700;
    private final Map<Integer, String> nodes = new HashMap<>();
    private final List<NodeConnection> nodeConnectionsList = new ArrayList<>();
    private final Integer id;
    private String ip;
    private String subnet;
    private NodeServer nodeServer;
    private final PacketManager packetManager;

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
        this.discover();
    }

    /**
     * Get NodeManager's ID.
     *
     * @return NodeManager's ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Restarts communications between 2 players.
     *
     * @param nodeConnection
     */
    public synchronized void setUpConnection(NodeConnection nodeConnection) {
        int id = nodeConnection.getNode().getId();
        String ip = this.nodes.get(id);
        this.nodeConnectionsList.remove(nodeConnection);
        try {
            this.nodeConnectionsList.add(new NodeConnection(new Node(id, ip), new NodeSocket(ip, NodeServer.DEFAULT_PORT), this));
        } catch (IOException e) {
            log.error("Problem creating new NoseSocket", e);
        }
    }

    /**
     * Adds new NodeConnection to ArrayList.
     *
     * @param nodeConnection new NodeConnection.
     */
    public synchronized void addNewConnection(NodeConnection nodeConnection) {
        this.nodeConnectionsList.add(nodeConnection);
    }

    /**
     * Verifies if an specific node's ip is registered in Map.
     *
     * @param ip
     * @return True if is in HashMap, False otherwise.
     */
    public boolean nodeInHash(String ip) {
        return nodes.containsValue(ip);
    }

    /**
     * Creates a new NodeClient instance from a discovered ip.
     *
     * @param ip
     */
    private void createNodeClient(String ip) {
        try {
            new NodeClient(ip, new NodeSocket(ip, NodeServer.DEFAULT_PORT), this);
        } catch (IOException e) {
            log.error("Error creating a socket for NodeClient", e);
        }
    }

    /**
     * Creates a new NodeServer and start listening for connections.
     *
     * @param port listening port
     * @return NodeServer instance
     */
    public static NodeServer startServer(Integer port) {
        return null;
    }

    /**
     * Send a Packet object to an other node with id.
     *
     * @param id     target node id
     * @param packet packet object to send
     */
    public void send(Integer id, Object packet) {
        // TODO: Get NodeConnection by id
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
                    log.info("trying: " + ip);
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

    /**
     * Retrieves subnet from user's ip.
     */
    public void getCurrentSubnet() {
        String[] splitIp = ip.split("\\.");
        subnet = String.format("%s.%s.%s", splitIp[0], splitIp[1], splitIp[2]);
    }
}

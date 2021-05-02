package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.packet.PacketManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discover and manage nodes in the network and register
 */
public class NodeManager {
    private final Map<Integer, String> nodes = new HashMap<>();
    private final PacketManager manager;

    /**
     * Create a NodeManager with a PacketManager.
     *
     * @param manager PacketManager
     */
    public NodeManager(PacketManager manager) {
        this.manager = manager;
    }

    /**
     * Connect to a node with the specified ID.
     *
     * @param id node ID
     * @return NodeConnection if connection was successful, null otherwise.
     */
    public static NodeConnection connect(Integer id) {
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
        return null;
    }

    /**
     * Get PacketManager.
     *
     * @return Current PacketManager
     */
    public PacketManager getManager() {
        return manager;
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

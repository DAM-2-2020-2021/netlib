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

    public NodeManager(PacketManager manager) {
        this.manager = manager;
    }

    public PacketManager getManager() {
        return manager;
    }

    public static NodeConnection connect(Integer id) {
        return null;
    }

    public static NodeConnection connect(String ip, Integer port) {
        return null;
    }

    public void put(Integer id, String ip) {
        this.nodes.put(id, ip);
    }

    public void remove(Integer id, String ip) {
        this.nodes.remove(id);
    }

    public String getIP(Integer id) {
        return this.nodes.get(id);
    }

    public void discover(List<String> ips) {
    }
}

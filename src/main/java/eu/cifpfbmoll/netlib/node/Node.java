package eu.cifpfbmoll.netlib.node;

/**
 * A Node is another device running the same code in the same network.
 *
 * <p>The Node Class holds information about a specific Node.</p>
 */
public class Node {
    private int id;
    private String ip;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Create a new Node with a specific ID and IP.
     *
     * @param id node ID
     * @param ip node IP
     */
    public Node(int id, String ip) {
        this.id = id;
        this.ip = ip;
    }
}

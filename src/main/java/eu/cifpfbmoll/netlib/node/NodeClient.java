package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.internal.ACKPacket;
import eu.cifpfbmoll.netlib.internal.HelloPacket;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;

/**
 * Sends messages until connects with another pc.
 */
public class NodeClient extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeClient.class);
    private static final int CONNECTION_DELAY = 1000;
    private static final int ACK_DELAY = 300;
    private final NodeManager manager;
    private final String ip;
    private final PacketManager packetManager = new PacketManager();
    private NodeSocket socket = null;

    /**
     * Creates NodeClient instance with given parameters.
     *
     * @param manager NodeManager's instance.
     */
    public NodeClient(String ip, NodeManager manager) {
        this.manager = manager;
        this.ip = ip;
        this.start();
    }

    /**
     * Get target IP.
     *
     * @return target IP
     */
    public String getIp() {
        return ip;
    }

    @Override
    public void run() {
        HelloPacket hello = new HelloPacket();
        try {
            while (this.run && this.socket == null) {
                this.socket = NodeSocket.connect(this.ip, NodeServer.DEFAULT_PORT);
                Thread.sleep(CONNECTION_DELAY);
            }
            if (this.socket != null) {
                this.packetManager.add(ACKPacket.class, (id, ack) -> {
                    this.manager.addNode(id, this.socket.getIp());
                    this.socket.safeClose();
                });
                while (this.run && !this.socket.isClosed()) {
                    this.socket.send(hello, this.manager.getId(), 0);
                    Thread.sleep(ACK_DELAY);
                    byte[] data = new byte[1024];
                    int size = this.socket.read(data);
                    if (size > 0) {
                        Packet packet = Packet.load(data);
                        this.packetManager.process(packet);
                    }
                }
            }
        } catch (SocketException ignored) {
        } catch (Exception e) {
            log.error("NodeClient's thread failed: ", e);
        } finally {
            if (this.socket != null) this.socket.safeClose();
            this.manager.removeNodeClient(this);
        }
    }
}

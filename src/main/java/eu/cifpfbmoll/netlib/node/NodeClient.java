package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.internal.ACKPacket;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.Objects;

/**
 * Sends messages until connects with another pc.
 */
public class NodeClient extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeClient.class);
    private static final int DELAY = 300;
    private final NodeSocket socket;
    private final NodeManager manager;
    private final PacketManager packetManager = new PacketManager();

    /**
     * Creates NodeClient instance with given parameters.
     *
     * @param socket  new NodeSocket.
     * @param manager NodeManager's instance.
     */
    public NodeClient(NodeSocket socket, NodeManager manager) {
        this.manager = manager;
        this.socket = socket;
        this.start();

        this.packetManager.add(ACKPacket.class, (id, ack) -> {
            System.out.println("received ACK packet from " + id);
        });
    }

    /**
     * Close NodeSocket and finish thread.
     */
    public void close() {
        try {
            log.info("closing NodeClient socket");
            this.socket.close();
        } catch (Exception ignored) {
        }
        this.run = false;
    }

    @Override
    public void run() {
        while (this.run && !this.socket.isClosed()) {
            try {
                byte[] data = new byte[1024];
                int size = this.socket.read(data);
                if (size < 0) continue;
                Packet packet = Packet.load(data);
                this.packetManager.process(packet);
            } catch (Exception e) {
                log.error("NodeConnection thread failed: ", e);
                this.close();
            }
        }
        this.manager.removeNodeClient(this);
        log.info("NodeClient finish: " + this.socket.getIp());
        /*log.info("NodeClient connecting to: " + this.nodeSocket.getIp());
        Packet packet = Packet.create("HELO", this.nodeManager.getId(), 0);
        try {
            while (this.run && !this.nodeManager.nodeInHash(this.nodeSocket.getIp()) && !this.nodeSocket.isClosed()) {
                this.nodeSocket.write(packet.dump());
                Thread.sleep(DELAY);
            }
        } catch (SocketException ignored) {
            log.error("socket: ", ignored);
        } catch (Exception e) {
            log.error("NodeClient's thread failed: ", e);
        } finally {
            close();
            this.nodeManager.removeNodeClient(this);
            log.info("NodeClient finish: " + this.nodeSocket.getIp());
        }*/
    }
}

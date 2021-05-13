package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.internal.HelloPacket;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeIdentification extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeIdentification.class);
    private final NodeSocket socket;
    private final NodeManager manager;
    private final Node node = new Node();
    private final PacketManager packetManager = new PacketManager();

    public NodeIdentification(NodeSocket socket, NodeManager manager) {
        this.socket = socket;
        this.manager = manager;
        log.info("NodeIdentification created");
        this.packetManager.add(HelloPacket.class, (id, packet) -> {
            log.info(String.format("received HelloPacket from (%d, %s)", id, packet.ip));
            this.node.setId(id);
            this.node.setIp(packet.ip);
            NodeConnection conn = new NodeConnection(this.node, this.socket, this.manager);
            this.manager.addNodeConnection(conn);
            this.run = false;
        });
        start();
    }

    public Node getNode() {
        return this.node;
    }

    @Override
    public void run() {
        while (this.run) {
            try {
                byte[] data = new byte[512];
                int size = this.socket.read(data);
                log.info("read size: " + size);
                Packet packet = Packet.load(data);
                log.info("packet data size: " + packet.data.length);
                this.packetManager.process(packet);
            } catch (Exception e) {
                log.error("NodeIdentification's thread failed: ", e);
            }
        }
    }
}

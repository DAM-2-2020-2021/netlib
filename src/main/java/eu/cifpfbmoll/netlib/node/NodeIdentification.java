package eu.cifpfbmoll.netlib.node;


import eu.cifpfbmoll.netlib.internal.ACKPacket;
import eu.cifpfbmoll.netlib.internal.HelloPacket;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;

/**
 * Identifies if this user is a Damn user.
 */
public class NodeIdentification extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeIdentification.class);
    private static final int ATTEMPS = 10;
    private final NodeSocket socket;
    private final NodeManager manager;
    private final PacketManager packetManager = new PacketManager();

    public NodeIdentification(NodeSocket socket, NodeManager manager) {
        this.manager = manager;
        this.socket = socket;
        this.start();

        this.packetManager.add(HelloPacket.class, (id, hello) -> {
            this.socket.send(new ACKPacket(), this.manager.getId(), id);
            this.manager.addNode(id, this.socket.getIp());
            this.socket.safeClose();
        });
    }

    @Override
    public void run() {
        while (this.run && !this.socket.isClosed()) {
            try {
                for (int i = 0; i < ATTEMPS && !this.socket.isClosed(); i++) {
                    byte[] data = new byte[1024];
                    int size = this.socket.read(data);
                    if (size < 0) continue;
                    Packet packet = Packet.load(data);
                    this.packetManager.process(packet);
                }
            } catch (SocketException ignored) {
            } catch (IOException e) {
                log.error("NodeIdentification's thread failed: ", e);
            } finally {
                this.socket.safeClose();
            }
        }
    }
}

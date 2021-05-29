package eu.cifpfbmoll.netlib.node;


import eu.cifpfbmoll.netlib.internal.HelloPacket;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.apache.commons.lang3.StringUtils;
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
            System.out.println("received hello packet from " + id);
        });
    }

    /**
     * Close NodeSocket and finish thread.
     */
    public void close() {
        try {
            log.info("closing NodeIdentification socket");
            this.socket.close();
        } catch (Exception ignored) {
        }
        this.run = false;
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
                log.error("socket: ", ignored);
            } catch (IOException e) {
                log.error("NodeIdentification's thread failed: ", e);
            } finally {
                close();
                log.info("NodeIdentification finish: " + this.socket.getIp());
            }
        }
        /*while (this.run && !this.socket.isClosed()) {
            try {
                for (int i = 0; i < ATTEMPS && !this.socket.isClosed(); i++) {
                    byte[] data = new byte[1024];
                    int size = this.socket.read(data);
                    if (size < 0) continue;
                    Packet packet = Packet.load(data);
                    if (StringUtils.equals(packet.getType(), "HELO")) {
                        this.manager.putNodeId(packet.getSourceId(), this.socket.getIp());
                        close();
                    } else {
                        log.info(String.format("%s is not a netlib node", this.socket.getIp()));
                    }
                }
            } catch (SocketException ignored) {
                log.error("socket: ", ignored);
            } catch (IOException e) {
                log.error("NodeIdentification's thread failed: ", e);
            } finally {
                close();
                log.info("NodeIdentification finish: " + this.socket.getIp());
            }
        }*/
    }
}

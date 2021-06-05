package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.internal.ACKPacket;
import eu.cifpfbmoll.netlib.internal.RYSTPacket;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class NodeChannel extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeChannel.class);
    private static final int SEND_DELAY = 1000;
    private static final int MAX_ACK_TRIES = 5;
    private final NodeConnection nodeConnection;
    private final PacketManager packetManager = new PacketManager();
    private final AtomicInteger tries = new AtomicInteger(0);

    public NodeChannel(NodeConnection nodeConnection) {
        this.nodeConnection = nodeConnection;

        this.packetManager.add(RYSTPacket.class, (id, ryst) -> {
            this.nodeConnection.send(new ACKPacket());
        });

        this.packetManager.add(ACKPacket.class, (id, ack) -> {
            this.tries.set(0);
        });
    }

    /**
     * Get Packet Manager.
     *
     * @return current Packet Manager
     */
    public PacketManager getPacketManager() {
        return packetManager;
    }

    @Override
    public void run() {
        RYSTPacket ryst = new RYSTPacket();
        while (this.run && !this.nodeConnection.getNodeSocket().isClosed()) {
            try {
                this.nodeConnection.send(ryst);
                Thread.sleep(SEND_DELAY);
                if (this.tries.getAndIncrement() > MAX_ACK_TRIES)
                    this.nodeConnection.getNodeSocket().safeClose();
            } catch (Exception e) {
                log.error("NodeChannel thread failed: ", e);
                this.nodeConnection.getNodeSocket().safeClose();
            }
        }
    }
}

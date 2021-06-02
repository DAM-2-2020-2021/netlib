package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.internal.ACKPacket;
import eu.cifpfbmoll.netlib.internal.RYSTPacket;
import eu.cifpfbmoll.netlib.packet.PacketManager;
import eu.cifpfbmoll.netlib.util.Threaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeChannel extends Threaded {
    private static final Logger log = LoggerFactory.getLogger(NodeChannel.class);
    private static final int SEND_DELAY = 750;
    private static final int MAX_ACK_TRIES = 10;
    private final NodeConnection nodeConnection;
    private final PacketManager packetManager = new PacketManager();
    private int tries = 0;

    public NodeChannel(NodeConnection nodeConnection) {
        this.nodeConnection = nodeConnection;

        this.packetManager.add(RYSTPacket.class, (id, ryst) -> {
            System.out.println("received RYST packet");
            this.nodeConnection.send(new ACKPacket());
        });

        this.packetManager.add(ACKPacket.class, (id, ack) -> {
            System.out.println("received ACK packet");
            setTries(0);
        });
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    public int getTries() {
        return tries;
    }

    public synchronized void setTries(int tries) {
        this.tries = tries;
        notifyAll();
    }

    public synchronized void increaseTries() {
        this.tries++;
        notifyAll();
    }

    public synchronized void decreaseTries() {
        this.tries--;
        notifyAll();
    }

    @Override
    public void run() {
        RYSTPacket ryst = new RYSTPacket();
        while (this.run && !this.nodeConnection.getNodeSocket().isClosed()) {
            try {
                this.nodeConnection.send(ryst);
                Thread.sleep(SEND_DELAY);
                if (getTries() > MAX_ACK_TRIES)
                    this.nodeConnection.getNodeSocket().safeClose();
                this.increaseTries();
            } catch (Exception e) {
                log.error("NodeChannel thread failed: ", e);
                this.nodeConnection.getNodeSocket().safeClose();
            }
        }
    }
}

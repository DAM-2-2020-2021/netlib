package eu.cifpfbmoll.netlib.net.packet;

/**
 * Functional Interface to define how to handle a specific Packet type.
 *
 * @see Packet
 */
@FunctionalInterface
public interface PacketHandler {
    void handle(Packet packet);
}
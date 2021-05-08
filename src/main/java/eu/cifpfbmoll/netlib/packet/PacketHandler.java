package eu.cifpfbmoll.netlib.packet;

/**
 * Functional Interface to define how to handle a specific Packet type.
 *
 * @see Packet
 */
@FunctionalInterface
public interface PacketHandler<T> {
    void handle(int id, T object);
}
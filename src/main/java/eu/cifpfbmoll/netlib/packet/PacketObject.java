package eu.cifpfbmoll.netlib.packet;

public interface PacketObject {
    /**
     * Decode PacketObject from byte array.
     *
     * @param data raw byte array.
     */
    void load(byte[] data);

    /**
     * Convert PacketObject data to byte array.
     *
     * @return raw byte array
     */
    byte[] dump();
}

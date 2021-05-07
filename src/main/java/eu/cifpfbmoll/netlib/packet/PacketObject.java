package eu.cifpfbmoll.netlib.packet;

import java.io.Serializable;

public interface PacketObject extends Serializable {
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

package eu.cifpfbmoll.netlib.net.packet;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Defines the structure of a packet to be sent to other nodes on the network.
 *
 * <p>Instances of this Class must be created using the Constructor Factory Method</p>
 */
public class Packet {
    private static final Charset CHARSET_ENCODING = StandardCharsets.UTF_8;
    private static final byte DEFAULT_TYPE_VALUE = 0;
    private static final int DEFAULT_TTL_VALUE = 32;
    private static final int PACKET_TYPE_SIZE = 4;
    private static final int PACKET_TTL_SIZE = 1;
    private static final int PACKET_ID_SIZE = 1;

    public String type;
    public byte ttl;
    public byte src;
    public byte dst;
    public byte resend;
    public byte[] data;

    /**
     * Create a new Packet with all parameters.
     *
     * @param type   packet type
     * @param ttl    time to live
     * @param src    source node id
     * @param dst    destination node id
     * @param resend resend node id
     * @param data   packet data
     * @return new Packet instance
     */
    public static Packet create(String type, Integer ttl, Integer src, Integer dst, Integer resend, byte[] data) {
        return new Packet(Packet.formatType(type), ttl.byteValue(), src.byteValue(), dst.byteValue(), resend.byteValue(), data);
    }

    /**
     * Create a new Packet without specifying the Time to Live field.
     *
     * <p>Time to Live will have its default value.</p>
     *
     * @param type   packet type
     * @param src    source node id
     * @param dst    destination node id
     * @param resend resend node id
     * @param data   packet data
     * @return new Packet instance
     */
    public static Packet create(String type, Integer src, Integer dst, Integer resend, byte[] data) {
        return Packet.create(type, DEFAULT_TTL_VALUE, src, dst, resend, data);
    }

    /**
     * Create a new Packet without specifying the Time to Live field
     * and the resend ID.
     *
     * <p>Time to Live will have its default value.
     * The resend ID will be the same as the source ID.</p>
     *
     * @param type packet type
     * @param src  source node id
     * @param dst  destination node id
     * @param data packet data
     * @return new Packet instance
     */
    public static Packet create(String type, Integer src, Integer dst, byte[] data) {
        return Packet.create(type, DEFAULT_TTL_VALUE, src, dst, src, data);
    }

    /**
     * Decode Packet from byte array.
     *
     * @param bytes raw byte array.
     * @return decoded Packet object
     */
    public static Packet fromBytes(byte[] bytes) {
        byte[] ptype = Arrays.copyOfRange(bytes, 0, PACKET_TYPE_SIZE);
        String type = new String(ptype, CHARSET_ENCODING);
        int index = PACKET_TYPE_SIZE;
        byte ttl = bytes[index++];
        byte src = bytes[index++];
        byte dst = bytes[index++];
        byte resend = bytes[index++];
        byte[] data = Arrays.copyOfRange(bytes, index, bytes.length);
        return new Packet(type, ttl, src, dst, resend, data);
    }

    /**
     * Get the correct format of type header field.
     *
     * <p>Checks for correct length and appends default values if needed.</p>
     *
     * @return corrected Packet type format
     */
    public static String formatType(String type) {
        if (type == null) type = StringUtils.EMPTY;
        byte[] bytes = new byte[PACKET_TYPE_SIZE];
        byte[] str = type.getBytes(CHARSET_ENCODING);
        for (int i = 0; i < PACKET_TYPE_SIZE; i++)
            bytes[i] = (i < str.length) ? str[i] : DEFAULT_TYPE_VALUE;
        return new String(bytes, CHARSET_ENCODING);
    }

    /**
     * Packet constructor with all attributes. This is the only constructor available,
     * in order to create new Packets the constructor factory method must be used.
     *
     * @param type   packet type
     * @param ttl    time to live
     * @param src    source node id
     * @param dst    destination node id
     * @param resend resend node id
     * @param data   packet data
     */
    private Packet(String type, byte ttl, byte src, byte dst, byte resend, byte[] data) {
        this.type = type;
        this.ttl = ttl;
        this.src = src;
        this.dst = dst;
        this.resend = resend;
        this.data = data;
    }

    /**
     * Get packet size.
     *
     * @return packet size
     */
    public int size() {
        return PACKET_TYPE_SIZE + PACKET_TTL_SIZE + PACKET_ID_SIZE * 3 + this.data.length;
    }

    /**
     * Convert Packet data to byte array.
     *
     * @return raw byte array
     */
    public byte[] bytes() {
        byte[] bytes = new byte[this.size()];
        byte[] str = this.type.getBytes(CHARSET_ENCODING);
        int index = 0;
        for (int i = 0; i < PACKET_TYPE_SIZE; i++)
            bytes[index++] = (i < str.length) ? str[i] : DEFAULT_TYPE_VALUE;
        bytes[index++] = this.ttl;
        bytes[index++] = this.src;
        bytes[index++] = this.dst;
        bytes[index++] = this.resend;
        System.arraycopy(this.data, 0, bytes, index, this.data.length);
        return bytes;
    }

    @Override
    public String toString() {
        return String.format("type: [%s]; ttl: [%d]; src: [%d]; dst: [%d]; resend: [%d]", this.type, this.ttl, this.src, this.dst, this.resend);
    }
}
package eu.cifpfbmoll.netlib.packet;

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
    public static final Charset CHARSET_ENCODING = StandardCharsets.UTF_8;
    public static final byte DEFAULT_TYPE_VALUE = 0;
    public static final int DEFAULT_TTL_VALUE = 16;
    public static final int PACKET_TYPE_SIZE = 4;
    public static final int PACKET_TTL_SIZE = 1;
    public static final int PACKET_ID_SIZE = 1;

    public String type;
    public byte ttl;
    public byte src;
    public byte dst;
    public byte[] resend;
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
    public static Packet create(String type, Integer ttl, Integer src, Integer dst, Integer[] resend, byte[] data) {
        byte[] bresend = new byte[resend.length];
        for (int i = 0; i < resend.length; i++)
            bresend[i] = resend[i].byteValue();
        return new Packet(Packet.formatType(type), ttl.byteValue(), src.byteValue(), dst.byteValue(), bresend, data);
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
    public static Packet create(String type, Integer src, Integer dst, Integer[] resend, byte[] data) {
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
        return Packet.create(type, DEFAULT_TTL_VALUE, src, dst, new Integer[]{src}, data);
    }

    /**
     * Create a new Packet from a byte array.
     *
     * @param bytes byte array to load packet from
     * @return deserialized packet
     */
    public static Packet load(byte[] bytes) {
        byte[] ptype = Arrays.copyOfRange(bytes, 0, PACKET_TYPE_SIZE);
        String type = new String(ptype, CHARSET_ENCODING);
        type = Packet.formatType(type);
        int index = PACKET_TYPE_SIZE;
        byte ttl = bytes[index++];
        byte src = bytes[index++];
        byte dst = bytes[index++];
        byte nresend = bytes[index++];
        byte[] resend = new byte[nresend];
        for (int i = 0; i < nresend; i++)
            resend[i] = bytes[index++];
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
     * @param resend resend node ids
     * @param data   packet data
     */
    private Packet(String type, byte ttl, byte src, byte dst, byte[] resend, byte[] data) {
        this.type = type;
        this.ttl = ttl;
        this.src = src;
        this.dst = dst;
        this.resend = resend;
        this.data = data;
    }

    /**
     * Get packet's header size.
     *
     * @return packet header size
     */
    public int headerSize() {
        return PACKET_TYPE_SIZE + PACKET_TTL_SIZE + PACKET_ID_SIZE * 2 + PACKET_ID_SIZE + this.resend.length;
    }

    /**
     * Get packet size.
     *
     * @return packet size
     */
    public int size() {
        return headerSize() + this.data.length;
    }

    /**
     * Add resender id to packet header.
     *
     * @param id new resender id
     */
    public void addResender(Integer id) {
        byte[] tmp = this.resend.clone();
        this.resend = new byte[this.resend.length + PACKET_ID_SIZE];
        System.arraycopy(tmp, 0, this.resend, 0, tmp.length);
        this.resend[this.resend.length - 1] = id.byteValue();
    }

    public byte[] dump() {
        byte[] bytes = new byte[this.size()];
        byte[] str = this.type.getBytes(CHARSET_ENCODING);
        int index = 0;
        for (int i = 0; i < PACKET_TYPE_SIZE; i++)
            bytes[index++] = (i < str.length) ? str[i] : DEFAULT_TYPE_VALUE;
        bytes[index++] = this.ttl;
        bytes[index++] = this.src;
        bytes[index++] = this.dst;
        bytes[index++] = (byte) this.resend.length;
        for (int i = 0; i < this.resend.length; i++)
            bytes[index++] = this.resend[i];
        System.arraycopy(this.data, 0, bytes, index, this.data.length);
        return bytes;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type='" + type + '\'' +
                ", ttl=" + ttl +
                ", src=" + src +
                ", dst=" + dst +
                ", resend=" + Arrays.toString(resend) +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
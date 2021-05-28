package eu.cifpfbmoll.netlib.packet;

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Defines the structure of a packet to be sent to other nodes on the network.
 *
 * <p>Instances of this Class must be created using the Constructor Factory Method</p>
 */
public class Packet {
    public static final int MAX_PACKET_SIZE = 1024;
    public static final Charset CHARSET_ENCODING = StandardCharsets.UTF_8;
    public static final byte DEFAULT_TYPE_VALUE = 0;
    public static final int DEFAULT_TTL_VALUE = 16;
    public static final int PACKET_TYPE_SIZE = 4;
    public static final int PACKET_TTL_SIZE = 1;
    public static final int PACKET_ID_SIZE = 1;

    private String type;
    private byte ttl;
    private byte src;
    private byte dst;
    private byte[] resend;
    private byte[] data;

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
     * Create a new Packet without specifying the Time to Live field
     * and the resend ID.
     *
     * <p>Time to Live will have its default value.
     * The resend ID will be the same as the source ID.</p>
     *
     * @param type packet type
     * @param src  source node id
     * @param dst  destination node id
     * @return new Packet instance
     */
    public static Packet create(String type, Integer src, Integer dst) {
        return Packet.create(type, DEFAULT_TTL_VALUE, src, dst, new Integer[]{src}, new byte[0]);
    }

    /**
     * Create a new Packet from a byte array.
     *
     * @param bytes byte array to load packet from
     * @return deserialized packet
     */
    public static Packet load(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        byte[] ptype = new byte[PACKET_TYPE_SIZE];
        for (int i = 0; i < PACKET_TYPE_SIZE; i++)
            ptype[i] = bb.get();
        String type = formatType(new String(ptype, CHARSET_ENCODING));
        byte ttl = bb.get();
        byte src = bb.get();
        byte dst = bb.get();
        byte nresend = bb.get();
        byte[] resend = new byte[nresend];
        for (int i = 0; i < nresend; i++)
            resend[i] = bb.get();
        short dataSize = bb.getShort();
        byte[] data = new byte[dataSize];
        for (int i = 0; i < dataSize; i++)
            data[i] = bb.get();
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
     * Get source node ID.
     *
     * @return source node ID
     */
    public Integer getSourceId() {
        return this.src & 0xff;
    }

    /**
     * Set source node ID
     *
     * @param id new source node ID
     */
    public void setSourceId(Integer id) {
        this.src = id.byteValue();
    }

    /**
     * Set destination node ID
     *
     * @return destination node ID
     */
    public Integer getDestinationId() {
        return this.dst & 0xff;
    }

    /**
     * Set destination node ID
     *
     * @param id new destination node ID
     */
    public void setDestinationId(Integer id) {
        this.dst = id.byteValue();
    }

    /**
     * Get all resender IDs.
     *
     * @return array with all resender ids
     */
    public Integer[] getResenderIds() {
        Integer[] ids = new Integer[this.resend.length];
        for (int i = 0; i < ids.length; i++)
            ids[i] = (int) this.resend[i];
        return ids;
    }

    /**
     * Set all resender IDs.
     *
     * @param ids new resender IDs.
     */
    public void setResenderIds(Integer... ids) {
        this.resend = new byte[ids.length];
        for (int i = 0; i < ids.length; i++)
            this.resend[i] = ids[i].byteValue();
    }

    /**
     * Add resenders IDs to packet header.
     *
     * @param ids new resender ids
     */
    public void addResender(Integer... ids) {
        byte[] tmp = this.resend.clone();
        this.resend = new byte[this.resend.length + PACKET_ID_SIZE * ids.length];
        System.arraycopy(tmp, 0, this.resend, 0, tmp.length);
        for (int i = 0; i < ids.length; i++)
            this.resend[tmp.length + i] = ids[i].byteValue();
    }

    /**
     * Get current TTL.
     *
     * @return TTL value
     */
    public byte getTTL() {
        return ttl;
    }

    /**
     * Set current TTL.
     *
     * @param ttl TTL value
     */
    public void setTTL(byte ttl) {
        this.ttl = ttl;
    }

    /**
     * Get packet type.
     *
     * @return packet type
     */
    public String getType() {
        return type;
    }

    /**
     * Set packet type.
     *
     * @param type new packet type
     */
    public void setType(String type) {
        this.type = formatType(type);
    }

    /**
     * Get packet data.
     *
     * @return packet data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Set packet data.
     *
     * @param data new packet data
     */
    public void setData(byte[] data) {
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
     * Get packet's maximum header size.
     *
     * @return packet maximum header size
     */
    public int maxHeaderSize() {
        return PACKET_TYPE_SIZE + PACKET_TTL_SIZE + PACKET_ID_SIZE * 2 + PACKET_ID_SIZE * this.ttl;
    }

    /**
     * Get packet's data size.
     *
     * @return packet data size
     */
    public int dataSize() {
        return this.data.length + 2;
    }

    /**
     * Get packet size.
     *
     * @return packet size
     */
    public int size() {
        return headerSize() + dataSize();
    }

    /**
     * Get packet size.
     *
     * @return packet size
     */
    public int maxSize() {
        return maxHeaderSize() + dataSize();
    }

    /**
     * Serialize packet data to byte array.
     *
     * @return Serialized packet as a byte array.
     */
    public byte[] dump() {
        ByteBuffer bb = ByteBuffer.allocate(this.size());
        byte[] str = this.type.getBytes(CHARSET_ENCODING);
        for (int i = 0; i < PACKET_TYPE_SIZE; i++)
            bb.put((i < str.length) ? str[i] : DEFAULT_TYPE_VALUE);
        bb.put(this.ttl);
        bb.put(this.src);
        bb.put(this.dst);
        bb.put((byte) this.resend.length);
        for (byte b : this.resend) bb.put(b);
        bb.putShort((short) this.data.length);
        for (byte b : this.data) bb.put(b);
        return bb.array();
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
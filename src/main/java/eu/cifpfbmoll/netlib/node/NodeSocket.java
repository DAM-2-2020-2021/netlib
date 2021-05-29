package eu.cifpfbmoll.netlib.node;

import eu.cifpfbmoll.netlib.annotation.PacketType;
import eu.cifpfbmoll.netlib.packet.Packet;
import eu.cifpfbmoll.netlib.packet.PacketParser;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * NodeSocket defines basic socket IO operations.
 *
 * <p>{@link InputStream} and {@link OutputStream} are used internally to read/write
 * data from/to the socket.</p>
 *
 * @see InputStream
 * @see OutputStream
 */
public class NodeSocket implements Closeable {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    /**
     * Get Socket from NodeSocket instance.
     *
     * @return Socket.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Create a new NodeSocket with a host and port.
     *
     * @param host host name
     * @param port port number
     * @throws IOException if the assignment of InputStream/OutputStream fails
     */
    public NodeSocket(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    /**
     * Create a new NodeSocket from an existing standard Socket.
     *
     * @param socket socket to create NodeSocket from
     * @throws IOException if the assignment of InputStream/OutputStream fails
     */
    public NodeSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    /**
     * Write data to the current connection using OutputStream.
     *
     * @param data data to send
     * @throws IOException if an IO error occurs
     */
    public void write(byte[] data) throws IOException {
        this.outputStream.write(data);
        this.outputStream.flush();
    }

    /**
     * Provides socket ip.
     *
     * @return NodeSocket's socket ip.
     */
    public String getIp() {
        return this.socket.getInetAddress().getHostAddress();
    }

    /**
     * Read from current connection using InputStream and
     * store the data in a buffer.
     *
     * @param data data buffer used to store read data
     * @return length of read data, or -1 if there is no more to read
     * @throws IOException if an IO error occurs
     */
    public int read(byte[] data) throws IOException {
        return this.inputStream.read(data);
    }

    /**
     * Read from current connection using InputStream and
     * store the data in a buffer.
     *
     * @param data   data buffer used to store read data
     * @param offset offset to read from
     * @param length maximum length of read data
     * @return length of read data, or -1 if there is no more to read
     * @throws IOException if an IO error occurs
     */
    public int read(byte[] data, int offset, int length) throws IOException {
        return this.inputStream.read(data, offset, length);
    }

    /**
     * Send a PacketObject to the connected node.
     *
     * @param object PacketObject to send
     * @return true if send was successful, false otherwise
     */
    public boolean send(Object object, Integer src, Integer dst) {
        if (object == null) return false;
        Class<?> clazz = object.getClass();
        try {
            PacketType packetType = clazz.getAnnotation(PacketType.class);
            if (packetType == null) return false;
            String type = Packet.formatType(packetType.value());
            PacketParser parser = PacketParser.getInstance();
            byte[] data = parser.serialize(object);
            if (data == null) return false;
            Packet packet = Packet.create(type, src, dst, data);
            int size = packet.size();
            if (size > Packet.MAX_PACKET_SIZE)
                throw new IllegalArgumentException(String.format("Object %s passed maximum size: %d/%d", clazz.getSimpleName(), packet.size(), Packet.MAX_PACKET_SIZE));
            write(packet.dump());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if socket is closed.
     *
     * @return true if socket is closed, false otherwise.
     */
    public boolean isClosed() {
        return this.socket.isClosed();
    }

    /**
     * Terminate connection and close socket and InputStream/OutputStream.
     *
     * @throws IOException if an IO error occurs
     */
    @Override
    public void close() throws IOException {
        if (this.inputStream != null) this.inputStream.close();
        if (this.outputStream != null) this.outputStream.close();
        if (this.socket != null) this.socket.close();
    }
}

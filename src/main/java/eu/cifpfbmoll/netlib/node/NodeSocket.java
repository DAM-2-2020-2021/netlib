package eu.cifpfbmoll.netlib.node;

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
public class NodeSocket {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    /**
     * Create a new NodeSocket with a host and port.
     *
     * @param host host name
     * @param port port number
     * @return NodeSocket with specified host and port
     * @throws IOException if the assignment of InputStream/OutputStream fails
     */
    public static NodeSocket create(String host, int port) throws IOException {
        return NodeSocket.create(new Socket(host, port));
    }

    /**
     * Create a new NodeSocket using the constructor factory method.
     *
     * @param socket socket to create NodeSocket from
     * @return NodeSocket with specified socket
     * @throws IOException if the assignment of InputStream/OutputStream fails
     */
    public static NodeSocket create(Socket socket) throws IOException {
        return new NodeSocket(socket);
    }

    /**
     * Create a new NodeSocket from an existing standard Socket.
     *
     * @param socket socket to create NodeSocket from
     * @throws IOException if the assignment of InputStream/OutputStream fails
     */
    private NodeSocket(Socket socket) throws IOException {
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
     * Terminate connection and close socket and InputStream/OutputStream.
     *
     * @throws IOException if an IO error occurs
     */
    public void close() throws IOException {
        if (this.inputStream != null) this.inputStream.close();
        if (this.outputStream != null) this.outputStream.close();
        if (this.socket != null) this.socket.close();
    }
}

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SimpleSyncNioClient {

    private static final byte[] BEGIN = "BEGIN".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] END   = "END".getBytes(StandardCharsets.US_ASCII);

    /**
     * Connects, sends the framed protocol, and closes the connection.
     */
    public static void send(String host, int port, Map<String, Object> payload) throws IOException {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(host, port));

            byte[] payloadBytes = serializeAsciiMap(payload);

            // Frame = BEGIN + length + keyCount + payload + END
            ByteBuffer beginBuf  = ByteBuffer.wrap(BEGIN);
            ByteBuffer lenBuf    = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer countBuf  = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer payloadBuf = ByteBuffer.wrap(payloadBytes);
            ByteBuffer endBuf    = ByteBuffer.wrap(END);

            lenBuf.putInt(payloadBytes.length).flip();
            countBuf.putInt(payload.size()).flip();

            // Synchronous write sequence
            writeFully(channel, beginBuf);
            writeFully(channel, lenBuf);
            writeFully(channel, countBuf);
            writeFully(channel, payloadBuf);
            writeFully(channel, endBuf);
        }
    }

    /**
     * Convert Map<String,Object> → ASCII bytes "key=value\n"
     */
    private static byte[] serializeAsciiMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder(map.size() * 16);
        map.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Standard NIO "write until buffer empty" loop.
     */
    private static void writeFully(SocketChannel channel, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }
}

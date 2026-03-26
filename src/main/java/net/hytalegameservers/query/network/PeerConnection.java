package net.hytalegameservers.query.network;

import lombok.Getter;
import net.hytalegameservers.query.network.interfaces.IPeerConnection;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Wraps a TCP socket connection to a single peer node.
 *
 * <p>Provides a thread-safe {@link #send(String)} method for writing JSON
 * lines to the peer. The {@code synchronized} keyword prevents interleaved
 * writes when multiple threads (heartbeat scheduler, snapshot requests,
 * shutdown broadcasts) write concurrently.</p>
 */
@Getter
public class PeerConnection implements IPeerConnection {

    /**
     * The original "host:port" address string used for identification and reconnection.
     */
    private final String address;
    private final Socket socket;
    private final PrintWriter printWriter;

    public PeerConnection(final String address, final Socket socket) {
        this.address = address;
        this.socket = socket;

        try {
            this.printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create writer for peer %s".formatted(address), e);
        }
    }

    /**
     * Sends a single JSON line to the peer. Synchronized to prevent
     * interleaved writes from concurrent threads.
     */
    @Override
    public synchronized void send(final String line) {
        if (!(this.getSocket().isClosed())) {
            this.getPrintWriter().println(line);
        }
    }

    /**
     * Closes the writer and socket. Silently ignores errors.
     */
    @Override
    public void close() {
        try {
            this.getPrintWriter().close();
            this.getSocket().close();
        } catch (final Exception ignored) {
        }
    }
}

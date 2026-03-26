package net.hytalegameservers.query.network;

import lombok.Getter;
import net.hytalegameservers.query.network.interfaces.IPeerConnection;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Getter
public class PeerConnection implements IPeerConnection {

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

    @Override
    public synchronized void send(final String line) {
        if (!(this.getSocket().isClosed())) {
            this.getPrintWriter().println(line);
        }
    }

    @Override
    public void close() {
        try {
            this.getPrintWriter().close();
            this.getSocket().close();
        } catch (final Exception ignored) {
        }
    }
}

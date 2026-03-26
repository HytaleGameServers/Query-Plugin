package net.hytalegameservers.query.network.interfaces;

public interface IPeerConnection {

    void send(final String line);

    void close();
}

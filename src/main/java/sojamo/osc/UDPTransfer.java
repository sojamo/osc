package sojamo.osc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sojamo.osc.OSC.debug;

public class UDPTransfer extends ATransfer {

    final private int packetSize;
    final private ExecutorService exec = Executors.newFixedThreadPool(1);
    private DatagramSocket socket;

    public UDPTransfer(final String theAddress, final int thePort) {
        this(theAddress, thePort, 1536);
    }

    public UDPTransfer(final String theAddress, final int thePort, final int thePacketSize) {
        super(2048);
        packetSize = thePacketSize;

        InetAddress networkAddress = null;
        try {
            networkAddress = InetAddress.getByName(theAddress);
        } catch (final Exception e) {
            debug("UDPTransfer: network address", theAddress, "not available", e.getMessage());
        }
        try {

            /* open a UDP connection */
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);

            if (networkAddress == null) {
                networkAddress = socket.getLocalAddress();
            }

            socket.bind(new InetSocketAddress(networkAddress, thePort));
            debug("UDPTransfer:", "binding to address", networkAddress, "on port", thePort);

            /* alternatively consider to use java.nio.channels here instead */

            try {

                debug("UDPTransfer:", "UDP socket running on port", thePort);

                exec.execute(new Runnable() {
                    public void run() {
                        final byte[] receiveData = new byte[packetSize];
                        final DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                        try {
                            while (!socket.isClosed()) {

                                socket.receive(packet);
                                final byte[] data = new byte[packet.getLength()];
                                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                                process(data, new NetAddress(packet.getAddress(), packet.getPort()));
                            }
                        } catch (final IOException e) {
                            debug("UDTransfer.exec: UDP socket running on port", thePort, e.getMessage(),
                                    "can't receive messages.");
                        }
                    }
                });

            } catch (final Exception e) {
                debug("UDPTransfer:", "Can't create socket.", e.getMessage());
            }
        } catch (final SocketException e1) {
            debug("UDPTransfer:", "Can't create socket. ", e1.getMessage());
        }
    }

    @Override
    public void send(final IAddress theIAddress, final OscPacket thePacket) {
        try {
            send(theIAddress, thePacket.getBytes());
        } catch (final NullPointerException npe) {
            debug(String.format("UDPTransfer.send: Can't send message (%s) : %s", theIAddress.getHost(),
                    npe.getMessage()));
        }
    }

    @Override
    public void send(final IAddress theIAddress, final byte[] theBytes) {
        final DatagramPacket myPacket = new DatagramPacket(theBytes, theBytes.length,
                ((NetAddress) theIAddress).getAddress(), theIAddress.getPort());

        try {
            socket.send(myPacket);
        } catch (final Exception e) {
            debug(String.format("UDPTransfer.send: Can't send bytes (%s) : %s",
                    myPacket.getAddress().getCanonicalHostName(), e.getMessage()));
        }
    }

    @Override
    public boolean isRunning() {
        return !socket.isClosed();
    }

    @Override
    public void close() {
        socket.close();
    }

}

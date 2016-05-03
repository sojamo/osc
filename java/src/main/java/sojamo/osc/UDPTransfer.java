package sojamo.osc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sojamo.osc.OSC.debug;


public class UDPTransfer extends ATransfer {

    final private int packetSize;
    final private ExecutorService exec = Executors.newFixedThreadPool(1);
    private DatagramSocket socket;

    UDPTransfer(final int thePort) {
        this(thePort, 1536);
    }

    UDPTransfer(final int thePort, final int thePacketSize) {
        super(2048);
        packetSize = thePacketSize;

        try {

            /* open a UDP connection */
            socket = new DatagramSocket(thePort);
            /* alternatively consider to use java.nio.channels here instead */

            try {

                debug("UDP socket running on port", thePort);

                exec.execute(
                        new Runnable() {
                            public void run() {
                                while (!socket.isClosed()) {

                                    byte[] receiveData = new byte[packetSize];
                                    DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);

                                    try {

                                        socket.receive(packet);

                                        byte[] data = new byte[packet.getLength()];

                                        System.arraycopy(
                                                packet.getData(),
                                                packet.getOffset(),
                                                data,
                                                0,
                                                packet.getLength());

                                        process(data);

                                    } catch (IOException e) {
                                        debug("UDP socket running on port", thePort, e.getMessage(), "can't receive messages.");
                                    }
                                }
                            }
                        }
                );

            } catch (Exception e) {
                debug("Can't create socket.", e.getMessage());
            }
        } catch (SocketException e1) {
            debug("Can't create socket. ", e1.getMessage());
        }
    }

    @Override
    public void send(final IAddress theIAddress, final OscPacket thePacket) {
        try {
            final byte[] bytes = thePacket.getBytes();
            DatagramPacket myPacket = new DatagramPacket(
                    bytes,
                    bytes.length,
                    ((NetAddress)theIAddress).getAddress(),
                    theIAddress.getPort());

            try {
                socket.send(myPacket);
            } catch (Exception e) {
                debug(String.format(
                        "Can't send message (%s) : %s",
                        myPacket.getAddress().getCanonicalHostName(),
                        e.getMessage()));
            }

        } catch (NullPointerException npe) {
            debug(String.format(
                    "Can't send message (%s) : %s",
                    theIAddress.getHost(),
                    npe.getMessage()));
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

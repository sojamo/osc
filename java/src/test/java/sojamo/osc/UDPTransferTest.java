package sojamo.osc;

import org.junit.Test;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import static org.junit.Assert.assertTrue;

public class UDPTransferTest {

    @Test
    public void testSend() throws Exception {

        UDPTransfer udp = new UDPTransfer(12000);

        udp.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Map m = OscParser.bytesToPackets(OscParser.bytes(arg));
                OSC.println(m.size(), m);
            }
        });

        OSC.sleep(200);

        assertTrue(udp.isRunning());

        udp.send(new NetAddress(12000), new OscMessage("/test", 1));

        udp.send(new NetAddress(12000), new OscBundle(new OscMessage("/test", 1), new OscMessage("/test", 2)));

        OSC.sleep(200);

        udp.close();

        OSC.sleep(200);

        assertTrue(!udp.isRunning());

    }
}

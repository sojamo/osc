package sojamo.osc;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OscParserTest {

    final OscMessage m1 = new OscMessage("/m1", 1, 2.0f, 3.0d, 4l, "hello", 'c', new byte[]{0x00, 0x01, 0x02}, Arrays.asList(1, Arrays.asList(100, 200), 3), true, false, null, OscImpulse.IMPULSE);
    final String t1 = "ifdhscb[i[ii]i]TFNI";

    @Test
    public void testGetTypetag() throws Exception {
        assertEquals(m1.getTypetag(), t1);
    }

    @Test
    public void testMessageToByteArray() throws Exception {
        byte[] bytes = OscParser.messageToBytes(m1); /* message to bytes */
        OscMessage m2 = OscParser.bytesToMessage(bytes); /* bytes to message */
        assertTrue(m1.getAddress().equals(m2.getAddress()));
        assertTrue(m1.getTypetag().equals(m2.getTypetag()));
    }
}
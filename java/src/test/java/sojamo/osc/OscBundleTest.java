package sojamo.osc;

import org.junit.Test;

import static org.junit.Assert.*;

public class OscBundleTest {

    final OscMessage m1 = new OscMessage("/m2", 1, 2.0f, 3.0d, 4L);
    final OscMessage m2 = new OscMessage("/m1", 1, 2, 3, 4);
    final OscMessage m3 = new OscMessage("/m3", "hello");

    @Test
    public void testBundle() throws Exception {

        OscBundle b1 = new OscBundle();
        b1.add(m1);
        b1.add(m2, m3);
        assertEquals(b1.getPackets().size(), 3);

        OscBundle b2 = new OscBundle();
        b2.add(b1, m1, m2);
        assertEquals(b2.getPackets().size(), 3);

    }

    @Test
    public void testIsImmediate() throws Exception {
        OscBundle b1 = new OscBundle();
        assertTrue(b1.isImmediate());

        OscBundle b2 = new OscBundle();
        b2.getTimetag().setFutureTimeMillis(10000);
        assertFalse(b2.isImmediate());

    }

    @Test
    public void testSetTimetag() throws Exception {
        OscBundle b1 = new OscBundle();
        b1.getTimetag().setFutureTimeMillis(1000);
        assertFalse(b1.getTimetag().isImmediate());
    }

    @Test
    public void testSetTimetag1() throws Exception {
        OscBundle b1 = new OscBundle();
        b1.setTimetag(new OscTimetag().setTimeMillis(System.currentTimeMillis()));
        assertFalse(b1.getTimetag().isImmediate());
    }

    @Test
    public void testAdd() throws Exception {

    }

    @Test
    public void testAdd1() throws Exception {

    }

    @Test
    public void testGetPackets() throws Exception {
        OscBundle b1 = new OscBundle();
        b1.add(m1, m2, m3);
        byte[] bytes = b1.getBytes();
    }
}
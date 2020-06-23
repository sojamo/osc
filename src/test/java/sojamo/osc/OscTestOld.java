package sojamo.osc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class OscTestOld {

    public static void main(String... args) {

        OscTimetag t1 = new OscTimetag();
        OSC.println("timetag t1:", t1);

        OscTimetag t2 = new OscTimetag();
        Date now = new Date();
        now.setTime(now.getTime() + 10000);
        t2.setTimeMillis(now.getTime());
        OSC.println("timetag t2:", t2, now, now);

        OscTimetag t3 = new OscTimetag();
        long jt = System.currentTimeMillis();
        t3.setTimeMillis(jt);
        OSC.println("timetag t3:", t3, jt, t3.toTimeMillis());

        OscTimetag t4 = new OscTimetag();
        t4.setFutureTimeMillis(1000);
        OSC.println("timetag t4:", t4);

        Collection data = new ArrayList(Arrays.asList(1.02f, 2l, 3d, 4, Arrays.asList(4, Arrays.asList(100, 101), 6),
                (char) (2416), "hello", 1));
        OscMessage m1 = new OscMessage("/foo", data);
        byte[] bytes = OscParser.messageToBytes(m1);
        OscMessage m2 = OscParser.bytesToMessage(bytes);
        OSC.println("message m2:", m2);
        OSC.println("arguments\t\t\t", m1.getArguments(), m2.getArguments());
        OSC.println("arguments equal?\t", m1.getArguments().equals(m2.getArguments()));
        OSC.println("typetags\t\t\t", OscParser.getTypetag(m1), OscParser.getTypetag(m2));
        OSC.println("typetags equal?\t\t", OscParser.getTypetag(m1).equals(OscParser.getTypetag(m2)));

        Collection data3 = Arrays.asList(new byte[] { 0x01, 0x02, 0x03, 0x04 });
        OscMessage m3 = new OscMessage("/blob", data3);
        byte[] b3 = OscParser.messageToBytes(m3);
        OSC.printBytes(b3);
        OSC.println(OscParser.bytesToMessage(b3));
    }
}
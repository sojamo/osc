package sojamo.osc;

import java.util.*;

public class OscBundle implements OscPacket {

    private final Collection<OscPacket> packets = new ArrayList<>();
    private final OscTimetag timetag = new OscTimetag();
    static public final byte[] BUNDLE_AS_BYTES = {0x23, 0x62, 0x75, 0x6E, 0x64, 0x6C, 0x65, 0x00};
    static public final int BUNDLE_HEADER_SIZE = 16;

    public OscBundle() {
    }


    public OscBundle(OscPacket... thePackets) {
        this(Arrays.asList(thePackets));
    }

    public OscBundle(Collection<OscPacket> thePackets) {
        addPackets(thePackets);
    }

    public OscBundle add(OscPacket... thePackets) {
        addPackets(Arrays.asList(thePackets));
        return this;
    }

    public OscBundle add(Collection<OscPacket> thePackets) {
        addPackets(thePackets);
        return this;
    }

    private void addPackets(Collection<OscPacket> thePackets) {
        packets.addAll(thePackets);
    }

    protected Collection<OscPacket> getPackets() {
        return packets;
    }

    public boolean isImmediate() {
        return timetag.isImmediate();
    }

    public OscBundle setTimetag(OscTimetag theTimetag) {
        timetag.setTimetag(theTimetag);
        return this;
    }

    public OscTimetag getTimetag() {
        return timetag;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = OscParser.append(BUNDLE_AS_BYTES, timetag.getBytes());
        for (OscPacket packet : packets) {
            byte[] b1 = packet.getBytes();
            bytes = OscParser.append(bytes, OscParser.toBytes(b1.length));
            bytes = OscParser.append(bytes, packet.getBytes());
        }
        return bytes;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder().
                append("OscBundle{").
                append(" timetag=").append(timetag).
                append(", packets={");
        for (OscPacket packet : packets) {
            b.append(" ").append(packet.toString());
        }
        b.append(" }");
        return b.toString();
    }

}

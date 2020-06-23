package sojamo.osc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sojamo.osc.OSC.*;

public class OscMessage implements OscPacket {

    private final List<Object> arguments;
    private final String address;
    private NetAddress receivedFrom;

    public OscMessage(final String theAddress) {
        this(theAddress, new ArrayList<>());
    }

    protected OscMessage(final OscMessage theMessage) {
        this(theMessage.getAddress(), theMessage.getArguments());
    }

    public OscMessage(final String theAddress, final Object... args) {
        this(theAddress, Arrays.asList(args));
    }

    public OscMessage(final String theAddress, final List theArguments) {
        address = theAddress;
        arguments = theArguments;
        /*
         * TODO arguments and address: check for null arguments: should we do a
         * (shallow) copy here? currently passed by reference.
         */
    }

    public OscMessage add(final Object... o) {
        arguments.addAll(Arrays.asList(o));
        return this;
    }

    public OscMessage add(final Object o) {
        arguments.add(o);
        return this;
    }

    public boolean isAddress(final String theAddress) {
        return getAddress().equals(theAddress);
    }

    public String getAddress() {
        return address;
    }

    public boolean isTypetag(final String theTypetag) {
        return getTypetag().equals(theTypetag);
    }

    public String getTypetag() {
        return OscParser.getTypetag(new StringBuilder(), arguments);
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public int getIntAt(final int theIndex) {
        return i(get(theIndex));
    }

    public float getFloatAt(final int theIndex) {
        return f(get(theIndex));
    }

    public char getCharAt(final int theIndex) {
        return (char) (get(theIndex));
    }

    public double getDoubleAt(final int theIndex) {
        return d(get(theIndex));
    }

    public byte[] getBlobAt(final int theIndex) {
        return bytes(get(theIndex));
    }

    public long getLongAt(final int theIndex) {
        return l(get(theIndex));
    }

    public String getStringAt(final int theIndex) {
        return s(get(theIndex));
    }

    public boolean getBooleanAt(final int theIndex) {
        return b(get(theIndex));
    }

    public Object getNilAt(final int theIndex) {
        return get(theIndex);
    }

    public OscImpulse getImpulseAt(final int theIndex) {
        return (OscImpulse) get(theIndex);
    }

    public int getMidiAt(final int theIndex) {
        return i(get(theIndex));
    }

    public int getRGBAAt(final int theIndex) {
        return i(get(theIndex));
    }

    public OscTimetag getTimetagAt(final int theIndex) {
        return get(theIndex) instanceof OscTimetag ? (OscTimetag) get(theIndex) : new OscTimetag();
    }

    public List getListAt(final int theIndex) {
        return toList(get(theIndex));
    }

    public OscSymbol getSymbolAt(final int theIndex) {
        return (OscSymbol) (get(theIndex));
    }

    public Object get(final int theIndex) {
        return getArguments().get(theIndex);
    }

    public void setReceivedFrom(NetAddress theAddress) {
        receivedFrom = theAddress;
    }

    public NetAddress receivedFrom() {
        return receivedFrom;
    }

    @Override
    public byte[] getBytes() {
        return OscParser.messageToBytes(this);
    }

    @Override
    public String toString() {

        String b = "OscMessage{" + " address: " + getAddress() + ", typetag: " + OscParser.getTypetag(this)
                + ", arguments: " + OscParser.asString(arguments) + ", receivedFrom: " + receivedFrom + "}";
        return b;
    }

}

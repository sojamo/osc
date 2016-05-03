package sojamo.osc;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sojamo.osc.OSC.*;

public class OscMessage implements OscPacket {


    private final List<Object> arguments;
    private final String address;

    public OscMessage(String theAddress) {
        this(theAddress, new ArrayList<>());
    }


    protected OscMessage(OscMessage theMessage) {
        this(theMessage.getAddress(), theMessage.getArguments());
    }

    public OscMessage(String theAddress, Object... args) {
        this(theAddress, Arrays.asList(args));
    }

    public OscMessage(String theAddress, List theArguments) {
        address = theAddress;
        arguments = theArguments;
        /* TODO
         * arguments: should we do a (shallow) copy here?
         * currently passed by reference.
         */
    }


    public OscMessage add(Object... o) {
        arguments.addAll(Arrays.asList(o));
        return this;
    }


    public OscMessage add(Object o) {
        arguments.add(o);
        return this;
    }


    public String getAddress() {
        return address;
    }


    public String getTypetag() {
        return OscParser.getTypetag(new StringBuilder(), arguments);
    }


    public List<Object> getArguments() {
        return arguments;
    }


    public int getIntAt(int theIndex) {
        return i(get(theIndex));
    }

    public float getFloatAt(int theIndex) {
        return f(get(theIndex));
    }

    public char getCharAt(int theIndex) {
        return (char) (get(theIndex));
    }

    public double getDoubleAt(int theIndex) {
        return d(get(theIndex));
    }

    public byte[] getBlobAt(int theIndex) {
        return bytes(get(theIndex));
    }

    public long getLongAt(int theIndex) {
        return l(get(theIndex));
    }

    public String getStringAt(int theIndex) {
        return s(get(theIndex));
    }

    public boolean getBooleanAt(int theIndex) {
        return b(get(theIndex));
    }

    public Object getNilAt(int theIndex) {
        return get(theIndex);
    }

    public OscImpulse getImpulseAt(int theIndex) {
        return (OscImpulse) get(theIndex);
    }

    public int getMidiAt(int theIndex) {
        return i(get(theIndex));
    }

    public int getRGBAAt(int theIndex) {
        return i(get(theIndex));
    }

    public OscTimetag getTimetagAt(int theIndex) {
        return get(theIndex) instanceof OscTimetag ? (OscTimetag) get(theIndex) : new OscTimetag();
    }

    public List getListAt(int theIndex) {
        return toList(get(theIndex));
    }

    public OscSymbol getSymbolAt(int theIndex) {
        return (OscSymbol) (get(theIndex));
    }

    public Object get(int theIndex) {
        return getArguments().get(theIndex);
    }

    @Override
    public byte[] getBytes() {
        return OscParser.messageToBytes(this);
    }

    @Override
    public String toString() {
        String b = "OscMessage{" +
                " address=" + getAddress() +
                ", typetag=" + OscParser.getTypetag(this) +
                ", arguments=" + OscParser.asString(arguments) +
                " }";
        return b;
    }

}

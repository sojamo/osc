package sojamo.osc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class OSC {

    public final static String VERSION = "0.2.1";

    public static boolean DEBUG = false;
    final private Collection<OscListener> listeners = new LinkedHashSet<>();
    final private ITransfer transfer;

    /*
     * TODO give examples that show how to handle OscListeners and Observers In
     * contrast to the OscP5 class, the OSC class does not support automatic method
     * detection using reflection. In order to receive an OSC message or a raw data
     * packet, use Observers and/or OscListeners - examples are give inside the
     * tests and inline below.
     */

    /*
     * TODO how to consume messages, give examples Contrary to OscP5, the OSC class
     * does not automatically consume received messages. Show how to do that when
     * using this class.
     */

    /**
     * Creates a new OSC instance using a DatagramSocket listening on port thePort.
     */
    public OSC(final String theAddress, final int thePort) {
        this(new UDPTransfer(theAddress, thePort));
    }

    public OSC(final ITransfer theTransfer) {
        transfer = theTransfer;
        System.out.println("OSC library sojamo.osc " + VERSION + " https://github.com/sojamo/osc");
    }

    public ITransfer getTransfer() {
        return transfer;
    }

    public OSC bind(final String theAddressPattern, final int theIndex, final Object theObject, final String theField) {
        return this;
    }

    public OscListener subscribe(final String theAddressPattern, final Object theObject, final String theMethod) {
        return subscribe(checkEventMethod(theObject, theMethod, oscMessageClass, theAddressPattern));
    }

    public OscListener subscribe(final OscListener theListener) {
        if (!theListener.equals(null) && !theListener.equals(dummy)) {
            listeners.add(theListener);
        }
        return theListener;
    }

    public OSC cancel(final OscListener theListener) {
        listeners.remove(theListener);
        return this;
    }

    public int consume() {
        final List<OscMessage> messages = transfer.consume();
        for (final OscMessage message : messages) {
            for (final OscListener listener : listeners) {
                listener.oscEvent(message);
            }
        }
        return messages.size();
    }

    public OSC send(final IAddress theIAddress, final String theAddressPattern, final Object... theArguments) {
        sendPacket(theIAddress, new OscMessage(theAddressPattern, Arrays.asList(theArguments)));
        return this;
    }

    public OSC send(final IAddress theIAddress, final OscMessage theMessage) {
        sendPacket(theIAddress, theMessage);
        return this;
    }

    public OSC send(final IAddress theIAddress, final OscBundle theBundle) {
        sendPacket(theIAddress, theBundle);
        return this;
    }

    private OSC sendPacket(final IAddress theIAddress, final OscPacket thePacket) {
        transfer.send(theIAddress, thePacket);
        return this;
    }

    protected OSC invoke(final Object theObject, final String theMethodName, final Class<?>[] theClasses,
            final Object[] theArgs) {
        try {
            final Method method = theObject.getClass().getMethod(theMethodName, theClasses);
            try {
                method.invoke(theObject, theArgs);
            } catch (final Exception e) {
                debug("OSC.invoke():", "Invoking", theArgs, "failed", e.getMessage());
            }

        } catch (final NoSuchMethodException e) {
            debug("OSC.invoke():", "Invoking method", theMethodName, "failed", e.getMessage());
        }
        return this;
    }

    protected boolean match(final String s1, final String s2) {
        return true;
    }

    protected OscListener checkEventMethod(final Object theObject, final String theMethod, final Class<?>[] theClass,
            final String theAddressPattern) {
        try {
            final Method method = theObject.getClass().getDeclaredMethod(theMethod, theClass);
            method.setAccessible(true);
            return new OscListener() {
                @Override
                public void oscEvent(final OscMessage m) {
                    if (match(m.getAddress(), theAddressPattern)) {
                        try {
                            method.invoke(theObject, m);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                        }
                    }
                }
            };
        } catch (SecurityException | NoSuchMethodException e1) {
            debug("OSC.checkEventMethod: failed", e1.getMessage());
        }
        return dummy;
    }

    protected void dispose() {
        transfer.close();
    }

    static protected final Class[] oscMessageClass = new Class[] { OscMessage.class };
    static protected final Class[] byteArrayClass = new Class[] { byte[].class };

    static private final OscListener dummy = new OscListener() {
        @Override
        public void oscEvent(final OscMessage m) {
        }
    };

    static public void debug(final Object... strs) {
        if (DEBUG) {
            println(strs);
        }
    }

    static public void print(final Object... strs) {
        for (final Object str : strs) {
            System.out.print(str + " ");
        }
    }

    static public void println(final Object... strs) {
        print(strs);
        System.out.println();
    }

    static public final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F' };

    static public void printBytes(final byte[] byteArray) {
        for (int i = 0; i < byteArray.length; i++) {

            println((char) byteArray[i], "(", hexDigits[byteArray[i] >>> 4 & 0xf], hexDigits[byteArray[i] & 0xf], ") ");

            if ((i + 1) % 4 == 0) {
                System.out.print("\n");
            }
        }
    }

    static public int i(final Object o) {
        return i(o, Integer.MIN_VALUE);
    }

    static public int i(final Object o, final int theDefault) {
        return (o instanceof Number) ? ((Number) o).intValue() : (o instanceof String) ? i(s(o)) : theDefault;
    }

    static public int i(final String o) {
        return i(o, Integer.MIN_VALUE);
    }

    static public int i(final String o, final int theDefault) {
        return isNumeric(o) ? Integer.parseInt(o) : theDefault;
    }

    static public float f(final Object o) {
        return f(o, Float.MIN_VALUE);
    }

    static public float f(final Object o, final float theDefault) {
        return (o instanceof Number) ? ((Number) o).floatValue() : (o instanceof String) ? f(s(o)) : theDefault;
    }

    static public float f(final String o) {
        return f(o, Float.MIN_VALUE);
    }

    static public float f(final String o, final float theDefault) {
        return isNumeric(o) ? Float.parseFloat(o) : theDefault;
    }

    static public double d(final Object o) {
        return d(o, Double.MIN_VALUE);
    }

    static public double d(final Object o, final double theDefault) {
        return (o instanceof Number) ? ((Number) o).doubleValue() : (o instanceof String) ? d(s(o)) : theDefault;
    }

    static public double d(final String o) {
        return d(o, Double.MIN_VALUE);
    }

    static public double d(final String o, final double theDefault) {
        return isNumeric(o) ? Double.parseDouble(o) : theDefault;
    }

    static public long l(final Object o) {
        return l(o, Long.MIN_VALUE);
    }

    static public long l(final Object o, final long theDefault) {
        return (o instanceof Number) ? ((Number) o).longValue() : (o instanceof String) ? l(s(o)) : theDefault;
    }

    static public long l(final String o) {
        return l(o, Integer.MIN_VALUE);
    }

    static public long l(final String o, final long theDefault) {
        return isNumeric(o) ? Long.parseLong(o) : theDefault;
    }

    static public String s(final Object o) {
        return (o != null) ? o.toString() : "";
    }

    static public String s(final Number o, final int theDec) {
        return (o != null) ? String.format("%." + theDec + "f", o.floatValue()) : "";
    }

    static public String s(final Object o, final String theDefault) {
        return (o != null) ? o.toString() : theDefault;
    }

    static public boolean b(final Object o) {
        return b(o, false);
    }

    static public boolean b(final Object o, final boolean theDefault) {
        return (o instanceof Boolean) ? (Boolean) o : (o instanceof Number) ? ((Number) o).intValue() != 0 : theDefault;
    }

    static public boolean b(final String o) {
        return b(o, false);
    }

    static public boolean b(final String o, final boolean theDefault) {
        return o.equalsIgnoreCase("true") || (!o.equalsIgnoreCase("false") && theDefault);
    }

    static public List toList(final Object o) {
        return o != null
                ? (o instanceof List) ? (List) o
                        : (o instanceof String) ? toList(o.toString()) : Collections.emptyList()
                : Collections.emptyList();
    }

    static public byte[] bytes(final Object o) {
        return (o != null && o instanceof byte[]) ? (byte[]) o : new byte[0];
    }

    static public boolean isNumeric(final String str) {
        return str.matches("(-|\\+)?\\d+(\\.\\d+)?");
    }

    static public boolean sleep(final long theMillis) {
        try {
            Thread.sleep(theMillis);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    static public String time() {
        final Calendar now = Calendar.getInstance();
        return now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND) + "."
                + now.get(Calendar.MILLISECOND);
    }

}
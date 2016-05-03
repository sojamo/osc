package sojamo.osc;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class OSC {

    static boolean DEBUG = true;
    final private Collection<OscListener> listeners = new LinkedHashSet<>();
    final private ITransfer transfer;


    /**
     * Creates a now OSC instance using a DatagramSocket
     * listening on port thePort.
     */
    public OSC(int thePort) {
        this(new UDPTransfer(thePort));
    }


    public OSC(ITransfer theTransfer) {
        transfer = theTransfer;
    }

    public ITransfer getTransfer() {
        return transfer;
    }


    public OscListener subscribe(final Object theObject, final String theMethod, final String theAddressPattern) {
        return subscribe(checkEventMethod(theObject, theMethod, oscMessageClass, theAddressPattern));
    }

    public OscListener subscribe(final OscListener theListener) {
        if(!theListener.equals(null) && !theListener.equals(dummy)) {
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
        for (OscMessage message : messages) {
            for (OscListener listener : listeners) {
                listener.oscEvent(message);
            }
        }
        return messages.size();
    }


    public OSC send(final IAddress theIAddress, final String theAddressPattern, Object... theArguments) {
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


    protected OSC invoke(final Object theObject,
                         final String theMethodName,
                         final Class<?>[] theClasses,
                         final Object[] theArgs) {
        try {
            Method method = theObject.getClass().getMethod(theMethodName, theClasses);
            try {
                method.invoke(theObject, theArgs);
            } catch (Exception e) {
                debug("OSC.invoke().", "Invoking", theArgs, "failed", e.getMessage());
            }

        } catch (NoSuchMethodException e) {
            debug("OSC.invoke().", "Invoking method", theMethodName, "failed", e.getMessage());
        }
        return this;
    }


    protected boolean match(final String s1, final String s2) {
        return true;
    }


    protected OscListener checkEventMethod(final Object theObject,
                                           final String theMethod,
                                           final Class<?>[] theClass,
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
            debug("OSC.checkEventMethod failed", e1.getMessage());
        }
        return dummy;
    }


    protected void dispose() {
        transfer.close();
    }


    static protected final Class[] oscMessageClass = new Class[]{OscMessage.class};

    static private final OscListener dummy = new OscListener() {
        @Override
        public void oscEvent(OscMessage m) {
        }
    };


    static public void debug(final Object... strs) {
        if (DEBUG) {
            println(strs);
        }
    }


    static public void print(final Object... strs) {
        for (Object str : strs) {
            System.out.print(str + " ");
        }
    }


    static public void println(final Object... strs) {
        print(strs);
        System.out.println();
    }


    static public final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    static public void printBytes(byte[] byteArray) {
        for (int i = 0; i < byteArray.length; i++) {

            println(
                    (char) byteArray[i],
                    "(",
                    hexDigits[byteArray[i] >>> 4 & 0xf],
                    hexDigits[byteArray[i] & 0xf],
                    ") ");

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

    static public String s(final Number o, int theDec) {
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
        return o != null ? (o instanceof List) ? (List) o : (o instanceof String) ? toList(o.toString()) : Collections.emptyList() : Collections.emptyList();
    }

    static public byte[] bytes(final Object o) {
        return (o != null && o instanceof byte[]) ? (byte[]) o : new byte[0];
    }

    static public boolean isNumeric(final String str) {
        return str.matches("(-|\\+)?\\d+(\\.\\d+)?");
    }

    static public boolean sleep(long theMillis) {
        try {
            Thread.sleep(theMillis);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
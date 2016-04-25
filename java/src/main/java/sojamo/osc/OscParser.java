package sojamo.osc;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class OscParser {


    private final static byte KOMMA = 0x2C;


    static public String getTypetag(OscMessage theMessage) {
        return getTypetag(new StringBuilder(), theMessage.getArguments());
    }

    static protected String getTypetag(StringBuilder theTypetag, Collection theData) {
        for (Object o : theData) {
            if (o == null) {
                theTypetag.append('N'); /* Nil */
            } else if (o instanceof Integer) { /* Integer */
                theTypetag.append('i');
            } else if (o instanceof Float) { /* Float */
                theTypetag.append('f');
            } else if (o instanceof Double) { /* Double */
                theTypetag.append('d');
            } else if (o instanceof Long) { /* Long */
                theTypetag.append('h');
            } else if (o instanceof OscTimetag) { /* Timetag */
                theTypetag.append('t');
            } else if (o instanceof OscImpulse) { /* Impulse */
                theTypetag.append('I');
            } else if (o instanceof OscSymbol) { /* Symbol */
                theTypetag.append('S');
            } else if (o instanceof Character) { /* Character */
                theTypetag.append('c');
            } else if (o instanceof byte[]) { /* blob */
                theTypetag.append('b');
            } else if (o instanceof String) { /* String */
                theTypetag.append('s');
            } else if (o instanceof Boolean) { /* Boolean */
                theTypetag.append((boolean) o ? 'T' : 'F');
            } else if (o instanceof Collection) { /* Array */
                theTypetag.append('[');
                getTypetag(theTypetag, (Collection) o);
                theTypetag.append(']');
            }
        }
        return theTypetag.toString();
    }

    /**
     * TODO
     * consider to return a list of OSC messages or a Message/Bundle instead of
     * adding them to the schedule-queue directly. Also add the timetag to the
     * message directly by including the timetag when a message is created
     * from a byte-array at byteArrayToMessage().
     */
    static public void byteArrayToPacket(byte[] theBytes,
                                         OSC.Schedule theQueue,
                                         long theMillis) {

        /* check if we are dealing with a valid OSC packet size */
        if (theBytes.length % 4 != 0) {
            return;
        }

        /* check if we are dealing with a Bundle */
        if (startsWith(theBytes, OscBundle.BUNDLE_AS_BYTES)) {

            /* extract timetag */
            final long time = l(Arrays.copyOfRange(theBytes, 8, 16));
            final long javaTime = OscTimetag.toTimeMillis(time);
            final OscTimetag timetag = new OscTimetag();
            timetag.setTimeMillis(javaTime);

            /* determine future time */
            final long millis = timetag.isImmediate() ? OscTimetag.TIMETAG_NOW : javaTime - System.currentTimeMillis();

            /* make a copy of the byte array excluding the Bundle header */
            final byte[] bundle = Arrays.copyOfRange(theBytes, OscBundle.BUNDLE_HEADER_SIZE, theBytes.length);

            /* start parsing the bundle */
            final int size = bundle.length;
            int index = 0;

            /* while parsing the bundle recursively check for Bundles or Messages */
            while (index < size) {
                /* determine the size of the byte block */
                final int len = i(Arrays.copyOfRange(bundle, index, index + 4));
                /* convert byte array to OSC packet recursively */
                byteArrayToPacket(Arrays.copyOfRange(bundle, index + 4, index + len + 4), theQueue, millis);
                index += (len + 4);
            }

        } else {
            /* put the message on the queue if it is to be published immediately */
            if (theMillis == OscTimetag.TIMETAG_NOW) {
                theQueue.immediately(byteArrayToMessage(theBytes));
            } else {
                theQueue.later(byteArrayToMessage(theBytes), theMillis);
            }
        }
    }


    static public OscMessage byteArrayToMessage(byte[] theData) {
        int n = 0;
        final int len = theData.length;

        while (theData[++n] != KOMMA && n < len) {
        }

		/* getting the address pattern */
        final String address = (new String(Arrays.copyOfRange(theData, 0, n))).trim();

		/* if the komma has been found, extract the typetag */
        final StringBuilder typetag = new StringBuilder();

        while (theData[++n] != 0x00 && n < len) {
            typetag.append((char) theData[n]);
        }

		/* now start converting bytes to osc arguments starting from index n */
        n = (n + (4 - n % 4));

        final List<Object> arguments = new ArrayList<>();
        byteArrayToArguments(theData, n, typetag.toString(), arguments);

        /* finally return a new OscMessage */
        return new OscMessage(address, arguments);
    }


    static private int byteArrayToArguments(final byte[] theByteArray,
                                            final int theByteArrayPosition,
                                            final String theTypetag,
                                            final List<Object> theArguments) {
        int byteArrayPosition = theByteArrayPosition;
        int index = 0;
        final int length = theTypetag.length();
        for (; index < length; index++) {
            final char c = theTypetag.charAt(index);
            switch (c) {
                case ('i'): /* integer */
                    theArguments.add(i(Arrays.copyOfRange(theByteArray, byteArrayPosition, byteArrayPosition += 4)));
                    break;
                case ('h'): /* long */
                    theArguments.add(l(Arrays.copyOfRange(theByteArray, byteArrayPosition, byteArrayPosition += 8)));
                    break;
                case ('t'): /* Timetag */
                    final OscTimetag timetag = new OscTimetag();
                    timetag.setTimetag(l(Arrays.copyOfRange(theByteArray, byteArrayPosition, byteArrayPosition += 8)));
                    theArguments.add(timetag);
                    break;
                case ('f'): /* float */
                    theArguments.add(f(Arrays.copyOfRange(theByteArray, byteArrayPosition, byteArrayPosition += 4)));
                    break;
                case ('d'): /* double */
                    theArguments.add(d(Arrays.copyOfRange(theByteArray, byteArrayPosition, byteArrayPosition += 8)));
                    break;
                case ('S'): /* Symbol */
                case ('s'): /* String */
                    int n1 = byteArrayPosition;
                    StringBuilder buffer = new StringBuilder();
                    stringLoop:
                    do {
                        if (theByteArray[n1] == 0x00) {
                            break stringLoop;
                        } else {
                            buffer.append((char) theByteArray[n1]);
                        }
                        n1++;
                    } while (n1 < theByteArray.length);
                    theArguments.add(c=='s' ? buffer.toString() : new OscSymbol(buffer.toString()));
                    byteArrayPosition = n1 + (4 - buffer.length() % 4);
                    break;
                case ('c'): /* character */
                    char chr = c(Arrays.copyOfRange(theByteArray, byteArrayPosition, byteArrayPosition += 4));
                    theArguments.add(chr);
                    break;
                case ('b'): /* blob */
                    int len = i(Arrays.copyOfRange(theByteArray, byteArrayPosition, byteArrayPosition += 4));
                    theArguments.add(bytes(Arrays.copyOfRange(theByteArray, byteArrayPosition, byteArrayPosition += len)));
                    int mod = len % 4;
                    byteArrayPosition += mod == 0 ? 0 : 4 - mod;
                    break;
                case ('N'): /* nil */
                    theArguments.add(null);
                    break;
                case ('I'): /* impulse */
                    theArguments.add(OscImpulse.IMPULSE);
                    break;
                case ('T'): /* true */
                    theArguments.add(true);
                    break;
                case ('F'): /* false */
                    theArguments.add(false);
                    break;
                case ('['): /* array */
                    List<Object> sub = new ArrayList<>();
                    int p0 = theTypetag.indexOf('[') + 1;
                    int p1 = theTypetag.lastIndexOf(']');
                    byteArrayPosition = byteArrayToArguments(theByteArray, byteArrayPosition, theTypetag.substring(p0, p1), sub);
                    index += p1 - p0;
                    theArguments.add(sub);
                    break;
                case (']'):
                    break;
            }
        }
        return byteArrayPosition;
    }


    static public byte[] messageToByteArray(OscMessage theMessage) {
        final byte[] address = theMessage.getAddress().getBytes();
        final byte[] arguments = argumentsToByteArray(theMessage.getArguments());
        return append(append(address, filln(address.length)), arguments);
    }


    static public byte[] argumentsToByteArray(Collection theData) {
        StringBuilder typetag = new StringBuilder();
        typetag.append(',');
        final byte[] arguments = argumentsToByteArray(typetag, theData);
        return append(append(String.valueOf(typetag).getBytes(), filln(typetag.length())), arguments);
    }


    static private byte[] argumentsToByteArray(StringBuilder theTypetag, Collection theData) {
        byte[] arguments = new byte[0];

        for (Object o : theData) {
            if (o == null) {
                theTypetag.append('N');
            } else if (o instanceof Integer) { /* Integer */
                theTypetag.append('i');
                arguments = append(arguments, toBytes(((Integer) o)));
            } else if (o instanceof Float) { /* Float */
                theTypetag.append('f');
                arguments = append(arguments, toBytes(Float.floatToIntBits(((Float) o))));
            } else if (o instanceof Double) { /* Double */
                theTypetag.append('d');
                arguments = append(arguments, toBytes(Double.doubleToLongBits(((Double) o))));
            } else if (o instanceof Long) { /* Long */
                theTypetag.append('h');
                arguments = append(arguments, toBytes((Long) o));
            } else if (o instanceof OscTimetag) { /* Timetag */
                theTypetag.append('t');
                arguments = append(arguments, ((OscTimetag) o).getBytes());
            } else if (o instanceof OscImpulse) { /* Impulse */
                theTypetag.append('I');
            } else if (o instanceof OscSymbol) { /* Symbol */
                theTypetag.append('S');
                arguments = append(arguments, o.toString().getBytes());
                arguments = append(arguments, zeros(o.toString().getBytes().length));
            } else if (o instanceof Character) { /* Character */
                theTypetag.append('c');
                final int chr = (int) ((Character) o);
                arguments = append(arguments, toBytes(chr));
            } else if (o instanceof byte[]) { /* blob */
                theTypetag.append('b');
                final byte[] bytes = (byte[]) o;
                final int len = bytes.length;
                arguments = append(arguments, toBytes(len));
                arguments = append(arguments, bytes);
                arguments = append(arguments, zeros(len));
            } else if (o instanceof String) { /* String */
                theTypetag.append('s');
                arguments = append(arguments, o.toString().getBytes());
                arguments = append(arguments, zeros(o.toString().getBytes().length));
            } else if (o instanceof Boolean) { /* Boolean */
                theTypetag.append((boolean) o ? 'T' : 'F');
            } else if (o instanceof Collection) { /* Collection */
                theTypetag.append('[');
                arguments = append(arguments, argumentsToByteArray(theTypetag, (Collection) o));
                theTypetag.append(']');
            } else {
                /* TODO
                 * should arrays be accepted, if yes, how?
                 * Treat as collection or iterate over array?
                 * */
                System.out.println("type not supported " + o.getClass().getSimpleName());
            }
        }
        return arguments;
    }


    static public byte[] bytes(final Object o) {
        return (o != null && o instanceof byte[]) ? (byte[]) o : new byte[0];
    }


    static public int i(byte abyte0[]) {
        return (abyte0[3] & 0xff) + ((abyte0[2] & 0xff) << 8) + ((abyte0[1] & 0xff) << 16) + ((abyte0[0] & 0xff) << 24);
    }


    static public long l(byte abyte0[]) {
        return ((long) abyte0[7] & 255L) + (((long) abyte0[6] & 255L) << 8) + (((long) abyte0[5] & 255L) << 16) + (((long) abyte0[4] & 255L) << 24) + (((long) abyte0[3] & 255L) << 32)
                + (((long) abyte0[2] & 255L) << 40) + (((long) abyte0[1] & 255L) << 48) + (((long) abyte0[0] & 255L) << 56);
    }


    static public float f(byte abyte0[]) {
        int i = i(abyte0);
        return Float.intBitsToFloat(i);
    }


    static public char c(byte abyte0[]) {
        return (char) i(abyte0);
    }


    static public double d(byte abyte0[]) {
        long l = l(abyte0);
        return Double.longBitsToDouble(l);
    }


    static public boolean startsWith(byte[] theData, byte[] thePattern) {
        byte[] section = Arrays.copyOfRange(theData, 0, thePattern.length);
        return Arrays.equals(thePattern, section);
    }

    static public byte[] append(byte abyte0[], byte abyte1[]) {
        byte abyte2[] = new byte[abyte0.length + abyte1.length];
        System.arraycopy(abyte0, 0, abyte2, 0, abyte0.length);
        System.arraycopy(abyte1, 0, abyte2, abyte0.length, abyte1.length);
        return abyte2;
    }


    static public byte[] toBytes(int i) {
        byte[] buffer = new byte[4];
        for (int n = buffer.length - 1; n >= 1; n--) {
            buffer[n] = (byte) i;
            i >>>= 8;
        }
        buffer[0] = (byte) i;
        return buffer;
    }


    static public byte[] toBytes(long i) {
        byte[] buffer = new byte[8];
        for (int n = buffer.length - 1; n >= 1; n--) {
            buffer[n] = (byte) i;
            i >>>= 8;
        }
        buffer[0] = (byte) i;
        return buffer;
    }

    static public byte[] zeros(int len) {
        int n = 4 - len % 4;
        n = len == 4 ? 0 : n;
        return new byte[n];
    }

    static public byte[] filln(int len) {
        /* adds an additional 4 bytes if the len % 4 = 0, required for address */
        return new byte[4 - len % 4];
    }


    static public String asString(Collection theArguments) {
        StringBuilder s1 = new StringBuilder();
        s1.append("[");
        String d1 = "";
        for (Object o : theArguments) {
            if (o instanceof Collection) {
                s1.append(d1).append(asString((Collection) o));
            } else if (o instanceof byte[]) {
                final byte[] bytes = (byte[]) o;
                final StringBuilder s2 = new StringBuilder();
                s2.append("[");
                String d2 = "";
                for (byte b : bytes) {
                    s2.append(d2).append(String.format("0x%02x", b & 0xff));
                    d2 = ",";
                }
                s2.append("]");
                s1.append(d1).append(s2);
            } else {
                s1.append(d1).append(o.toString());
            }
            d1 = ",";
        }
        s1.append("]");
        return s1.toString();
    }

}

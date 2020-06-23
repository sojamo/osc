package sojamo.osc;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import static sojamo.osc.OSC.debug;

public abstract class ATransfer extends Observable implements ITransfer {

    /*
     * start a scheduled executor service to invoke time-tagged messages in the
     * future
     */
    final private ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);

    /*
     * initialize a queue to store all incoming OSC messages before they are
     * published
     */
    final private ArrayBlockingQueue<OscMessage> queue;

    public ATransfer(final int theQueueSize) {
        queue = new ArrayBlockingQueue<>(theQueueSize);
    }

    @Override
    public void immediately(final OscMessage theMessage) {
        queue.offer(theMessage);
    }

    @Override
    public void later(final OscMessage theMessage, final long theMillis) {
        if (theMillis < 0) {
            return; /* in case the message has expired, don't schedule it. */
        }
        schedule.schedule(new Runnable() {
            @Override
            public void run() {
                queue.offer(theMessage);
            }
        }, theMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void process(final byte[] theBytes, final NetAddress theReceivedFrom) {

        if (OscParser.isBundle(theBytes)) {

            /*
             * use a map to collect all incoming message, then pass the incoming data to the
             * parser
             */
            final Map<OscMessage, Long> collect = OscParser.bytesToPackets(theBytes);

            /* schedule the messages that were received */
            for (final Map.Entry<OscMessage, Long> entry : collect.entrySet()) {
                final OscMessage m = entry.getKey();
                m.setReceivedFrom(theReceivedFrom);
                if (entry.getValue() == OscTimetag.TIMETAG_NOW) {
                    immediately(m);
                } else {
                    later(m, entry.getValue());
                }
            }
        } else {
            final OscMessage m = OscParser.bytesToMessage(theBytes);
            m.setReceivedFrom(theReceivedFrom);
            immediately(m);
        }

        /*
         * to grab a copy of the raw data received, add an observer to the Transfer
         * implementation
         */
        setChanged();
        notifyObservers(theBytes);

        /* messages can now be consumed by calling @see #consume() */
    }

    @Override
    public List<OscMessage> consume() {
        final List<OscMessage> messages = new ArrayList<>();
        queue.drainTo(messages);
        return messages;
    }

    public static HashMap<String, String> getNetworkInterfaces() {
        final HashMap<String, String> interfaces = new HashMap<>();
        final String domain = "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}";
        final String localhost = "localhost";
        final String ip = "(([0-9]{1,3}\\.){3})[0-9]{1,3})";
        final Pattern p = Pattern.compile("^" + domain + "|" + localhost + "|" + ip);
        try {
            for (final Enumeration<NetworkInterface> item = NetworkInterface.getNetworkInterfaces();
                    /** iterate interfaces */
                    item.hasMoreElements();) {
                final NetworkInterface intf = item.nextElement();
                for (final Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                        /** iterate addresses */
                        enumIpAddr.hasMoreElements();) {
                    final String addr = enumIpAddr.nextElement().toString().replaceAll("\\/", "");
                    /** extract ip address and ignore other interfaces */
                    if (p.matcher(addr).matches()) {
                        interfaces.put(intf.getName(), addr);
                    }
                }
            }
        } catch (final SocketException e) {
            debug("ATransfer.printNetworkInterfaces: (error retrieving network interface list)");
        }
        return interfaces;
    }
}

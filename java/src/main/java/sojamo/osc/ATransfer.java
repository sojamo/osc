package sojamo.osc;


import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ATransfer extends Observable implements ITransfer {

    /* start a scheduled executor service to invoke time-tagged messages in the future */
    final private ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);

    /* initialize a queue to store all incoming OSC messages before they are published */
    final private ArrayBlockingQueue<OscMessage> queue;

    public ATransfer(int theQueueSize) {
        queue = new ArrayBlockingQueue<>(theQueueSize);
    }

    @Override
    public void immediately(final OscMessage theMessage) {
        queue.offer(theMessage);
    }

    @Override
    public void later(final OscMessage theMessage, final long theMillis) {
        if (theMillis < 0) {
            return;  /* in case the message has expired, don't schedule it. */
        }
        schedule.schedule(new Runnable() {
            @Override
            public void run() {
                queue.offer(theMessage);
            }
        }, theMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void process(byte[] theBytes) {

        /* TODO validate packet? */

        if (OscParser.isBundle(theBytes)) {

            /* use a map to collect all incoming message, then pass the incoming data to the parser */
            final Map<OscMessage, Long> collect = OscParser.bytesToPackets(theBytes);

            /* schedule the messages that were received */
            for (Map.Entry<OscMessage, Long> entry : collect.entrySet()) {
                if (entry.getValue() == OscTimetag.TIMETAG_NOW) {
                    immediately(entry.getKey());
                } else {
                    later(entry.getKey(), entry.getValue());
                }
            }
        } else {
            immediately(OscParser.bytesToMessage(theBytes));
        }

        /* to grab a copy of the raw data received,
         * add an observer to the Transfer implementation */
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
}

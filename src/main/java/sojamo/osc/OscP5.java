package sojamo.osc;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;

public class OscP5 extends OSC {

    public OscP5(final Object theApp,
                 final int thePort) {

        /* let the superclass know about us */
        super(thePort);

        /* Check if we are dealing with a PApplet */
        registerPApplet(theApp);

        /* Check if theApp implements the oscEvent method */
        subscribe(checkEventMethod(theApp, "oscEvent", oscMessageClass, ""));
        /* OK we are done with initializing OscP5 */


        /* optional and just in case a sketch wants to capture the raw data
         * packet, lets check if a method transferEvent exists which would then
         * receive the raw bytes of a packet (which doesn't have to be an
         * OSC packet). */
        try {
            final Method method = theApp.getClass().getDeclaredMethod("transferEvent", byteArrayClass);
            method.setAccessible(true);
            getTransfer().addObserver(new Observer() {
                public void update(Observable o, Object arg) {
                    try {
                        method.invoke(theApp, (byte[]) arg);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                    }
                }
            });
        } catch (Exception e) {
        }
    }


    /**
     * Check if we are dealing with a PApplet parent.
     * If this is the case, register "dispose".
     * Do so quietly, no error messages will be displayed.
     */
    private void registerPApplet(final Object theObject) {

        final String child = "processing.core.PApplet";
        Object parent = null;

        /* Check if we are dealing with a PApplet instance */
        try {

            final Class<?> childClass = Class.forName(child);
            final Class<?> parentClass = Object.class;

            if (parentClass.isAssignableFrom(childClass)) {
                parent = childClass.newInstance();
                parent = theObject;
            }

            /* After we have found out about PApplet, register draw and dispose with the PApplet */
            invoke(parent, "registerMethod", new Class[]{String.class, Object.class}, new Object[]{"pre", this});
            invoke(parent, "registerMethod", new Class[]{String.class, Object.class}, new Object[]{"dispose", this});

        } catch (Exception e) {
            debug("OscP5.registerPApplet().", "Registering PApplet failed", e.getMessage());
        }

    }

    /**
     * PApplet specific method which is called automatically before the sketch
     * calls its draw routine.
     */
    public void pre() {
        consume();
    }

    /**
     * PApplet specific method which is called when a sketch
     * finishes and closes down.
     */
    public void dispose() {
        debug("Disposing OscP5 instance");
        super.dispose();
    }

}

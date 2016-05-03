package sojamo.osc;


public class OscP5 extends OSC {

    public OscP5(Object theApp, int thePort) {

        /* let the superclass know about us */
        super(thePort);

        /* Check if we are dealing with a PApplet */
        registerPApplet(theApp);

        /* Check if theApp implements the oscEvent method */
        subscribe(checkEventMethod(theApp, "oscEvent", oscMessageClass, "" ));
        /* OK we are done with initializing OscP5 */
    }


    /**
     * Check if we are dealing with a PApplet parent.
     * If this is the case, register "dispose".
     * Do so quietly, no error messages will be displayed.
     */
    private void registerPApplet(Object theObject) {

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

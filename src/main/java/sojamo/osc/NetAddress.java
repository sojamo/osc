package sojamo.osc;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class NetAddress extends InetSocketAddress implements IAddress {

    static public final String LOCALHOST_ADDR = "localhost";
    static public String DEFAULT_ADDR = "0.0.0.0";

    public NetAddress(final int thePort) {
        this(LOCALHOST_ADDR, thePort);
    }

    public NetAddress(final InetAddress theAddr, final int thePort) {
        super(theAddr, thePort);
    }

    public NetAddress(final String theHostname, final int thePort) {
        super(theHostname, thePort);
    }

    @Override
    public String getHost() {
        return super.getHostName();
    }

    @Override
    public String toString() {
        return "NetAddress{" + " address:" + getAddress().getHostAddress() + ", hostname:" + getHostName() + ", port:"
                + getPort() + " }";
    }

}

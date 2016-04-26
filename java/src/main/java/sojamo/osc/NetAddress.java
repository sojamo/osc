package sojamo.osc;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class NetAddress extends InetSocketAddress {

    static public final String LOCALHOST = "localhost";

    public NetAddress(int thePort) {
        this(LOCALHOST, thePort);
    }

    public NetAddress(InetAddress theAddr, int thePort) {
        super(theAddr, thePort);
    }

    public NetAddress(String theHostname, int thePort) {
        super(theHostname, thePort);
    }

    @Override
    public String toString() {
        return "NetAddress{" +
                " address=" + getAddress().getHostAddress() +
                ", hostname=" + getHostName() +
                ", port=" + getPort() +
                " }";
    }
}

package sojamo.osc;

/**
 * Created by andi on 25/4/16.
 */
public interface ITransfer {

    void send(NetAddress theNetAddress, OscPacket thePacket);

    void receive(OscPacket thePacket);

    void close();
}

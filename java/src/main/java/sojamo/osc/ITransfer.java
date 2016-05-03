package sojamo.osc;



import java.util.List;
import java.util.Observer;

public interface ITransfer {

    void send(IAddress theIAddress, OscPacket thePacket);

    void process(byte[] theData);

    void immediately(OscMessage theMessage);

    void later(OscMessage theMessage,long theMillis);

    List<OscMessage> consume();

    boolean isRunning();

    void close();

    void deleteObservers();

    void addObserver(Observer o);

    void deleteObserver(Observer o);

}

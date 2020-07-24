package cj.netos.jpush.pusher;

import cj.studio.ecm.net.CircuitException;
import com.rabbitmq.client.LongString;

public interface IPersonFinder {
    PersonInfo find(String person) throws CircuitException;

}

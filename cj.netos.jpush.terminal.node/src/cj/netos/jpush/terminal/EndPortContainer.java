package cj.netos.jpush.terminal;

import cj.netos.jpush.EndPort;
import cj.netos.jpush.IJPushServiceProvider;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//自动生成队列，接收消息，在同一用户内向其不同的设备广播消息
public class EndPortContainer implements IEndPortContainer {
    IRabbitMQConsumer rabbitMQConsumer;
    Map<String, PersonEndPorts> endPorts;//key是person.即一个用户可拥有多个终结点，也就是一个用户可以在多个设备上登录

    public EndPortContainer(IJPushServiceProvider site) {
        endPorts = new ConcurrentHashMap<>();
        rabbitMQConsumer = (IRabbitMQConsumer) site.getService("$.terminal.rabbitMQConsumer");
    }

    @Override
    public PersonEndPorts getPersonEndPorts(String person) {
        return endPorts.get(person);
    }

    @Override
    public Set<String> listPerson() {
        return endPorts.keySet();
    }

    @Override
    public int count() {
        return endPorts.size();
    }

    //{expireTime=7200000.0, person=cj@la.netos, nickName=大地经济, pubTime=1.595424721408E12, roles=[app:users@la.netos], portal=nodepower, isExpired=false, device=b1c5321a7b40f1b7582e1d36fc04db48}
    @Override
    public EndPort online(Channel channel, Map<String, Object> info) throws CircuitException {
        EndPort endPort = new EndPort(channel);
        endPort.setDevice(info.get("device") + "");
        endPort.setPerson(info.get("person") + "");
        endPort.setNickName(info.get("nickName") + "");
        endPort.setRoles((List<String>) info.get("roles"));
        PersonEndPorts personEndPorts = endPorts.get(endPort.getPerson());
        if (personEndPorts == null) {
            personEndPorts = new PersonEndPorts();
            personEndPorts.setPerson(endPort.getPerson());
            personEndPorts.setNickName(endPort.getNickName());
            personEndPorts.addEndPort(endPort);
            endPorts.put(endPort.getPerson(), personEndPorts);
            try {
                rabbitMQConsumer.bindEndPort(personEndPorts);
                rabbitMQConsumer.consumePersonQueue(personEndPorts);
            } catch (IOException e) {
                throw new CircuitException("500", e);
            }
        } else {
            personEndPorts.addEndPort(endPort);
            if (!personEndPorts.isConsumed()) {
                rabbitMQConsumer.consumePersonQueue(personEndPorts);
            }
        }

        return endPort;
    }

    @Override
    public void offline(EndPort theEndPort) throws CircuitException {
        if (theEndPort == null) {
            return;
        }
        String person = theEndPort.getPerson();
        String device = theEndPort.getDevice();
        PersonEndPorts personEndPorts = endPorts.get(person);
        if (personEndPorts == null) {
            return;
        }
        personEndPorts.removeEndPort(person, device);
        if (personEndPorts.isEmpty()) {
            rabbitMQConsumer.stopConsumePersonQueue(personEndPorts);
        }
    }
}

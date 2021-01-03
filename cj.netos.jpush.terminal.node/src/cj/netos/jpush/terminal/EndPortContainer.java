package cj.netos.jpush.terminal;

import cj.netos.jpush.EndPort;
import cj.netos.jpush.IJPushServiceProvider;
import cj.netos.jpush.IPersistenceMessageService;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//自动生成队列，接收消息，在同一用户内向其不同的设备广播消息
public class EndPortContainer implements IEndPortContainer {
    IRabbitMQConsumer rabbitMQConsumer;
    Map<String, PersonEndPorts> endPorts;//key是person.即一个用户可拥有多个终结点，也就是一个用户可以在多个设备上登录
    IPersistenceMessageService persistenceMessageService;

    public EndPortContainer(IJPushServiceProvider site) {
        endPorts = new ConcurrentHashMap<>();
        rabbitMQConsumer = (IRabbitMQConsumer) site.getService("$.terminal.rabbitMQConsumer");
        persistenceMessageService = (IPersistenceMessageService) site.getService("$.plugin.persistenceMessageService");
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
        if (persistenceMessageService == null) {
            return noNotificationOnline(channel, info);
        }
        return doNotificationOnline(channel, info);
    }

    private EndPort doNotificationOnline(Channel channel, Map<String, Object> info) throws CircuitException {
        //检查用户是否绑定队列；是否在终结点；
        EndPort endPort =  new EndPort(channel);
        endPort.setDevice(info.get("device") + "");
        endPort.setPerson(info.get("person") + "");
        endPort.setNickName(info.get("nickName") + "");
        endPort.setRoles((List<String>) info.get("roles"));

        String person = info.get("person") + "";
        PersonEndPorts personEndPorts = endPorts.get(person);
        if (personEndPorts == null) {//用户没有终结点则新建
            personEndPorts = new PersonEndPorts();
            personEndPorts.setPerson(endPort.getPerson());
            personEndPorts.setNickName(endPort.getNickName());
            endPorts.put(endPort.getPerson(), personEndPorts);
            //用户由空上线时首先到插件中检查未读消息并发终结点发送完后再绑定消费
            persistenceMessageService.downstream(endPort);
        } else if (personEndPorts.isEmpty()) {
            //用户由空上线时首先到插件中检查未读消息并发终结点发送完后再绑定消费
            persistenceMessageService.downstream(endPort);
        }

        personEndPorts.addEndPort(endPort);
        try {
            if (!personEndPorts.isConsumed()) {
                rabbitMQConsumer.bindEndPort(personEndPorts);
                rabbitMQConsumer.consumePersonQueue(personEndPorts);
            }
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }

        if (persistenceMessageService != null) {
            persistenceMessageService.checkAndUpdateBuddyDevice(endPort);
        }
        return endPort;
    }

    private EndPort noNotificationOnline(Channel channel, Map<String, Object> info) throws CircuitException {
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
            endPorts.put(endPort.getPerson(), personEndPorts);
        }
        personEndPorts.addEndPort(endPort);
        try {
            rabbitMQConsumer.bindEndPort(personEndPorts);
            if (!personEndPorts.isConsumed()) {
                rabbitMQConsumer.consumePersonQueue(personEndPorts);
            }
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
        return endPort;
    }

    @Override
    public void offline(EndPort theEndPort) throws CircuitException {
        if (theEndPort == null) {
            return;
        }
        if (persistenceMessageService == null) {
            noNotificationOffline(theEndPort);
            return;
        }
        doNotificationOffline(theEndPort);
    }

    private void doNotificationOffline(EndPort theEndPort) {
        //通知模式不解绑，仍然消费，但消息会被订阅到插件中存储,并同时发通知给客端
        String person = theEndPort.getPerson();
        String device = theEndPort.getDevice();
        PersonEndPorts personEndPorts = endPorts.get(person);
        if (personEndPorts == null) {
            return;
        }
        personEndPorts.removeEndPort(person, device);

        if (personEndPorts.isEmpty()) {
            endPorts.remove(person);
        }

        persistenceMessageService.removeBuddyDevice(theEndPort);
    }

    private void noNotificationOffline(EndPort theEndPort) throws CircuitException {
        String person = theEndPort.getPerson();
        String device = theEndPort.getDevice();
        PersonEndPorts personEndPorts = endPorts.get(person);
        if (personEndPorts == null) {
            return;
        }
        try {
            rabbitMQConsumer.unbindEndPort(theEndPort);
            personEndPorts.removeEndPort(person, device);
        } catch (IOException e) {
            throw new CircuitException("500", e);
        }
        if (personEndPorts.isEmpty()) {
            rabbitMQConsumer.stopConsumePersonQueue(personEndPorts);
        }
    }
}

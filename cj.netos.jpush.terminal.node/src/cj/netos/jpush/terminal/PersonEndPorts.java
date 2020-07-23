package cj.netos.jpush.terminal;

import cj.netos.jpush.EndPort;

import java.util.ArrayList;
import java.util.List;

public class PersonEndPorts {
    private String person;
    private String nickName;
    private String ConsumerTag;
    private List<EndPort> endPorts;
    private boolean isConsumed;
    public PersonEndPorts() {
        endPorts = new ArrayList<>();
    }

    public String getConsumerTag() {
        return ConsumerTag;
    }

    public void setConsumerTag(String consumerTag) {
        ConsumerTag = consumerTag;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isEmpty() {
        return endPorts.isEmpty();
    }

    public int count() {
        return endPorts.size();
    }

    public EndPort[] endPorts() {
        return endPorts.toArray(new EndPort[0]);
    }

    public void addEndPort(EndPort endPort) {
        endPorts.add(endPort);
    }

    public void removeEndPort(String person, String device) {
        EndPort[] copy = endPorts.toArray(new EndPort[0]);
        for (int i = 0; i < copy.length; i++) {
            EndPort endPort = copy[i];
            if (endPort == null) {
                continue;
            }
            if (person.equals(endPort.getPerson()) && device.equals(endPort.getDevice())) {
                copy[i] = null;
            }
        }
        endPorts.clear();
        for (EndPort port : copy) {
            if (port == null) {
                continue;
            }
            endPorts.add(port);
        }
    }

    public boolean isConsumed() {
        return isConsumed;
    }

    public void setConsumed(boolean consumed) {
        isConsumed = consumed;
    }
}

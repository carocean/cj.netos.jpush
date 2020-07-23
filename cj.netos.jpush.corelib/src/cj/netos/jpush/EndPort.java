package cj.netos.jpush;

import io.netty.channel.Channel;

import java.util.List;

public class EndPort {
    String person;
    String device;
    String nickName;
    ChannelWriter writer;
    Channel channel;
    List<String> roles;

    public EndPort(Channel channel) {
        this.writer = new ChannelWriter();
        this.channel = channel;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean roleIn(String role) {
        return roles.contains(role);
    }

    public void writeFrame(JPushFrame frame) {
        writer.writeChannel(channel, frame);
    }
}

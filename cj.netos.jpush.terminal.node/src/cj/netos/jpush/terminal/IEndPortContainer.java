package cj.netos.jpush.terminal;

import cj.netos.jpush.EndPort;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

import java.util.Map;

public interface IEndPortContainer {
    /**
     * {expireTime=7200000.0, person=cj@la.netos, nickName=大地经济, pubTime=1.595424721408E12, roles=[app:users@la.netos], portal=nodepower, isExpired=false, device=b1c5321a7b40f1b7582e1d36fc04db48}
     * @param info
     * @return
     */
    EndPort online(Channel channel, Map<String, Object> info) throws CircuitException;


    void offline(EndPort endPort) throws CircuitException;

}

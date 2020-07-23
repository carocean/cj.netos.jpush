package cj.netos.jpush.terminal;

import io.netty.channel.ChannelFuture;

public interface ITerminalNodeServer {
    void stop();

    ChannelFuture start();
}

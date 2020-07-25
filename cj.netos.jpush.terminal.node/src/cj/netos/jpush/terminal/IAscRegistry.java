package cj.netos.jpush.terminal;

public interface IAscRegistry {
    void start(INodeConfig nodeConfig) throws Exception;

    void stop() throws Exception;
}

package cj.netos.jpush.terminal;

import java.io.FileNotFoundException;

public interface ITerminalNode {
    void entrypoint(String home) throws FileNotFoundException;
}

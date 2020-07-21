package cj.netos.jpush.device.console;



import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DeviceMonitor extends BaseMonitor {

    @Override
    protected boolean isExit(String text) {
        return "bye".equals(text) || "exit".equals(text);
    }

    @Override
    protected boolean checkExitOnAfterCommand(String text) {
        return false;
    }

    @Override
    protected Scanner getScanner() {
        return new Scanner(System.in);
    }

    @Override
    protected String getPrefix() {
        return ">";
    }

    @Override
    protected Map<String, Command> getCommands() {
        Map<String, Command> cmds = new HashMap<>();

        return cmds;
    }
}

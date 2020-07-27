package cj.netos.jpush.device.console;


import cj.netos.jpush.device.console.cmd.*;

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
        Command login = new LoginCommand();
        cmds.put(login.cmd(), login);
        Command ls = new LsCommand();
        cmds.put(ls.cmd(), ls);
        Command pause = new PauseCommand();
        cmds.put(pause.cmd(), pause);
        Command resume = new ResumeCommand();
        cmds.put(resume.cmd(), resume);
        Command admin = new AdminCommand();
        cmds.put(admin.cmd(), admin);
        Command logout = new LogoutCommand();
        cmds.put(logout.cmd(), logout);
        return cmds;
    }
}

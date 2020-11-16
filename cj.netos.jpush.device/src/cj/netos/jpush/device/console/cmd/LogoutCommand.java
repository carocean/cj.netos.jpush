package cj.netos.jpush.device.console.cmd;

import cj.netos.jpush.device.IDevice;
import cj.netos.jpush.device.console.CmdLine;
import cj.netos.jpush.device.console.Command;
import org.apache.commons.cli.Options;

import java.io.IOException;

public class LogoutCommand extends Command {
    @Override
    public String cmd() {
        return "logout";
    }

    @Override
    public String cmdDesc() {
        return "登出。如果服务器启用了通知插件，虽然用户所有终结点都登出，但通知插件仍在消费";
    }

    @Override
    public Options options() {
        Options options = new Options();
//        Option t = new Option("t", "token", true, "[必须]访问令牌");
//        options.addOption(t);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        IDevice device = cl.device();
        device.logout();
        return true;
    }
}

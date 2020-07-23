package cj.netos.jpush.device.console.cmd;

import cj.netos.jpush.device.IDevice;
import cj.netos.jpush.device.console.CmdLine;
import cj.netos.jpush.device.console.Command;
import org.apache.commons.cli.Options;

import java.io.IOException;

public class ResumeCommand extends Command {
    @Override
    public String cmd() {
        return "resume";
    }

    @Override
    public String cmdDesc() {
        return "继续接收消息";
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
        device.resume();
        return true;
    }
}

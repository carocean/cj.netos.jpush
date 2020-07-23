package cj.netos.jpush.device.console.cmd;

import cj.netos.jpush.device.IDevice;
import cj.netos.jpush.device.console.CmdLine;
import cj.netos.jpush.device.console.Command;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.List;

public class AdminCommand extends Command {
    @Override
    public String cmd() {
        return "admin";
    }

    @Override
    public String cmdDesc() {
        return "管理员权限，用法:admin subcommand。subcommand有：ls,view 例：admin ls 。 admin view -u cj@gbera.netos";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option u = new Option("u", "user", true, "[必须]该参数仅在使用view命令时有效。");
        options.addOption(u);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        CommandLine line = cl.line();
        List<String> args = line.getArgList();
        if (args.isEmpty()) {
            System.out.println(String.format("错误：未指定命令，格式：admin ls 或者admin view"));
            return true;
        }
        String name = args.get(0);
        IDevice device = cl.device();
        switch (name) {
            case "ls":
                device.adminLs();
                break;
            case "view":
                if (!line.hasOption("u")) {
                    System.out.println("缺少参数-u");
                    return true;
                }
                device.adminView(line.getOptionValue("u"));
                break;
            default:
                System.out.println("不支持的子命令:" + name);
                break;
        }
        return true;
    }
}

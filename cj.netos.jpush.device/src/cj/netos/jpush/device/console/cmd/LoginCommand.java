package cj.netos.jpush.device.console.cmd;

import cj.netos.jpush.device.IDevice;
import cj.netos.jpush.device.console.CmdLine;
import cj.netos.jpush.device.console.Command;
import cj.ultimate.util.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.List;

public class LoginCommand extends Command {
    @Override
    public String cmd() {
        return "login";
    }

    @Override
    public String cmdDesc() {
        return "登录终端服务器，语法：login -t xxxx 例：login -t DFBE52B0F142F055BB7A06838F2B7F44";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option t = new Option("t", "token", true, "[必须]访问令牌");
        options.addOption(t);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        IDevice device = cl.device();
        CommandLine line = cl.line();
//        List<String> args = line.getArgList();
//        if (args.isEmpty()) {
//            System.out.println(String.format("错误：未指定网络名"));
//            return true;
//        }
//        String name = args.get(0);
        String token = line.getOptionValue("t");
        if (StringUtil.isEmpty(token)) {
            System.out.println("缺少参数-t");
            return true;
        }
        device.login(token);
        return true;
    }
}

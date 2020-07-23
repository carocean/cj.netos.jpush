package cj.netos.jpush.device.console;

import cj.netos.jpush.JPushFrame;
import cj.netos.jpush.device.*;
import cj.netos.jpush.device.IOnmessage;
import cj.studio.ecm.net.CircuitException;
import org.apache.commons.cli.*;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class DeviceEntrypoint {

    public static void main(String... args) throws ParseException, IOException {
        String fileName = "cj.netos.jpush.device";
        Options options = new Options();

        Option r = new Option("u", "url", true, "[必须]远程node的url地址，格式：'protocol://host:port?workThreadCount=2&prop2=yy'。注意：如果含有&符则必须加单引号将整个url包住");
        options.addOption(r);
        Option m = new Option("m", "man", false, "帮助");
        options.addOption(m);
        Option debug = new Option("d", "debug", true, "调试命令行程序集时使用，需指定以下jar包所在目录\r\n" + fileName);
        options.addOption(debug);

        CommandLine line = new DefaultParser().parse(options, args);

        if (line.hasOption("m")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("device", options);
            return;
        }
        if (!line.hasOption("u")) {
            System.out.println("缺少必须参数-u，请使用-m参数查看帮助");
            return;
        }
        String url = line.getOptionValue("u");

        File consoleFile = getHomeDir(fileName, line);
        PropertyConfigurator.configure(String.format("%s%sconf%slog4j.properties", consoleFile.getParent(), File.separator, File.separator));
        IDevice device = Device.connect(url, new IOnopen() {
            @Override
            public void onopen() {
                System.out.println("*********************************************");
                System.out.println("*************已打开。欢迎使用******************");
            }
        }, new IOnclose() {
            @Override
            public void onclose() {
                System.out.println("*************已关闭。色友那啦******************");
                System.exit(0);
            }

        }, new IOnerror() {

            @Override
            public void onerror(Throwable error) {
                System.out.println(String.format("--------------系统异常响应--------------------"));
                System.out.println(error);
                System.out.println("--------------end");
            }

        }, new IOnevent() {
            @Override
            public void onevent(JPushFrame frame) throws CircuitException {
                System.out.println(String.format("--------------系统消息--------------------"));
                System.out.println(new String(frame.toBytes()));
                System.out.println("--------------end");
            }
        }, new IOnmessage() {
            @Override
            public void onmessage(JPushFrame frame) throws CircuitException {
                System.out.println(String.format("--------------用户消息--------------------"));
                System.out.println(new String(frame.toBytes()));
                System.out.println("--------------end");
            }
        });
        IMonitor console = new DeviceMonitor();
        try {
            console.moniter(device);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static File getHomeDir(String fileName, CommandLine line) throws IOException {
        String usr = System.getProperty("user.dir");
        File f = new File(usr);
        File[] arr = f.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.startsWith(fileName)) {
                    return true;
                }
                return false;
            }
        });
        if (arr.length < 1 && !line.hasOption("debug")) {
            throw new IOException(fileName + " 程序集不存在.");
        }
        if (line.hasOption("debug")) {
            File[] da = new File(line.getOptionValue("debug")).listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    if (name.startsWith(fileName)) {
                        return true;
                    }
                    return false;
                }
            });
            if (da.length < 0)
                throw new IOException("调试时不存在指定的必要jar包" + fileName);
            f = da[0];
        } else {
            f = arr[0];
        }
        return f;
    }


}

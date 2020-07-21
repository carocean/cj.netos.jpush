package cj.netos.jpush.device.console;

import cj.netos.jpush.device.IDevice;
import cj.netos.jpush.device.ILogicNetwork;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public interface IMonitor {
    void moniter(IDevice peer, ILogicNetwork network) throws ParseException, IOException;

}

package cj.netos.jpush.device.console;

import cj.netos.jpush.device.IDevice;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public interface IMonitor {
    void moniter(IDevice device) throws ParseException, IOException;

}

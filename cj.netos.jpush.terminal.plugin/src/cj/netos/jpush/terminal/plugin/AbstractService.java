package cj.netos.jpush.terminal.plugin;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;

public class AbstractService {
    public static transient final String _COL_NAME_MESSAGE_UNREDS = "jpush.plugin.unreads";
    public static transient final String _COL_NAME_MESSAGE_TOTAL = "jpush.plugin.total";
    public static transient final String _COL_NAME_MESSAGE_DEVICE = "jpush.plugin.devices";
    @CjServiceRef(refByName = "mongodb.netos.home")
    ICube home;

    protected ICube home() throws CircuitException {
        return home;
    }
}

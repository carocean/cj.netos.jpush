package cj.netos.jpush.terminal.plugin;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.studio.ecm.IEntryPointActivator;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.context.IElement;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

public class TerminalPluginEntryPointActivator implements IEntryPointActivator {
    @Override
    public void activate(IServiceSite site, IElement args) {
        ICube home = (ICube) site.getService("mongodb.netos.home");
        checkUnreadIndex(home);
        checkTotalIndex(home);
        checkDeviceIndex(home);
    }

    private void checkDeviceIndex(ICube home) {
        ListIndexesIterable<Document> list = home.listIndexes(AbstractService._COL_NAME_MESSAGE_DEVICE);
        boolean existsIndex = false;
        for (Document document : list) {
            String name = (String) document.get("name");
            if (name.indexOf("tuple.person") > -1 || name.indexOf("tuple.device") > -1) {
                existsIndex = true;
                break;
            }
        }
        if (!existsIndex) {
            home.createIndex(AbstractService._COL_NAME_MESSAGE_DEVICE, Document.parse(String.format("{'tuple.person':1,'tuple.device':1}")));
        }
    }

    private void checkTotalIndex(ICube home) {
        ListIndexesIterable<Document> list = home.listIndexes(AbstractService._COL_NAME_MESSAGE_TOTAL);
        boolean existsIndex = false;
        for (Document document : list) {
            String name = (String) document.get("name");
            if (name.indexOf("tuple.person") > -1) {
                existsIndex = true;
                break;
            }
        }
        if (!existsIndex) {
            home.createIndex(AbstractService._COL_NAME_MESSAGE_TOTAL, Document.parse(String.format("{'tuple.person':1}")));
        }
    }

    private void checkUnreadIndex(ICube home) {
        ListIndexesIterable<Document> list = home.listIndexes(AbstractService._COL_NAME_MESSAGE_UNREDS);
        boolean existsIndex = false;
        for (Document document : list) {
            String name = (String) document.get("name");
            if (name.indexOf("tuple.person") > -1 || name.indexOf("tuple.ctime") > -1) {
                existsIndex = true;
                break;
            }
        }
        if (!existsIndex) {
            home.createIndex(AbstractService._COL_NAME_MESSAGE_UNREDS, Document.parse(String.format("{'tuple.ctime':1,'tuple.person':1}")));
        }
    }

    @Override
    public void inactivate(IServiceSite site) {

    }
}

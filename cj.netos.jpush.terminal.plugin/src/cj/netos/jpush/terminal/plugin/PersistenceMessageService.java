package cj.netos.jpush.terminal.plugin;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.jpush.EndPort;
import cj.netos.jpush.IPersistenceMessageService;
import cj.netos.jpush.JPushFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjExotericalType;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceInvertInjection;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CjExotericalType(typeName = "persistences")
@CjService(name = "persistenceMessageService", isExoteric = true)
public class PersistenceMessageService extends AbstractService implements IPersistenceMessageService {
    @CjServiceRef
    IBuddyPusherFactory buddyPusherFactory;

    @CjServiceRef
    IAbsorbNotifyWriter absorbNotifyWriter;

    @Override
    public void writeFrame(JPushFrame frame, String person, String nickName) throws CircuitException {
        PersistenceMessage message = new PersistenceMessage();
        message.setCtime(System.currentTimeMillis());
        message.setPerson(person);
        message.setNickName(nickName);
        JPushFrame copy = frame.copy();
        message.setData(new String(copy.toBytes()));
        copy.dispose();
        home.saveDoc(_COL_NAME_MESSAGE_UNREDS, new TupleDocument<>(message));
        totalAdd(person);

        List<String> devices = getBuddyDeviceOfPerson(person);
        for (String device : devices) {
            copy = frame.copy();
            copy.head("to-nick", nickName);
            String senderNick = absorbNotifyWriter.getSenderNick(copy.head("sender-person"));
            if (!StringUtil.isEmpty(senderNick)) {
                copy.head("sender-nick", senderNick);
            }
            buddyPusherFactory.push(copy, device);
            copy.dispose();
        }
//
    }


    private List<String> getBuddyDeviceOfPerson(String person) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.person':'%s'}",
                _COL_NAME_MESSAGE_DEVICE,
                HashMap.class.getName(),
                person);
        IQuery<Map<String, Object>> query = home.createQuery(cjql);
        List<IDocument<Map<String, Object>>> list = query.getResultList();
        List<String> devices = new ArrayList<>();
        for (IDocument<Map<String, Object>> document : list) {
            devices.add((String) document.tuple().get("device"));
        }
        return devices;
    }

    @Override
    public void checkAndUpdateBuddyDevice(EndPort endPort) {
        if (endPort.getDevice().indexOf("://") < 0) {
            return;
        }
        boolean exists = home.tupleCount(_COL_NAME_MESSAGE_DEVICE, String.format("{'tuple.person':'%s','tuple.device':'%s'}", endPort.getPerson(), endPort.getDevice())) > 0;
        if (exists) {
            return;
        }
        //保存
//        home.deleteDocs(_COL_NAME_MESSAGE_DEVICE,String.format("{'tuple.person':'%s'}",endPort.getPerson()));
        Map<String, String> map = new HashMap<>();
        map.put("person", endPort.getPerson());
        map.put("device", endPort.getDevice());
        home.saveDoc(_COL_NAME_MESSAGE_DEVICE, new TupleDocument<>(map));
    }

    private synchronized void totalAdd(String person) {
        boolean exists = home.tupleCount(_COL_NAME_MESSAGE_TOTAL, String.format("{'tuple.person':'%s'}", person)) > 0;
        if (exists) {
            long count = 0;
            String cjql = String.format("select {'tuple':'*'}.limit(1) from tuple %s %s where {'tuple.person':'%s'}",
                    _COL_NAME_MESSAGE_TOTAL, PersonUnreadMessageTotal.class.getName(), person
            );
            IQuery<PersonUnreadMessageTotal> query = home.createQuery(cjql);
            IDocument<PersonUnreadMessageTotal> document = query.getSingleResult();
            count = document.tuple().count + 1;

            home.updateDocOne(_COL_NAME_MESSAGE_TOTAL, Document.parse(String.format("{'tuple.person':'%s'}", person)),
                    Document.parse(String.format("{'$set':{'tuple.count':%s}}", count)));
            return;
        }
        PersonUnreadMessageTotal total = new PersonUnreadMessageTotal();
        total.setCount(1);
        total.setPerson(person);
        home.saveDoc(_COL_NAME_MESSAGE_TOTAL, new TupleDocument<>(total));
    }

    @Override
    public void downstream(EndPort endPort) throws CircuitException {
        int limit = 100;
        long offset = 0;
        while (true) {
            List<PersistenceMessage> messages = pagePersistenceMessage(endPort.getPerson(), limit, offset);
            if (messages.isEmpty()) {
                break;
            }
            offset += messages.size();
            for (PersistenceMessage message : messages) {
                String data = message.data;
                JPushFrame frame = new JPushFrame(data.getBytes());
                endPort.writeFrame(frame);
            }
        }
        home.deleteDocs(_COL_NAME_MESSAGE_UNREDS, String.format("{'tuple.person':'%s'}", endPort.getPerson()));
        home.deleteDocs(_COL_NAME_MESSAGE_TOTAL, String.format("{'tuple.person':'%s'}", endPort.getPerson()));
    }

    private List<PersistenceMessage> pagePersistenceMessage(String person, int limit, long offset) {
        String cjql = String.format("select {'tuple':'*'}.limit(%s).skip(%s) from tuple %s %s where {'tuple.person':'%s'}",
                limit, offset, _COL_NAME_MESSAGE_UNREDS, PersistenceMessage.class.getName(), person
        );
        IQuery<PersistenceMessage> query = home.createQuery(cjql);
        List<IDocument<PersistenceMessage>> list = query.getResultList();
        List<PersistenceMessage> messages = new ArrayList<>();
        for (IDocument<PersistenceMessage> document : list) {
            messages.add(document.tuple());
        }
        return messages;
    }
}

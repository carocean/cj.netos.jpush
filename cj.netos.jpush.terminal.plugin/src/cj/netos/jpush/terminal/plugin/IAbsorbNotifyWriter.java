package cj.netos.jpush.terminal.plugin;

import cj.studio.ecm.net.CircuitException;

import java.math.BigDecimal;

public interface IAbsorbNotifyWriter {
    void addAmount(String receiverPerson, String receiverNick, String device, BigDecimal amount);

    String getSenderNick(String person) throws CircuitException;

    void checkAndSendAbsorbNotify(String receiverPerson, String receiverNick);

}

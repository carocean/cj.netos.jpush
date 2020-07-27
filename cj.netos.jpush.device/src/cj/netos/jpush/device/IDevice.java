package cj.netos.jpush.device;

public interface IDevice {

    void close();

    void login( String token);

    void lsInfo();

    void resume();

    void pause();

    void adminLs();

    void adminView(String u);

    void logout();

}

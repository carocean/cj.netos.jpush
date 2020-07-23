package cj.netos.jpush.device;

public interface IDevice {

    void close();

    void login( String token);

}

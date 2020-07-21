package cj.netos.jpush.terminal;
//代表一个用户在一个设备上上线，同一用户在多个设备上上线会有多个终结点，用户可以为空，但设备必须非空
public interface IEndPort {
    EndPortInfo getInfo();
}

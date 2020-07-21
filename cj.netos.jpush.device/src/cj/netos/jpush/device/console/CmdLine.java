package cj.netos.jpush.device.console;

import cj.netos.jpush.device.IDevice;
import cj.netos.jpush.device.ILogicNetwork;
import org.apache.commons.cli.CommandLine;

import java.util.HashMap;
import java.util.Map;


public final class CmdLine {
	private final ILogicNetwork network;
	IDevice device;
	String cmd;
	CommandLine line;
	Map<String,Object> props;
	public CmdLine(String cmd, CommandLine line, IDevice device, ILogicNetwork network) {
		this.cmd=cmd;
		this.line=line;
		props=new HashMap<>();
		this.device = device;
		this.network=network;
	}

	public ILogicNetwork network() {
		return network;
	}

	public IDevice device() {
		return device;
	}

	public String cmd() {
		return cmd;
	}
	public CommandLine line() {
		return line;
	}
	public String propString(String key){
		return (String)props.get(key);
	}
	public Object prop(String key){
		return props.get(key);
	}
	public void prop(String key,Object v){
		props.put(key, v);
	}
	public void copyPropsFrom(CmdLine cl) {
		props.putAll(cl.props);
	}
}

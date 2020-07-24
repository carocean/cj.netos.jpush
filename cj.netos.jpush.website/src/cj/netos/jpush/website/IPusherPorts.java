package cj.netos.jpush.website;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.PKeyInRequest;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.util.Map;

@CjOpenports(usage = "推送器")
public interface IPusherPorts extends IOpenportService {
    @CjOpenport(usage = "推送给person，其所有在线设备", command = "post")
    void pushToPerson(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "目标person", name = "toPerson") String toPerson,
            @CjOpenportParameter(usage = "请求行", name = "headline") String headline,
            @CjOpenportParameter(usage = "消息头", name = "headers", in = PKeyInRequest.content) Map<String, String> headers,
            @CjOpenportParameter(usage = "消息参数", name = "parameters", in = PKeyInRequest.content) Map<String, String> parameters,
            @CjOpenportParameter(usage = "内容", name = "content", in = PKeyInRequest.content) String content
    ) throws CircuitException;

    @CjOpenport(usage = "推送给person的指定的在线设备", command = "post")
    void pushToDevice(
            ISecuritySession securitySession,
            @CjOpenportParameter(usage = "目标person", name = "toPerson") String toPerson,
            @CjOpenportParameter(usage = "目标device,该device必须是person的在线设备", name = "toDevice") String toDevice,
            @CjOpenportParameter(usage = "请求行", name = "headline") String headline,
            @CjOpenportParameter(usage = "消息头", name = "headers", in = PKeyInRequest.content) Map<String, String> headers,
            @CjOpenportParameter(usage = "消息参数", name = "parameters", in = PKeyInRequest.content) Map<String, String> parameters,
            @CjOpenportParameter(usage = "内容", name = "content", in = PKeyInRequest.content) String content
    ) throws CircuitException;
}

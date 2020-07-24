package cj.netos.jpush.website;


import cj.studio.ecm.net.CircuitException;

import java.util.Map;

public interface IUcService {
    Map<String, Object> checkAccessToken(String accessToken) throws CircuitException;
}

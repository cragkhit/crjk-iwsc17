package org.apache.tomcat.websocket.server;
import javax.websocket.Extension;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.websocket.server.ServerEndpointConfig;
class WsPerSessionServerEndpointConfig implements ServerEndpointConfig {
    private final ServerEndpointConfig perEndpointConfig;
    private final Map<String, Object> perSessionUserProperties;
    WsPerSessionServerEndpointConfig ( final ServerEndpointConfig perEndpointConfig ) {
        this.perSessionUserProperties = new ConcurrentHashMap<String, Object>();
        this.perEndpointConfig = perEndpointConfig;
        this.perSessionUserProperties.putAll ( perEndpointConfig.getUserProperties() );
    }
    public List<Class<? extends Encoder>> getEncoders() {
        return ( List<Class<? extends Encoder>> ) this.perEndpointConfig.getEncoders();
    }
    public List<Class<? extends Decoder>> getDecoders() {
        return ( List<Class<? extends Decoder>> ) this.perEndpointConfig.getDecoders();
    }
    public Map<String, Object> getUserProperties() {
        return this.perSessionUserProperties;
    }
    public Class<?> getEndpointClass() {
        return ( Class<?> ) this.perEndpointConfig.getEndpointClass();
    }
    public String getPath() {
        return this.perEndpointConfig.getPath();
    }
    public List<String> getSubprotocols() {
        return ( List<String> ) this.perEndpointConfig.getSubprotocols();
    }
    public List<Extension> getExtensions() {
        return ( List<Extension> ) this.perEndpointConfig.getExtensions();
    }
    public ServerEndpointConfig.Configurator getConfigurator() {
        return this.perEndpointConfig.getConfigurator();
    }
}

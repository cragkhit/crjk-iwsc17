package org.apache.tomcat.websocket.server;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;
class WsPerSessionServerEndpointConfig implements ServerEndpointConfig {
    private final ServerEndpointConfig perEndpointConfig;
    private final Map<String, Object> perSessionUserProperties =
        new ConcurrentHashMap<>();
    WsPerSessionServerEndpointConfig ( ServerEndpointConfig perEndpointConfig ) {
        this.perEndpointConfig = perEndpointConfig;
        perSessionUserProperties.putAll ( perEndpointConfig.getUserProperties() );
    }
    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return perEndpointConfig.getEncoders();
    }
    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return perEndpointConfig.getDecoders();
    }
    @Override
    public Map<String, Object> getUserProperties() {
        return perSessionUserProperties;
    }
    @Override
    public Class<?> getEndpointClass() {
        return perEndpointConfig.getEndpointClass();
    }
    @Override
    public String getPath() {
        return perEndpointConfig.getPath();
    }
    @Override
    public List<String> getSubprotocols() {
        return perEndpointConfig.getSubprotocols();
    }
    @Override
    public List<Extension> getExtensions() {
        return perEndpointConfig.getExtensions();
    }
    @Override
    public Configurator getConfigurator() {
        return perEndpointConfig.getConfigurator();
    }
}

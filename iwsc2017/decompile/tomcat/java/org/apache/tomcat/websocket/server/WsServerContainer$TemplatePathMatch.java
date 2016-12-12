package org.apache.tomcat.websocket.server;
import javax.websocket.server.ServerEndpointConfig;
private static class TemplatePathMatch {
    private final ServerEndpointConfig config;
    private final UriTemplate uriTemplate;
    public TemplatePathMatch ( final ServerEndpointConfig config, final UriTemplate uriTemplate ) {
        this.config = config;
        this.uriTemplate = uriTemplate;
    }
    public ServerEndpointConfig getConfig() {
        return this.config;
    }
    public UriTemplate getUriTemplate() {
        return this.uriTemplate;
    }
}

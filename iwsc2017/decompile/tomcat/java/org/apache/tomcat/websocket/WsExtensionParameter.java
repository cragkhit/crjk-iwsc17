package org.apache.tomcat.websocket;
import javax.websocket.Extension;
public class WsExtensionParameter implements Extension.Parameter {
    private final String name;
    private final String value;
    WsExtensionParameter ( final String name, final String value ) {
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return this.name;
    }
    public String getValue() {
        return this.value;
    }
}

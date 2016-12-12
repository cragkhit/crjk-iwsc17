package org.apache.tomcat.websocket;
import java.util.ArrayList;
import java.util.List;
import javax.websocket.Extension;
public class WsExtension implements Extension {
    private final String name;
    private final List<Extension.Parameter> parameters;
    WsExtension ( final String name ) {
        this.parameters = new ArrayList<Extension.Parameter>();
        this.name = name;
    }
    void addParameter ( final Extension.Parameter parameter ) {
        this.parameters.add ( parameter );
    }
    public String getName() {
        return this.name;
    }
    public List<Extension.Parameter> getParameters() {
        return this.parameters;
    }
}

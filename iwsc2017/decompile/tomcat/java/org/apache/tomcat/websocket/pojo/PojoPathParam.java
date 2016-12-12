package org.apache.tomcat.websocket.pojo;
public class PojoPathParam {
    private final Class<?> type;
    private final String name;
    public PojoPathParam ( final Class<?> type, final String name ) {
        this.type = type;
        this.name = name;
    }
    public Class<?> getType() {
        return this.type;
    }
    public String getName() {
        return this.name;
    }
}

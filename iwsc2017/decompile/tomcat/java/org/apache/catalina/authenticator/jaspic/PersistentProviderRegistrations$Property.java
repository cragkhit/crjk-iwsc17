package org.apache.catalina.authenticator.jaspic;
public static class Property {
    private String name;
    private String value;
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue ( final String value ) {
        this.value = value;
    }
}

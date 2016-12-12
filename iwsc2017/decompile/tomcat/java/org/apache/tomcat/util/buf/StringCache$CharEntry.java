package org.apache.tomcat.util.buf;
private static class CharEntry {
    private char[] name;
    private String value;
    private CharEntry() {
        this.name = null;
        this.value = null;
    }
    @Override
    public String toString() {
        return this.value;
    }
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj instanceof CharEntry && this.value.equals ( ( ( CharEntry ) obj ).value );
    }
}

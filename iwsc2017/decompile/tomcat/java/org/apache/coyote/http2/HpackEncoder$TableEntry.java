package org.apache.coyote.http2;
private static class TableEntry {
    private final String name;
    private final String value;
    private final int size;
    private int position;
    private TableEntry ( final String name, final String value, final int position ) {
        this.name = name;
        this.value = value;
        this.position = position;
        if ( value != null ) {
            this.size = 32 + name.length() + value.length();
        } else {
            this.size = -1;
        }
    }
    int getPosition() {
        return this.position;
    }
    int getSize() {
        return this.size;
    }
}

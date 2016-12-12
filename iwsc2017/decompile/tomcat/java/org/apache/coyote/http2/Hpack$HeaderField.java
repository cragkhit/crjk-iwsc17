package org.apache.coyote.http2;
static class HeaderField {
    final String name;
    final String value;
    final int size;
    HeaderField ( final String name, final String value ) {
        this.name = name;
        this.value = value;
        if ( value != null ) {
            this.size = 32 + name.length() + value.length();
        } else {
            this.size = -1;
        }
    }
}

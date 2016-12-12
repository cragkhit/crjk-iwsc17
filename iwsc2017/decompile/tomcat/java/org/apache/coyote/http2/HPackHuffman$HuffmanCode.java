package org.apache.coyote.http2;
protected static class HuffmanCode {
    int value;
    int length;
    public HuffmanCode ( final int value, final int length ) {
        this.value = value;
        this.length = length;
    }
    public int getValue() {
        return this.value;
    }
    public int getLength() {
        return this.length;
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || this.getClass() != o.getClass() ) {
            return false;
        }
        final HuffmanCode that = ( HuffmanCode ) o;
        return this.length == that.length && this.value == that.value;
    }
    @Override
    public int hashCode() {
        int result = this.value;
        result = 31 * result + this.length;
        return result;
    }
    @Override
    public String toString() {
        return "HuffmanCode{value=" + this.value + ", length=" + this.length + '}';
    }
}

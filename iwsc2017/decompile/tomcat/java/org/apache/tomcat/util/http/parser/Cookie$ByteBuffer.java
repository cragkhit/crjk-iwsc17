package org.apache.tomcat.util.http.parser;
private static class ByteBuffer {
    private final byte[] bytes;
    private int limit;
    private int position;
    public ByteBuffer ( final byte[] bytes, final int offset, final int len ) {
        this.position = 0;
        this.bytes = bytes;
        this.position = offset;
        this.limit = offset + len;
    }
    public int position() {
        return this.position;
    }
    public void position ( final int position ) {
        this.position = position;
    }
    public int limit() {
        return this.limit;
    }
    public int remaining() {
        return this.limit - this.position;
    }
    public boolean hasRemaining() {
        return this.position < this.limit;
    }
    public byte get() {
        return this.bytes[this.position++];
    }
    public byte peek() {
        return this.bytes[this.position];
    }
    public void rewind() {
        --this.position;
    }
    public byte[] array() {
        return this.bytes;
    }
    @Override
    public String toString() {
        return "position [" + this.position + "], limit [" + this.limit + "]";
    }
}

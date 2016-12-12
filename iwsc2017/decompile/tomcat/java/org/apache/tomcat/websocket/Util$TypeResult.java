package org.apache.tomcat.websocket;
private static class TypeResult {
    private final Class<?> clazz;
    private final int index;
    private int dimension;
    public TypeResult ( final Class<?> clazz, final int index, final int dimension ) {
        this.clazz = clazz;
        this.index = index;
        this.dimension = dimension;
    }
    public Class<?> getClazz() {
        return this.clazz;
    }
    public int getIndex() {
        return this.index;
    }
    public int getDimension() {
        return this.dimension;
    }
    public void incrementDimension ( final int inc ) {
        this.dimension += inc;
    }
}

package org.apache.catalina.mapper;
protected abstract static class MapElement<T> {
    public final String name;
    public final T object;
    public MapElement ( final String name, final T object ) {
        this.name = name;
        this.object = object;
    }
}

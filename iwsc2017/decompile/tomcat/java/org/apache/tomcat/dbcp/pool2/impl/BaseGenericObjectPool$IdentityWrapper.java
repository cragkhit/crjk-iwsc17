package org.apache.tomcat.dbcp.pool2.impl;
static class IdentityWrapper<T> {
    private final T instance;
    public IdentityWrapper ( final T instance ) {
        this.instance = instance;
    }
    @Override
    public int hashCode() {
        return System.identityHashCode ( this.instance );
    }
    @Override
    public boolean equals ( final Object other ) {
        return other instanceof IdentityWrapper && ( ( IdentityWrapper ) other ).instance == this.instance;
    }
    public T getObject() {
        return this.instance;
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "IdentityWrapper [instance=" );
        builder.append ( this.instance );
        builder.append ( "]" );
        return builder.toString();
    }
}

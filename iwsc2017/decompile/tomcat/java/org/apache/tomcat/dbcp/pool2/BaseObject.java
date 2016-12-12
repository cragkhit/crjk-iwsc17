package org.apache.tomcat.dbcp.pool2;
public abstract class BaseObject {
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( this.getClass().getSimpleName() );
        builder.append ( " [" );
        this.toStringAppendFields ( builder );
        builder.append ( "]" );
        return builder.toString();
    }
    protected void toStringAppendFields ( final StringBuilder builder ) {
    }
}

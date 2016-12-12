package org.apache.tomcat.dbcp.pool2;
public abstract class BaseObject {
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( getClass().getSimpleName() );
        builder.append ( " [" );
        toStringAppendFields ( builder );
        builder.append ( "]" );
        return builder.toString();
    }
    protected void toStringAppendFields ( final StringBuilder builder ) {
    }
}

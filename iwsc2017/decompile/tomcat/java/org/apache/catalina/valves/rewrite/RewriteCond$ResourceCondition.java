package org.apache.catalina.valves.rewrite;
public static class ResourceCondition extends Condition {
    public int type;
    public ResourceCondition() {
        this.type = 0;
    }
    @Override
    public boolean evaluate ( final String value, final Resolver resolver ) {
        return resolver.resolveResource ( this.type, value );
    }
}

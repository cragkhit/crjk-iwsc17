package org.apache.catalina.realm;
protected static class AllRolesMode {
    private final String name;
    public static final AllRolesMode STRICT_MODE;
    public static final AllRolesMode AUTH_ONLY_MODE;
    public static final AllRolesMode STRICT_AUTH_ONLY_MODE;
    static AllRolesMode toMode ( final String name ) {
        AllRolesMode mode;
        if ( name.equalsIgnoreCase ( AllRolesMode.STRICT_MODE.name ) ) {
            mode = AllRolesMode.STRICT_MODE;
        } else if ( name.equalsIgnoreCase ( AllRolesMode.AUTH_ONLY_MODE.name ) ) {
            mode = AllRolesMode.AUTH_ONLY_MODE;
        } else {
            if ( !name.equalsIgnoreCase ( AllRolesMode.STRICT_AUTH_ONLY_MODE.name ) ) {
                throw new IllegalStateException ( "Unknown mode, must be one of: strict, authOnly, strictAuthOnly" );
            }
            mode = AllRolesMode.STRICT_AUTH_ONLY_MODE;
        }
        return mode;
    }
    private AllRolesMode ( final String name ) {
        this.name = name;
    }
    @Override
    public boolean equals ( final Object o ) {
        boolean equals = false;
        if ( o instanceof AllRolesMode ) {
            final AllRolesMode mode = ( AllRolesMode ) o;
            equals = this.name.equals ( mode.name );
        }
        return equals;
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    @Override
    public String toString() {
        return this.name;
    }
    static {
        STRICT_MODE = new AllRolesMode ( "strict" );
        AUTH_ONLY_MODE = new AllRolesMode ( "authOnly" );
        STRICT_AUTH_ONLY_MODE = new AllRolesMode ( "strictAuthOnly" );
    }
}

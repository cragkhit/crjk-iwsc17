package org.apache.catalina.valves.rewrite;
public abstract class Resolver {
    public abstract String resolve ( String key );
    public String resolveEnv ( String key ) {
        return System.getProperty ( key );
    }
    public abstract String resolveSsl ( String key );
    public abstract String resolveHttp ( String key );
    public abstract boolean resolveResource ( int type, String name );
    public abstract String getUriEncoding();
}

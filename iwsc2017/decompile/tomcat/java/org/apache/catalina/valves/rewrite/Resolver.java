package org.apache.catalina.valves.rewrite;
public abstract class Resolver {
    public abstract String resolve ( final String p0 );
    public String resolveEnv ( final String key ) {
        return System.getProperty ( key );
    }
    public abstract String resolveSsl ( final String p0 );
    public abstract String resolveHttp ( final String p0 );
    public abstract boolean resolveResource ( final int p0, final String p1 );
    public abstract String getUriEncoding();
}

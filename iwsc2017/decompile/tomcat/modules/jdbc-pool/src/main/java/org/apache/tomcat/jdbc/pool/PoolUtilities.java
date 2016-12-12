// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.Map;
import java.util.Properties;

public class PoolUtilities
{
    public static final String PROP_USER = "user";
    public static final String PROP_PASSWORD = "password";
    
    public static Properties clone(final Properties p) {
        final Properties c = new Properties();
        c.putAll(p);
        return c;
    }
    
    public static Properties cloneWithoutPassword(final Properties p) {
        final Properties result = clone(p);
        result.remove("password");
        return result;
    }
}

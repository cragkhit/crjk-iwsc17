// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;

public class ClassLoaderUtil
{
    private static final Log log;
    private static final boolean onlyAttemptFirstLoader;
    
    public static Class<?> loadClass(final String className, final ClassLoader... classLoaders) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        StringBuilder errorMsg = null;
        final int length = classLoaders.length;
        int i = 0;
        while (i < length) {
            final ClassLoader cl = classLoaders[i];
            try {
                if (cl != null) {
                    if (ClassLoaderUtil.log.isDebugEnabled()) {
                        ClassLoaderUtil.log.debug((Object)("Attempting to load class[" + className + "] from " + cl));
                    }
                    return Class.forName(className, true, cl);
                }
                throw new ClassNotFoundException("Classloader is null");
            }
            catch (ClassNotFoundException x) {
                last = x;
                if (errorMsg == null) {
                    errorMsg = new StringBuilder();
                }
                else {
                    errorMsg.append(';');
                }
                errorMsg.append("ClassLoader:");
                errorMsg.append(cl);
                if (!ClassLoaderUtil.onlyAttemptFirstLoader) {
                    ++i;
                    continue;
                }
            }
            break;
        }
        throw new ClassNotFoundException("Unable to load class: " + className + " from " + (Object)errorMsg, last);
    }
    
    static {
        log = LogFactory.getLog((Class)ClassLoaderUtil.class);
        onlyAttemptFirstLoader = Boolean.parseBoolean(System.getProperty("org.apache.tomcat.jdbc.pool.onlyAttemptCurrentClassLoader", "false"));
    }
}

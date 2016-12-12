// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.naming;

import org.apache.juli.logging.LogFactory;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Enumeration;
import javax.naming.RefAddr;
import org.apache.tomcat.jdbc.pool.ClassLoaderUtil;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import org.apache.juli.logging.Log;
import javax.naming.spi.ObjectFactory;

public class GenericNamingResourcesFactory implements ObjectFactory
{
    private static final Log log;
    
    @Override
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment) throws Exception {
        if (obj == null || !(obj instanceof Reference)) {
            return null;
        }
        final Reference ref = (Reference)obj;
        final Enumeration<RefAddr> refs = ref.getAll();
        final String type = ref.getClassName();
        final Object o = ClassLoaderUtil.loadClass(type, GenericNamingResourcesFactory.class.getClassLoader(), Thread.currentThread().getContextClassLoader()).newInstance();
        while (refs.hasMoreElements()) {
            final RefAddr addr = refs.nextElement();
            final String param = addr.getType();
            String value = null;
            if (addr.getContent() != null) {
                value = addr.getContent().toString();
            }
            if (setProperty(o, param, value)) {
                continue;
            }
            GenericNamingResourcesFactory.log.debug((Object)("Property not configured[" + param + "]. No setter found on[" + o + "]."));
        }
        return o;
    }
    
    private static boolean setProperty(final Object o, final String name, final String value) {
        if (GenericNamingResourcesFactory.log.isDebugEnabled()) {
            GenericNamingResourcesFactory.log.debug((Object)("IntrospectionUtils: setProperty(" + o.getClass() + " " + name + "=" + value + ")"));
        }
        final String setter = "set" + capitalize(name);
        try {
            final Method[] methods = o.getClass().getMethods();
            Method setPropertyMethodVoid = null;
            Method setPropertyMethodBool = null;
            for (int i = 0; i < methods.length; ++i) {
                final Class<?>[] paramT = methods[i].getParameterTypes();
                if (setter.equals(methods[i].getName()) && paramT.length == 1 && "java.lang.String".equals(paramT[0].getName())) {
                    methods[i].invoke(o, value);
                    return true;
                }
            }
            for (int i = 0; i < methods.length; ++i) {
                boolean ok = true;
                if (setter.equals(methods[i].getName()) && methods[i].getParameterTypes().length == 1) {
                    final Class<?> paramType = methods[i].getParameterTypes()[0];
                    final Object[] params = { null };
                    Label_0493: {
                        if (!"java.lang.Integer".equals(paramType.getName())) {
                            if (!"int".equals(paramType.getName())) {
                                if (!"java.lang.Long".equals(paramType.getName())) {
                                    if (!"long".equals(paramType.getName())) {
                                        if ("java.lang.Boolean".equals(paramType.getName()) || "boolean".equals(paramType.getName())) {
                                            params[0] = Boolean.valueOf(value);
                                            break Label_0493;
                                        }
                                        if ("java.net.InetAddress".equals(paramType.getName())) {
                                            try {
                                                params[0] = InetAddress.getByName(value);
                                            }
                                            catch (UnknownHostException exc) {
                                                if (GenericNamingResourcesFactory.log.isDebugEnabled()) {
                                                    GenericNamingResourcesFactory.log.debug((Object)("IntrospectionUtils: Unable to resolve host name:" + value));
                                                }
                                                ok = false;
                                            }
                                            break Label_0493;
                                        }
                                        if (GenericNamingResourcesFactory.log.isDebugEnabled()) {
                                            GenericNamingResourcesFactory.log.debug((Object)("IntrospectionUtils: Unknown type " + paramType.getName()));
                                        }
                                        break Label_0493;
                                    }
                                }
                                try {
                                    params[0] = new Long(value);
                                }
                                catch (NumberFormatException ex3) {
                                    ok = false;
                                }
                                break Label_0493;
                            }
                        }
                        try {
                            params[0] = new Integer(value);
                        }
                        catch (NumberFormatException ex3) {
                            ok = false;
                        }
                    }
                    if (ok) {
                        methods[i].invoke(o, params);
                        return true;
                    }
                }
                if ("setProperty".equals(methods[i].getName())) {
                    if (methods[i].getReturnType() == Boolean.TYPE) {
                        setPropertyMethodBool = methods[i];
                    }
                    else {
                        setPropertyMethodVoid = methods[i];
                    }
                }
            }
            if (setPropertyMethodBool != null || setPropertyMethodVoid != null) {
                final Object[] params2 = { name, value };
                if (setPropertyMethodBool != null) {
                    try {
                        return (boolean)setPropertyMethodBool.invoke(o, params2);
                    }
                    catch (IllegalArgumentException biae) {
                        if (setPropertyMethodVoid != null) {
                            setPropertyMethodVoid.invoke(o, params2);
                            return true;
                        }
                        throw biae;
                    }
                }
                setPropertyMethodVoid.invoke(o, params2);
                return true;
            }
        }
        catch (IllegalArgumentException ex2) {
            GenericNamingResourcesFactory.log.warn((Object)("IAE " + o + " " + name + " " + value), (Throwable)ex2);
        }
        catch (SecurityException ex2) {
            if (GenericNamingResourcesFactory.log.isDebugEnabled()) {
                GenericNamingResourcesFactory.log.debug((Object)("IntrospectionUtils: SecurityException for " + o.getClass() + " " + name + "=" + value + ")"), (Throwable)ex2);
            }
        }
        catch (IllegalAccessException iae) {
            if (GenericNamingResourcesFactory.log.isDebugEnabled()) {
                GenericNamingResourcesFactory.log.debug((Object)("IntrospectionUtils: IllegalAccessException for " + o.getClass() + " " + name + "=" + value + ")"), (Throwable)iae);
            }
        }
        catch (InvocationTargetException ie) {
            final Throwable cause = ie.getCause();
            if (cause instanceof ThreadDeath) {
                throw (ThreadDeath)cause;
            }
            if (cause instanceof VirtualMachineError) {
                throw (VirtualMachineError)cause;
            }
            if (GenericNamingResourcesFactory.log.isDebugEnabled()) {
                GenericNamingResourcesFactory.log.debug((Object)("IntrospectionUtils: InvocationTargetException for " + o.getClass() + " " + name + "=" + value + ")"), (Throwable)ie);
            }
        }
        return false;
    }
    
    public static String capitalize(final String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        final char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    
    static {
        log = LogFactory.getLog((Class)GenericNamingResourcesFactory.class);
    }
}

package org.apache.naming.factory;
import java.util.Enumeration;
import java.util.Map;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import javax.naming.RefAddr;
import java.util.Locale;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.beans.Introspector;
import javax.naming.NamingException;
import javax.naming.Reference;
import org.apache.naming.ResourceRef;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
public class BeanFactory implements ObjectFactory {
    @Override
    public Object getObjectInstance ( final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment ) throws NamingException {
        if ( obj instanceof ResourceRef ) {
            try {
                final Reference ref = ( Reference ) obj;
                final String beanClassName = ref.getClassName();
                Class<?> beanClass = null;
                final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
                if ( tcl != null ) {
                    try {
                        beanClass = tcl.loadClass ( beanClassName );
                    } catch ( ClassNotFoundException ex3 ) {}
                } else {
                    try {
                        beanClass = Class.forName ( beanClassName );
                    } catch ( ClassNotFoundException e ) {
                        e.printStackTrace();
                    }
                }
                if ( beanClass == null ) {
                    throw new NamingException ( "Class not found: " + beanClassName );
                }
                final BeanInfo bi = Introspector.getBeanInfo ( beanClass );
                final PropertyDescriptor[] pda = bi.getPropertyDescriptors();
                final Object bean = beanClass.newInstance();
                RefAddr ra = ref.get ( "forceString" );
                final Map<String, Method> forced = new HashMap<String, Method>();
                if ( ra != null ) {
                    final String value = ( String ) ra.getContent();
                    final Class<?>[] paramTypes = ( Class<?>[] ) new Class[] { String.class };
                    for ( String param : value.split ( "," ) ) {
                        param = param.trim();
                        final int index = param.indexOf ( 61 );
                        String setterName;
                        if ( index >= 0 ) {
                            setterName = param.substring ( index + 1 ).trim();
                            param = param.substring ( 0, index ).trim();
                        } else {
                            setterName = "set" + param.substring ( 0, 1 ).toUpperCase ( Locale.ENGLISH ) + param.substring ( 1 );
                        }
                        try {
                            forced.put ( param, beanClass.getMethod ( setterName, paramTypes ) );
                        } catch ( NoSuchMethodException | SecurityException ex ) {
                            throw new NamingException ( "Forced String setter " + setterName + " not found for property " + param );
                        }
                    }
                }
                final Enumeration<RefAddr> e2 = ref.getAll();
                while ( e2.hasMoreElements() ) {
                    ra = e2.nextElement();
                    final String propName = ra.getType();
                    if ( !propName.equals ( "factory" ) && !propName.equals ( "scope" ) && !propName.equals ( "auth" ) && !propName.equals ( "forceString" ) ) {
                        if ( propName.equals ( "singleton" ) ) {
                            continue;
                        }
                        final String value = ( String ) ra.getContent();
                        final Object[] valueArray = { null };
                        final Method method = forced.get ( propName );
                        if ( method != null ) {
                            valueArray[0] = value;
                            try {
                                method.invoke ( bean, valueArray );
                                continue;
                            } catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex2 ) {
                                throw new NamingException ( "Forced String setter " + method.getName() + " threw exception for property " + propName );
                            }
                        }
                        int i = 0;
                        i = 0;
                        while ( i < pda.length ) {
                            if ( pda[i].getName().equals ( propName ) ) {
                                final Class<?> propType = pda[i].getPropertyType();
                                if ( propType.equals ( String.class ) ) {
                                    valueArray[0] = value;
                                } else if ( propType.equals ( Character.class ) || propType.equals ( Character.TYPE ) ) {
                                    valueArray[0] = value.charAt ( 0 );
                                } else if ( propType.equals ( Byte.class ) || propType.equals ( Byte.TYPE ) ) {
                                    valueArray[0] = Byte.valueOf ( value );
                                } else if ( propType.equals ( Short.class ) || propType.equals ( Short.TYPE ) ) {
                                    valueArray[0] = Short.valueOf ( value );
                                } else if ( propType.equals ( Integer.class ) || propType.equals ( Integer.TYPE ) ) {
                                    valueArray[0] = Integer.valueOf ( value );
                                } else if ( propType.equals ( Long.class ) || propType.equals ( Long.TYPE ) ) {
                                    valueArray[0] = Long.valueOf ( value );
                                } else if ( propType.equals ( Float.class ) || propType.equals ( Float.TYPE ) ) {
                                    valueArray[0] = Float.valueOf ( value );
                                } else if ( propType.equals ( Double.class ) || propType.equals ( Double.TYPE ) ) {
                                    valueArray[0] = Double.valueOf ( value );
                                } else {
                                    if ( !propType.equals ( Boolean.class ) && !propType.equals ( Boolean.TYPE ) ) {
                                        throw new NamingException ( "String conversion for property " + propName + " of type '" + propType.getName() + "' not available" );
                                    }
                                    valueArray[0] = Boolean.valueOf ( value );
                                }
                                final Method setProp = pda[i].getWriteMethod();
                                if ( setProp != null ) {
                                    setProp.invoke ( bean, valueArray );
                                    break;
                                }
                                throw new NamingException ( "Write not allowed for property: " + propName );
                            } else {
                                ++i;
                            }
                        }
                        if ( i == pda.length ) {
                            throw new NamingException ( "No set method found for property: " + propName );
                        }
                        continue;
                    }
                }
                return bean;
            } catch ( IntrospectionException ie ) {
                final NamingException ne = new NamingException ( ie.getMessage() );
                ne.setRootCause ( ie );
                throw ne;
            } catch ( IllegalAccessException iae ) {
                final NamingException ne = new NamingException ( iae.getMessage() );
                ne.setRootCause ( iae );
                throw ne;
            } catch ( InstantiationException ie2 ) {
                final NamingException ne = new NamingException ( ie2.getMessage() );
                ne.setRootCause ( ie2 );
                throw ne;
            } catch ( InvocationTargetException ite ) {
                final Throwable cause = ite.getCause();
                if ( cause instanceof ThreadDeath ) {
                    throw ( ThreadDeath ) cause;
                }
                if ( cause instanceof VirtualMachineError ) {
                    throw ( VirtualMachineError ) cause;
                }
                final NamingException ne2 = new NamingException ( ite.getMessage() );
                ne2.setRootCause ( ite );
                throw ne2;
            }
        }
        return null;
    }
}

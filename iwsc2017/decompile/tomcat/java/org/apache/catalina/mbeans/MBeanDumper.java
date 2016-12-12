package org.apache.catalina.mbeans;
import org.apache.juli.logging.LogFactory;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import java.util.Iterator;
import java.lang.reflect.Array;
import org.apache.tomcat.util.ExceptionUtils;
import javax.management.JMRuntimeException;
import javax.management.ObjectName;
import java.util.Set;
import javax.management.MBeanServer;
import org.apache.juli.logging.Log;
public class MBeanDumper {
    private static final Log log;
    private static final String CRLF = "\r\n";
    public static String dumpBeans ( final MBeanServer mbeanServer, final Set<ObjectName> names ) {
        final StringBuilder buf = new StringBuilder();
        for ( final ObjectName oname : names ) {
            buf.append ( "Name: " );
            buf.append ( oname.toString() );
            buf.append ( "\r\n" );
            try {
                final MBeanInfo minfo = mbeanServer.getMBeanInfo ( oname );
                String code = minfo.getClassName();
                if ( "org.apache.commons.modeler.BaseModelMBean".equals ( code ) ) {
                    code = ( String ) mbeanServer.getAttribute ( oname, "modelerType" );
                }
                buf.append ( "modelerType: " );
                buf.append ( code );
                buf.append ( "\r\n" );
                final MBeanAttributeInfo[] attrs = minfo.getAttributes();
                Object value = null;
                for ( int i = 0; i < attrs.length; ++i ) {
                    if ( attrs[i].isReadable() ) {
                        final String attName = attrs[i].getName();
                        if ( !"modelerType".equals ( attName ) ) {
                            if ( attName.indexOf ( 61 ) < 0 && attName.indexOf ( 58 ) < 0 ) {
                                if ( attName.indexOf ( 32 ) < 0 ) {
                                    try {
                                        value = mbeanServer.getAttribute ( oname, attName );
                                    } catch ( JMRuntimeException rme ) {
                                        final Throwable cause = rme.getCause();
                                        if ( cause instanceof UnsupportedOperationException ) {
                                            if ( MBeanDumper.log.isDebugEnabled() ) {
                                                MBeanDumper.log.debug ( "Error getting attribute " + oname + " " + attName, rme );
                                            }
                                        } else if ( cause instanceof NullPointerException ) {
                                            if ( MBeanDumper.log.isDebugEnabled() ) {
                                                MBeanDumper.log.debug ( "Error getting attribute " + oname + " " + attName, rme );
                                            }
                                        } else {
                                            MBeanDumper.log.error ( "Error getting attribute " + oname + " " + attName, rme );
                                        }
                                        continue;
                                    } catch ( Throwable t ) {
                                        ExceptionUtils.handleThrowable ( t );
                                        MBeanDumper.log.error ( "Error getting attribute " + oname + " " + attName, t );
                                        continue;
                                    }
                                    if ( value != null ) {
                                        try {
                                            final Class<?> c = value.getClass();
                                            String valueString;
                                            if ( c.isArray() ) {
                                                final int len = Array.getLength ( value );
                                                final StringBuilder sb = new StringBuilder ( "Array[" + c.getComponentType().getName() + "] of length " + len );
                                                if ( len > 0 ) {
                                                    sb.append ( "\r\n" );
                                                }
                                                for ( int j = 0; j < len; ++j ) {
                                                    sb.append ( "\t" );
                                                    final Object item = Array.get ( value, j );
                                                    if ( item == null ) {
                                                        sb.append ( "NULL VALUE" );
                                                    } else {
                                                        try {
                                                            sb.append ( escape ( item.toString() ) );
                                                        } catch ( Throwable t2 ) {
                                                            ExceptionUtils.handleThrowable ( t2 );
                                                            sb.append ( "NON-STRINGABLE VALUE" );
                                                        }
                                                    }
                                                    if ( j < len - 1 ) {
                                                        sb.append ( "\r\n" );
                                                    }
                                                }
                                                valueString = sb.toString();
                                            } else {
                                                valueString = escape ( value.toString() );
                                            }
                                            buf.append ( attName );
                                            buf.append ( ": " );
                                            buf.append ( valueString );
                                            buf.append ( "\r\n" );
                                        } catch ( Throwable t3 ) {
                                            ExceptionUtils.handleThrowable ( t3 );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch ( Throwable t4 ) {
                ExceptionUtils.handleThrowable ( t4 );
            }
            buf.append ( "\r\n" );
        }
        return buf.toString();
    }
    public static String escape ( final String value ) {
        int idx = value.indexOf ( "\n" );
        if ( idx < 0 ) {
            return value;
        }
        int prev = 0;
        final StringBuilder sb = new StringBuilder();
        while ( idx >= 0 ) {
            appendHead ( sb, value, prev, idx );
            sb.append ( "\\n\n " );
            prev = idx + 1;
            if ( idx == value.length() - 1 ) {
                break;
            }
            idx = value.indexOf ( 10, idx + 1 );
        }
        if ( prev < value.length() ) {
            appendHead ( sb, value, prev, value.length() );
        }
        return sb.toString();
    }
    private static void appendHead ( final StringBuilder sb, final String value, final int start, final int end ) {
        if ( end < 1 ) {
            return;
        }
        int pos;
        for ( pos = start; end - pos > 78; pos += 78 ) {
            sb.append ( value.substring ( pos, pos + 78 ) );
            sb.append ( "\n " );
        }
        sb.append ( value.substring ( pos, end ) );
    }
    static {
        log = LogFactory.getLog ( MBeanDumper.class );
    }
}

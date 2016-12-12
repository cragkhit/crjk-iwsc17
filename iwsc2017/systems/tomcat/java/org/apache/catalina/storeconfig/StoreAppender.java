package org.apache.catalina.storeconfig;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Iterator;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.descriptor.web.ResourceBase;
public class StoreAppender {
    private static Class<?> persistables[] = { String.class, Integer.class,
                                               Integer.TYPE, Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE,
                                               Character.class, Character.TYPE, Double.class, Double.TYPE,
                                               Float.class, Float.TYPE, Long.class, Long.TYPE, Short.class,
                                               Short.TYPE, InetAddress.class
                                             };
    private int pos = 0;
    public void printCloseTag ( PrintWriter aWriter, StoreDescription aDesc )
    throws Exception {
        aWriter.print ( "</" );
        aWriter.print ( aDesc.getTag() );
        aWriter.println ( ">" );
    }
    public void printOpenTag ( PrintWriter aWriter, int indent, Object bean,
                               StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        if ( aDesc.isAttributes() && bean != null ) {
            printAttributes ( aWriter, indent, bean, aDesc );
        }
        aWriter.println ( ">" );
    }
    public void printTag ( PrintWriter aWriter, int indent, Object bean,
                           StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        if ( aDesc.isAttributes() && bean != null ) {
            printAttributes ( aWriter, indent, bean, aDesc );
        }
        aWriter.println ( "/>" );
    }
    public void printTagContent ( PrintWriter aWriter, String tag, String content )
    throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( tag );
        aWriter.print ( ">" );
        aWriter.print ( convertStr ( content ) );
        aWriter.print ( "</" );
        aWriter.print ( tag );
        aWriter.println ( ">" );
    }
    public void printTagValueArray ( PrintWriter aWriter, String tag, int indent,
                                     String[] elements ) {
        if ( elements != null && elements.length > 0 ) {
            printIndent ( aWriter, indent + 2 );
            aWriter.print ( "<" );
            aWriter.print ( tag );
            aWriter.print ( ">" );
            for ( int i = 0; i < elements.length; i++ ) {
                printIndent ( aWriter, indent + 4 );
                aWriter.print ( elements[i] );
                if ( i + 1 < elements.length ) {
                    aWriter.println ( "," );
                }
            }
            printIndent ( aWriter, indent + 2 );
            aWriter.print ( "</" );
            aWriter.print ( tag );
            aWriter.println ( ">" );
        }
    }
    public void printTagArray ( PrintWriter aWriter, String tag, int indent,
                                String[] elements ) throws Exception {
        if ( elements != null ) {
            for ( int i = 0; i < elements.length; i++ ) {
                printIndent ( aWriter, indent );
                printTagContent ( aWriter, tag, elements[i] );
            }
        }
    }
    public void printIndent ( PrintWriter aWriter, int indent ) {
        for ( int i = 0; i < indent; i++ ) {
            aWriter.print ( ' ' );
        }
        pos = indent;
    }
    public void printAttributes ( PrintWriter writer, int indent, Object bean,
                                  StoreDescription desc ) throws Exception {
        printAttributes ( writer, indent, true, bean, desc );
    }
    public void printAttributes ( PrintWriter writer, int indent,
                                  boolean include, Object bean, StoreDescription desc )
    throws Exception {
        if ( include && desc != null && !desc.isStandard() ) {
            writer.print ( " className=\"" );
            writer.print ( bean.getClass().getName() );
            writer.print ( "\"" );
        }
        PropertyDescriptor descriptors[] = Introspector.getBeanInfo (
                                               bean.getClass() ).getPropertyDescriptors();
        if ( descriptors == null ) {
            descriptors = new PropertyDescriptor[0];
        }
        Object bean2 = defaultInstance ( bean );
        for ( int i = 0; i < descriptors.length; i++ ) {
            if ( descriptors[i] instanceof IndexedPropertyDescriptor ) {
                continue;
            }
            if ( !isPersistable ( descriptors[i].getPropertyType() )
                    || ( descriptors[i].getReadMethod() == null )
                    || ( descriptors[i].getWriteMethod() == null ) ) {
                continue;
            }
            if ( desc.isTransientAttribute ( descriptors[i].getName() ) ) {
                continue;
            }
            Object value = IntrospectionUtils.getProperty ( bean, descriptors[i]
                           .getName() );
            if ( value == null ) {
                continue;
            }
            Object value2 = IntrospectionUtils.getProperty ( bean2,
                            descriptors[i].getName() );
            if ( value.equals ( value2 ) ) {
                continue;
            }
            printAttribute ( writer, indent, bean, desc, descriptors[i].getName(), bean2, value );
        }
        if ( bean instanceof ResourceBase ) {
            ResourceBase resource = ( ResourceBase ) bean;
            for ( Iterator<String> iter = resource.listProperties(); iter.hasNext(); ) {
                String name = iter.next();
                Object value = resource.getProperty ( name );
                if ( !isPersistable ( value.getClass() ) ) {
                    continue;
                }
                if ( desc.isTransientAttribute ( name ) ) {
                    continue;
                }
                printValue ( writer, indent, name, value );
            }
        }
    }
    protected void printAttribute ( PrintWriter writer, int indent, Object bean, StoreDescription desc, String attributeName, Object bean2, Object value ) {
        if ( isPrintValue ( bean, bean2, attributeName, desc ) ) {
            printValue ( writer, indent, attributeName, value );
        }
    }
    public boolean isPrintValue ( Object bean, Object bean2, String attrName,
                                  StoreDescription desc ) {
        boolean printValue = false;
        Object value = IntrospectionUtils.getProperty ( bean, attrName );
        if ( value != null ) {
            Object value2 = IntrospectionUtils.getProperty ( bean2, attrName );
            printValue = !value.equals ( value2 );
        }
        return printValue;
    }
    public Object defaultInstance ( Object bean ) throws InstantiationException,
        IllegalAccessException {
        return bean.getClass().newInstance();
    }
    public void printValue ( PrintWriter writer, int indent, String name,
                             Object value ) {
        if ( value instanceof InetAddress ) {
            value = ( ( InetAddress ) value ).getHostAddress();
        }
        if ( ! ( value instanceof String ) ) {
            value = value.toString();
        }
        String strValue = convertStr ( ( String ) value );
        pos = pos + name.length() + strValue.length();
        if ( pos > 60 ) {
            writer.println();
            printIndent ( writer, indent + 4 );
        } else {
            writer.print ( ' ' );
        }
        writer.print ( name );
        writer.print ( "=\"" );
        writer.print ( strValue );
        writer.print ( "\"" );
    }
    public String convertStr ( String input ) {
        StringBuffer filtered = new StringBuffer ( input.length() );
        char c;
        for ( int i = 0; i < input.length(); i++ ) {
            c = input.charAt ( i );
            if ( c == '<' ) {
                filtered.append ( "&lt;" );
            } else if ( c == '>' ) {
                filtered.append ( "&gt;" );
            } else if ( c == '\'' ) {
                filtered.append ( "&apos;" );
            } else if ( c == '"' ) {
                filtered.append ( "&quot;" );
            } else if ( c == '&' ) {
                filtered.append ( "&amp;" );
            } else {
                filtered.append ( c );
            }
        }
        return ( filtered.toString() );
    }
    protected boolean isPersistable ( Class<?> clazz ) {
        for ( int i = 0; i < persistables.length; i++ ) {
            if ( persistables[i] == clazz || persistables[i].isAssignableFrom ( clazz ) ) {
                return true;
            }
        }
        return false;
    }
}

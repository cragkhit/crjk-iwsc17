package org.apache.catalina.storeconfig;
import java.net.InetAddress;
import java.util.Iterator;
import org.apache.tomcat.util.descriptor.web.ResourceBase;
import org.apache.tomcat.util.IntrospectionUtils;
import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.io.PrintWriter;
public class StoreAppender {
    private static Class<?>[] persistables;
    private int pos;
    public StoreAppender() {
        this.pos = 0;
    }
    public void printCloseTag ( final PrintWriter aWriter, final StoreDescription aDesc ) throws Exception {
        aWriter.print ( "</" );
        aWriter.print ( aDesc.getTag() );
        aWriter.println ( ">" );
    }
    public void printOpenTag ( final PrintWriter aWriter, final int indent, final Object bean, final StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        if ( aDesc.isAttributes() && bean != null ) {
            this.printAttributes ( aWriter, indent, bean, aDesc );
        }
        aWriter.println ( ">" );
    }
    public void printTag ( final PrintWriter aWriter, final int indent, final Object bean, final StoreDescription aDesc ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( aDesc.getTag() );
        if ( aDesc.isAttributes() && bean != null ) {
            this.printAttributes ( aWriter, indent, bean, aDesc );
        }
        aWriter.println ( "/>" );
    }
    public void printTagContent ( final PrintWriter aWriter, final String tag, final String content ) throws Exception {
        aWriter.print ( "<" );
        aWriter.print ( tag );
        aWriter.print ( ">" );
        aWriter.print ( this.convertStr ( content ) );
        aWriter.print ( "</" );
        aWriter.print ( tag );
        aWriter.println ( ">" );
    }
    public void printTagValueArray ( final PrintWriter aWriter, final String tag, final int indent, final String[] elements ) {
        if ( elements != null && elements.length > 0 ) {
            this.printIndent ( aWriter, indent + 2 );
            aWriter.print ( "<" );
            aWriter.print ( tag );
            aWriter.print ( ">" );
            for ( int i = 0; i < elements.length; ++i ) {
                this.printIndent ( aWriter, indent + 4 );
                aWriter.print ( elements[i] );
                if ( i + 1 < elements.length ) {
                    aWriter.println ( "," );
                }
            }
            this.printIndent ( aWriter, indent + 2 );
            aWriter.print ( "</" );
            aWriter.print ( tag );
            aWriter.println ( ">" );
        }
    }
    public void printTagArray ( final PrintWriter aWriter, final String tag, final int indent, final String[] elements ) throws Exception {
        if ( elements != null ) {
            for ( int i = 0; i < elements.length; ++i ) {
                this.printIndent ( aWriter, indent );
                this.printTagContent ( aWriter, tag, elements[i] );
            }
        }
    }
    public void printIndent ( final PrintWriter aWriter, final int indent ) {
        for ( int i = 0; i < indent; ++i ) {
            aWriter.print ( ' ' );
        }
        this.pos = indent;
    }
    public void printAttributes ( final PrintWriter writer, final int indent, final Object bean, final StoreDescription desc ) throws Exception {
        this.printAttributes ( writer, indent, true, bean, desc );
    }
    public void printAttributes ( final PrintWriter writer, final int indent, final boolean include, final Object bean, final StoreDescription desc ) throws Exception {
        if ( include && desc != null && !desc.isStandard() ) {
            writer.print ( " className=\"" );
            writer.print ( bean.getClass().getName() );
            writer.print ( "\"" );
        }
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo ( bean.getClass() ).getPropertyDescriptors();
        if ( descriptors == null ) {
            descriptors = new PropertyDescriptor[0];
        }
        final Object bean2 = this.defaultInstance ( bean );
        for ( int i = 0; i < descriptors.length; ++i ) {
            if ( ! ( descriptors[i] instanceof IndexedPropertyDescriptor ) ) {
                if ( this.isPersistable ( descriptors[i].getPropertyType() ) && descriptors[i].getReadMethod() != null ) {
                    if ( descriptors[i].getWriteMethod() != null ) {
                        if ( !desc.isTransientAttribute ( descriptors[i].getName() ) ) {
                            final Object value = IntrospectionUtils.getProperty ( bean, descriptors[i].getName() );
                            if ( value != null ) {
                                final Object value2 = IntrospectionUtils.getProperty ( bean2, descriptors[i].getName() );
                                if ( !value.equals ( value2 ) ) {
                                    this.printAttribute ( writer, indent, bean, desc, descriptors[i].getName(), bean2, value );
                                }
                            }
                        }
                    }
                }
            }
        }
        if ( bean instanceof ResourceBase ) {
            final ResourceBase resource = ( ResourceBase ) bean;
            final Iterator<String> iter = resource.listProperties();
            while ( iter.hasNext() ) {
                final String name = iter.next();
                final Object value3 = resource.getProperty ( name );
                if ( !this.isPersistable ( value3.getClass() ) ) {
                    continue;
                }
                if ( desc.isTransientAttribute ( name ) ) {
                    continue;
                }
                this.printValue ( writer, indent, name, value3 );
            }
        }
    }
    protected void printAttribute ( final PrintWriter writer, final int indent, final Object bean, final StoreDescription desc, final String attributeName, final Object bean2, final Object value ) {
        if ( this.isPrintValue ( bean, bean2, attributeName, desc ) ) {
            this.printValue ( writer, indent, attributeName, value );
        }
    }
    public boolean isPrintValue ( final Object bean, final Object bean2, final String attrName, final StoreDescription desc ) {
        boolean printValue = false;
        final Object value = IntrospectionUtils.getProperty ( bean, attrName );
        if ( value != null ) {
            final Object value2 = IntrospectionUtils.getProperty ( bean2, attrName );
            printValue = !value.equals ( value2 );
        }
        return printValue;
    }
    public Object defaultInstance ( final Object bean ) throws InstantiationException, IllegalAccessException {
        return bean.getClass().newInstance();
    }
    public void printValue ( final PrintWriter writer, final int indent, final String name, Object value ) {
        if ( value instanceof InetAddress ) {
            value = ( ( InetAddress ) value ).getHostAddress();
        }
        if ( ! ( value instanceof String ) ) {
            value = value.toString();
        }
        final String strValue = this.convertStr ( ( String ) value );
        this.pos = this.pos + name.length() + strValue.length();
        if ( this.pos > 60 ) {
            writer.println();
            this.printIndent ( writer, indent + 4 );
        } else {
            writer.print ( ' ' );
        }
        writer.print ( name );
        writer.print ( "=\"" );
        writer.print ( strValue );
        writer.print ( "\"" );
    }
    public String convertStr ( final String input ) {
        final StringBuffer filtered = new StringBuffer ( input.length() );
        for ( int i = 0; i < input.length(); ++i ) {
            final char c = input.charAt ( i );
            if ( c == '<' ) {
                filtered.append ( "&lt;" );
            } else if ( c == '>' ) {
                filtered.append ( "&gt;" );
            } else if ( c == '\'' ) {
                filtered.append ( "&apos;" );
            } else if ( c == '\"' ) {
                filtered.append ( "&quot;" );
            } else if ( c == '&' ) {
                filtered.append ( "&amp;" );
            } else {
                filtered.append ( c );
            }
        }
        return filtered.toString();
    }
    protected boolean isPersistable ( final Class<?> clazz ) {
        for ( int i = 0; i < StoreAppender.persistables.length; ++i ) {
            if ( StoreAppender.persistables[i] == clazz || StoreAppender.persistables[i].isAssignableFrom ( clazz ) ) {
                return true;
            }
        }
        return false;
    }
    static {
        StoreAppender.persistables = ( Class<?>[] ) new Class[] { String.class, Integer.class, Integer.TYPE, Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE, Character.class, Character.TYPE, Double.class, Double.TYPE, Float.class, Float.TYPE, Long.class, Long.TYPE, Short.class, Short.TYPE, InetAddress.class };
    }
}

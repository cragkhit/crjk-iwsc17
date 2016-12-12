package org.apache.tomcat.util.digester;
import org.xml.sax.Attributes;
public class ObjectCreateRule extends Rule {
    public ObjectCreateRule ( String className ) {
        this ( className, ( String ) null );
    }
    public ObjectCreateRule ( String className,
                              String attributeName ) {
        this.className = className;
        this.attributeName = attributeName;
    }
    protected String attributeName = null;
    protected String className = null;
    @Override
    public void begin ( String namespace, String name, Attributes attributes )
    throws Exception {
        String realClassName = className;
        if ( attributeName != null ) {
            String value = attributes.getValue ( attributeName );
            if ( value != null ) {
                realClassName = value;
            }
        }
        if ( digester.log.isDebugEnabled() ) {
            digester.log.debug ( "[ObjectCreateRule]{" + digester.match +
                                 "}New " + realClassName );
        }
        if ( realClassName == null ) {
            throw new NullPointerException ( "No class name specified for " +
                                             namespace + " " + name );
        }
        Class<?> clazz = digester.getClassLoader().loadClass ( realClassName );
        Object instance = clazz.newInstance();
        digester.push ( instance );
    }
    @Override
    public void end ( String namespace, String name ) throws Exception {
        Object top = digester.pop();
        if ( digester.log.isDebugEnabled() ) {
            digester.log.debug ( "[ObjectCreateRule]{" + digester.match +
                                 "} Pop " + top.getClass().getName() );
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ObjectCreateRule[" );
        sb.append ( "className=" );
        sb.append ( className );
        sb.append ( ", attributeName=" );
        sb.append ( attributeName );
        sb.append ( "]" );
        return ( sb.toString() );
    }
}

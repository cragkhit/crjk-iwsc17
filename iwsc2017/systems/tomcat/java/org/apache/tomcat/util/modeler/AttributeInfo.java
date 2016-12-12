package org.apache.tomcat.util.modeler;
import javax.management.MBeanAttributeInfo;
public class AttributeInfo extends FeatureInfo {
    static final long serialVersionUID = -2511626862303972143L;
    protected String displayName = null;
    protected String getMethod = null;
    protected String setMethod = null;
    protected boolean readable = true;
    protected boolean writeable = true;
    protected boolean is = false;
    public String getDisplayName() {
        return ( this.displayName );
    }
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }
    public String getGetMethod() {
        if ( getMethod == null ) {
            getMethod = getMethodName ( getName(), true, isIs() );
        }
        return ( this.getMethod );
    }
    public void setGetMethod ( String getMethod ) {
        this.getMethod = getMethod;
    }
    public boolean isIs() {
        return ( this.is );
    }
    public void setIs ( boolean is ) {
        this.is = is;
    }
    public boolean isReadable() {
        return ( this.readable );
    }
    public void setReadable ( boolean readable ) {
        this.readable = readable;
    }
    public String getSetMethod() {
        if ( setMethod == null ) {
            setMethod = getMethodName ( getName(), false, false );
        }
        return ( this.setMethod );
    }
    public void setSetMethod ( String setMethod ) {
        this.setMethod = setMethod;
    }
    public boolean isWriteable() {
        return ( this.writeable );
    }
    public void setWriteable ( boolean writeable ) {
        this.writeable = writeable;
    }
    MBeanAttributeInfo createAttributeInfo() {
        if ( info == null ) {
            info = new MBeanAttributeInfo ( getName(), getType(), getDescription(),
                                            isReadable(), isWriteable(), false );
        }
        return ( MBeanAttributeInfo ) info;
    }
    private String getMethodName ( String name, boolean getter, boolean is ) {
        StringBuilder sb = new StringBuilder();
        if ( getter ) {
            if ( is ) {
                sb.append ( "is" );
            } else {
                sb.append ( "get" );
            }
        } else {
            sb.append ( "set" );
        }
        sb.append ( Character.toUpperCase ( name.charAt ( 0 ) ) );
        sb.append ( name.substring ( 1 ) );
        return ( sb.toString() );
    }
}

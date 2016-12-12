package javax.servlet.jsp.tagext;
import java.util.Hashtable;
public class TagData implements Cloneable {
    public static final Object REQUEST_TIME_VALUE = new Object();
    public TagData ( Object[] atts[] ) {
        if ( atts == null ) {
            attributes = new Hashtable<>();
        } else {
            attributes = new Hashtable<> ( atts.length );
        }
        if ( atts != null ) {
            for ( int i = 0; i < atts.length; i++ ) {
                attributes.put ( ( String ) atts[i][0], atts[i][1] );
            }
        }
    }
    public TagData ( Hashtable<String, Object> attrs ) {
        this.attributes = attrs;
    }
    public String getId() {
        return getAttributeString ( TagAttributeInfo.ID );
    }
    public Object getAttribute ( String attName ) {
        return attributes.get ( attName );
    }
    public void setAttribute ( String attName,
                               Object value ) {
        attributes.put ( attName, value );
    }
    public String getAttributeString ( String attName ) {
        Object o = attributes.get ( attName );
        if ( o == null ) {
            return null;
        }
        return ( String ) o;
    }
    public java.util.Enumeration<String> getAttributes() {
        return attributes.keys();
    }
    private final Hashtable<String, Object> attributes;
}

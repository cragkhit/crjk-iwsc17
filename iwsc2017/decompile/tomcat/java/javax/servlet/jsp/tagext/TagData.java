package javax.servlet.jsp.tagext;
import java.util.Enumeration;
import java.util.Hashtable;
public class TagData implements Cloneable {
    public static final Object REQUEST_TIME_VALUE;
    private final Hashtable<String, Object> attributes;
    public TagData ( final Object[][] atts ) {
        if ( atts == null ) {
            this.attributes = new Hashtable<String, Object>();
        } else {
            this.attributes = new Hashtable<String, Object> ( atts.length );
        }
        if ( atts != null ) {
            for ( int i = 0; i < atts.length; ++i ) {
                this.attributes.put ( ( String ) atts[i][0], atts[i][1] );
            }
        }
    }
    public TagData ( final Hashtable<String, Object> attrs ) {
        this.attributes = attrs;
    }
    public String getId() {
        return this.getAttributeString ( "id" );
    }
    public Object getAttribute ( final String attName ) {
        return this.attributes.get ( attName );
    }
    public void setAttribute ( final String attName, final Object value ) {
        this.attributes.put ( attName, value );
    }
    public String getAttributeString ( final String attName ) {
        final Object o = this.attributes.get ( attName );
        if ( o == null ) {
            return null;
        }
        return ( String ) o;
    }
    public Enumeration<String> getAttributes() {
        return this.attributes.keys();
    }
    static {
        REQUEST_TIME_VALUE = new Object();
    }
}

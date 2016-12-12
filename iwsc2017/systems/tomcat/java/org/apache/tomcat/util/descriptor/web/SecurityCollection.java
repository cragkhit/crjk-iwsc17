package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
import org.apache.tomcat.util.buf.UDecoder;
public class SecurityCollection implements Serializable {
    private static final long serialVersionUID = 1L;
    private String encoding = null;
    public void setEncoding ( String encoding ) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        if ( encoding == null || encoding.length() == 0 ) {
            return "UTF-8";
        }
        return encoding;
    }
    public SecurityCollection() {
        this ( null, null );
    }
    public SecurityCollection ( String name, String description ) {
        super();
        setName ( name );
        setDescription ( description );
    }
    private String description = null;
    private String methods[] = new String[0];
    private String omittedMethods[] = new String[0];
    private String name = null;
    private String patterns[] = new String[0];
    private boolean isFromDescriptor = true;
    public String getDescription() {
        return ( this.description );
    }
    public void setDescription ( String description ) {
        this.description = description;
    }
    public String getName() {
        return ( this.name );
    }
    public void setName ( String name ) {
        this.name = name;
    }
    public boolean isFromDescriptor() {
        return isFromDescriptor;
    }
    public void setFromDescriptor ( boolean isFromDescriptor ) {
        this.isFromDescriptor = isFromDescriptor;
    }
    public void addMethod ( String method ) {
        if ( method == null ) {
            return;
        }
        String results[] = new String[methods.length + 1];
        for ( int i = 0; i < methods.length; i++ ) {
            results[i] = methods[i];
        }
        results[methods.length] = method;
        methods = results;
    }
    public void addOmittedMethod ( String method ) {
        if ( method == null ) {
            return;
        }
        String results[] = new String[omittedMethods.length + 1];
        for ( int i = 0; i < omittedMethods.length; i++ ) {
            results[i] = omittedMethods[i];
        }
        results[omittedMethods.length] = method;
        omittedMethods = results;
    }
    public void addPattern ( String pattern ) {
        addPatternDecoded ( UDecoder.URLDecode ( pattern, "UTF-8" ) );
    }
    public void addPatternDecoded ( String pattern ) {
        if ( pattern == null ) {
            return;
        }
        String decodedPattern = UDecoder.URLDecode ( pattern );
        String results[] = new String[patterns.length + 1];
        for ( int i = 0; i < patterns.length; i++ ) {
            results[i] = patterns[i];
        }
        results[patterns.length] = decodedPattern;
        patterns = results;
    }
    public boolean findMethod ( String method ) {
        if ( methods.length == 0 && omittedMethods.length == 0 ) {
            return true;
        }
        if ( methods.length > 0 ) {
            for ( int i = 0; i < methods.length; i++ ) {
                if ( methods[i].equals ( method ) ) {
                    return true;
                }
            }
            return false;
        }
        if ( omittedMethods.length > 0 ) {
            for ( int i = 0; i < omittedMethods.length; i++ ) {
                if ( omittedMethods[i].equals ( method ) ) {
                    return false;
                }
            }
        }
        return true;
    }
    public String[] findMethods() {
        return ( methods );
    }
    public String[] findOmittedMethods() {
        return ( omittedMethods );
    }
    public boolean findPattern ( String pattern ) {
        for ( int i = 0; i < patterns.length; i++ ) {
            if ( patterns[i].equals ( pattern ) ) {
                return true;
            }
        }
        return false;
    }
    public String[] findPatterns() {
        return ( patterns );
    }
    public void removeMethod ( String method ) {
        if ( method == null ) {
            return;
        }
        int n = -1;
        for ( int i = 0; i < methods.length; i++ ) {
            if ( methods[i].equals ( method ) ) {
                n = i;
                break;
            }
        }
        if ( n >= 0 ) {
            int j = 0;
            String results[] = new String[methods.length - 1];
            for ( int i = 0; i < methods.length; i++ ) {
                if ( i != n ) {
                    results[j++] = methods[i];
                }
            }
            methods = results;
        }
    }
    public void removeOmittedMethod ( String method ) {
        if ( method == null ) {
            return;
        }
        int n = -1;
        for ( int i = 0; i < omittedMethods.length; i++ ) {
            if ( omittedMethods[i].equals ( method ) ) {
                n = i;
                break;
            }
        }
        if ( n >= 0 ) {
            int j = 0;
            String results[] = new String[omittedMethods.length - 1];
            for ( int i = 0; i < omittedMethods.length; i++ ) {
                if ( i != n ) {
                    results[j++] = omittedMethods[i];
                }
            }
            omittedMethods = results;
        }
    }
    public void removePattern ( String pattern ) {
        if ( pattern == null ) {
            return;
        }
        int n = -1;
        for ( int i = 0; i < patterns.length; i++ ) {
            if ( patterns[i].equals ( pattern ) ) {
                n = i;
                break;
            }
        }
        if ( n >= 0 ) {
            int j = 0;
            String results[] = new String[patterns.length - 1];
            for ( int i = 0; i < patterns.length; i++ ) {
                if ( i != n ) {
                    results[j++] = patterns[i];
                }
            }
            patterns = results;
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "SecurityCollection[" );
        sb.append ( name );
        if ( description != null ) {
            sb.append ( ", " );
            sb.append ( description );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
}

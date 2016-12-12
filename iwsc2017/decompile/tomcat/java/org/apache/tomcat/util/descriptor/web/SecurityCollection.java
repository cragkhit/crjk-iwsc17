package org.apache.tomcat.util.descriptor.web;
import org.apache.tomcat.util.buf.UDecoder;
import java.io.Serializable;
public class SecurityCollection implements Serializable {
    private static final long serialVersionUID = 1L;
    private String encoding;
    private String description;
    private String[] methods;
    private String[] omittedMethods;
    private String name;
    private String[] patterns;
    private boolean isFromDescriptor;
    public void setEncoding ( final String encoding ) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        if ( this.encoding == null || this.encoding.length() == 0 ) {
            return "UTF-8";
        }
        return this.encoding;
    }
    public SecurityCollection() {
        this ( null, null );
    }
    public SecurityCollection ( final String name, final String description ) {
        this.encoding = null;
        this.description = null;
        this.methods = new String[0];
        this.omittedMethods = new String[0];
        this.name = null;
        this.patterns = new String[0];
        this.isFromDescriptor = true;
        this.setName ( name );
        this.setDescription ( description );
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String description ) {
        this.description = description;
    }
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        this.name = name;
    }
    public boolean isFromDescriptor() {
        return this.isFromDescriptor;
    }
    public void setFromDescriptor ( final boolean isFromDescriptor ) {
        this.isFromDescriptor = isFromDescriptor;
    }
    public void addMethod ( final String method ) {
        if ( method == null ) {
            return;
        }
        final String[] results = new String[this.methods.length + 1];
        for ( int i = 0; i < this.methods.length; ++i ) {
            results[i] = this.methods[i];
        }
        results[this.methods.length] = method;
        this.methods = results;
    }
    public void addOmittedMethod ( final String method ) {
        if ( method == null ) {
            return;
        }
        final String[] results = new String[this.omittedMethods.length + 1];
        for ( int i = 0; i < this.omittedMethods.length; ++i ) {
            results[i] = this.omittedMethods[i];
        }
        results[this.omittedMethods.length] = method;
        this.omittedMethods = results;
    }
    public void addPattern ( final String pattern ) {
        this.addPatternDecoded ( UDecoder.URLDecode ( pattern, "UTF-8" ) );
    }
    public void addPatternDecoded ( final String pattern ) {
        if ( pattern == null ) {
            return;
        }
        final String decodedPattern = UDecoder.URLDecode ( pattern );
        final String[] results = new String[this.patterns.length + 1];
        for ( int i = 0; i < this.patterns.length; ++i ) {
            results[i] = this.patterns[i];
        }
        results[this.patterns.length] = decodedPattern;
        this.patterns = results;
    }
    public boolean findMethod ( final String method ) {
        if ( this.methods.length == 0 && this.omittedMethods.length == 0 ) {
            return true;
        }
        if ( this.methods.length > 0 ) {
            for ( int i = 0; i < this.methods.length; ++i ) {
                if ( this.methods[i].equals ( method ) ) {
                    return true;
                }
            }
            return false;
        }
        if ( this.omittedMethods.length > 0 ) {
            for ( int i = 0; i < this.omittedMethods.length; ++i ) {
                if ( this.omittedMethods[i].equals ( method ) ) {
                    return false;
                }
            }
        }
        return true;
    }
    public String[] findMethods() {
        return this.methods;
    }
    public String[] findOmittedMethods() {
        return this.omittedMethods;
    }
    public boolean findPattern ( final String pattern ) {
        for ( int i = 0; i < this.patterns.length; ++i ) {
            if ( this.patterns[i].equals ( pattern ) ) {
                return true;
            }
        }
        return false;
    }
    public String[] findPatterns() {
        return this.patterns;
    }
    public void removeMethod ( final String method ) {
        if ( method == null ) {
            return;
        }
        int n = -1;
        for ( int i = 0; i < this.methods.length; ++i ) {
            if ( this.methods[i].equals ( method ) ) {
                n = i;
                break;
            }
        }
        if ( n >= 0 ) {
            int j = 0;
            final String[] results = new String[this.methods.length - 1];
            for ( int k = 0; k < this.methods.length; ++k ) {
                if ( k != n ) {
                    results[j++] = this.methods[k];
                }
            }
            this.methods = results;
        }
    }
    public void removeOmittedMethod ( final String method ) {
        if ( method == null ) {
            return;
        }
        int n = -1;
        for ( int i = 0; i < this.omittedMethods.length; ++i ) {
            if ( this.omittedMethods[i].equals ( method ) ) {
                n = i;
                break;
            }
        }
        if ( n >= 0 ) {
            int j = 0;
            final String[] results = new String[this.omittedMethods.length - 1];
            for ( int k = 0; k < this.omittedMethods.length; ++k ) {
                if ( k != n ) {
                    results[j++] = this.omittedMethods[k];
                }
            }
            this.omittedMethods = results;
        }
    }
    public void removePattern ( final String pattern ) {
        if ( pattern == null ) {
            return;
        }
        int n = -1;
        for ( int i = 0; i < this.patterns.length; ++i ) {
            if ( this.patterns[i].equals ( pattern ) ) {
                n = i;
                break;
            }
        }
        if ( n >= 0 ) {
            int j = 0;
            final String[] results = new String[this.patterns.length - 1];
            for ( int k = 0; k < this.patterns.length; ++k ) {
                if ( k != n ) {
                    results[j++] = this.patterns[k];
                }
            }
            this.patterns = results;
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "SecurityCollection[" );
        sb.append ( this.name );
        if ( this.description != null ) {
            sb.append ( ", " );
            sb.append ( this.description );
        }
        sb.append ( "]" );
        return sb.toString();
    }
}

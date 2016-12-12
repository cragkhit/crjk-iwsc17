package org.apache.tomcat.util.descriptor.web;
import javax.servlet.descriptor.TaglibDescriptor;
public class TaglibDescriptorImpl implements TaglibDescriptor {
    private final String location;
    private final String uri;
    public TaglibDescriptorImpl ( final String location, final String uri ) {
        this.location = location;
        this.uri = uri;
    }
    public String getTaglibLocation() {
        return this.location;
    }
    public String getTaglibURI() {
        return this.uri;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ( ( this.location == null ) ? 0 : this.location.hashCode() );
        result = 31 * result + ( ( this.uri == null ) ? 0 : this.uri.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof TaglibDescriptorImpl ) ) {
            return false;
        }
        final TaglibDescriptorImpl other = ( TaglibDescriptorImpl ) obj;
        if ( this.location == null ) {
            if ( other.location != null ) {
                return false;
            }
        } else if ( !this.location.equals ( other.location ) ) {
            return false;
        }
        if ( this.uri == null ) {
            if ( other.uri != null ) {
                return false;
            }
        } else if ( !this.uri.equals ( other.uri ) ) {
            return false;
        }
        return true;
    }
}

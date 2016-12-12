package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
public class MultipartDef implements Serializable {
    private static final long serialVersionUID = 1L;
    private String location;
    private String maxFileSize;
    private String maxRequestSize;
    private String fileSizeThreshold;
    public String getLocation() {
        return this.location;
    }
    public void setLocation ( final String location ) {
        this.location = location;
    }
    public String getMaxFileSize() {
        return this.maxFileSize;
    }
    public void setMaxFileSize ( final String maxFileSize ) {
        this.maxFileSize = maxFileSize;
    }
    public String getMaxRequestSize() {
        return this.maxRequestSize;
    }
    public void setMaxRequestSize ( final String maxRequestSize ) {
        this.maxRequestSize = maxRequestSize;
    }
    public String getFileSizeThreshold() {
        return this.fileSizeThreshold;
    }
    public void setFileSizeThreshold ( final String fileSizeThreshold ) {
        this.fileSizeThreshold = fileSizeThreshold;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ( ( this.fileSizeThreshold == null ) ? 0 : this.fileSizeThreshold.hashCode() );
        result = 31 * result + ( ( this.location == null ) ? 0 : this.location.hashCode() );
        result = 31 * result + ( ( this.maxFileSize == null ) ? 0 : this.maxFileSize.hashCode() );
        result = 31 * result + ( ( this.maxRequestSize == null ) ? 0 : this.maxRequestSize.hashCode() );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( ! ( obj instanceof MultipartDef ) ) {
            return false;
        }
        final MultipartDef other = ( MultipartDef ) obj;
        if ( this.fileSizeThreshold == null ) {
            if ( other.fileSizeThreshold != null ) {
                return false;
            }
        } else if ( !this.fileSizeThreshold.equals ( other.fileSizeThreshold ) ) {
            return false;
        }
        if ( this.location == null ) {
            if ( other.location != null ) {
                return false;
            }
        } else if ( !this.location.equals ( other.location ) ) {
            return false;
        }
        if ( this.maxFileSize == null ) {
            if ( other.maxFileSize != null ) {
                return false;
            }
        } else if ( !this.maxFileSize.equals ( other.maxFileSize ) ) {
            return false;
        }
        if ( this.maxRequestSize == null ) {
            if ( other.maxRequestSize != null ) {
                return false;
            }
        } else if ( !this.maxRequestSize.equals ( other.maxRequestSize ) ) {
            return false;
        }
        return true;
    }
}

package org.apache.tomcat.util.descriptor.web;
import org.apache.tomcat.util.buf.UDecoder;
import java.io.Serializable;
public class ErrorPage implements Serializable {
    private static final long serialVersionUID = 1L;
    private int errorCode;
    private String exceptionType;
    private String location;
    public ErrorPage() {
        this.errorCode = 0;
        this.exceptionType = null;
        this.location = null;
    }
    public int getErrorCode() {
        return this.errorCode;
    }
    public void setErrorCode ( final int errorCode ) {
        this.errorCode = errorCode;
    }
    public void setErrorCode ( final String errorCode ) {
        try {
            this.errorCode = Integer.parseInt ( errorCode );
        } catch ( NumberFormatException nfe ) {
            throw new IllegalArgumentException ( nfe );
        }
    }
    public String getExceptionType() {
        return this.exceptionType;
    }
    public void setExceptionType ( final String exceptionType ) {
        this.exceptionType = exceptionType;
    }
    public String getLocation() {
        return this.location;
    }
    public void setLocation ( final String location ) {
        this.location = UDecoder.URLDecode ( location );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ErrorPage[" );
        if ( this.exceptionType == null ) {
            sb.append ( "errorCode=" );
            sb.append ( this.errorCode );
        } else {
            sb.append ( "exceptionType=" );
            sb.append ( this.exceptionType );
        }
        sb.append ( ", location=" );
        sb.append ( this.location );
        sb.append ( "]" );
        return sb.toString();
    }
    public String getName() {
        if ( this.exceptionType == null ) {
            return Integer.toString ( this.errorCode );
        }
        return this.exceptionType;
    }
}

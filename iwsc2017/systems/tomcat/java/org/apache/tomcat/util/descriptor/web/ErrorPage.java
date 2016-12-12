package org.apache.tomcat.util.descriptor.web;
import java.io.Serializable;
import org.apache.tomcat.util.buf.UDecoder;
public class ErrorPage implements Serializable {
    private static final long serialVersionUID = 1L;
    private int errorCode = 0;
    private String exceptionType = null;
    private String location = null;
    public int getErrorCode() {
        return ( this.errorCode );
    }
    public void setErrorCode ( int errorCode ) {
        this.errorCode = errorCode;
    }
    public void setErrorCode ( String errorCode ) {
        try {
            this.errorCode = Integer.parseInt ( errorCode );
        } catch ( NumberFormatException nfe ) {
            throw new IllegalArgumentException ( nfe );
        }
    }
    public String getExceptionType() {
        return ( this.exceptionType );
    }
    public void setExceptionType ( String exceptionType ) {
        this.exceptionType = exceptionType;
    }
    public String getLocation() {
        return ( this.location );
    }
    public void setLocation ( String location ) {
        this.location = UDecoder.URLDecode ( location );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ErrorPage[" );
        if ( exceptionType == null ) {
            sb.append ( "errorCode=" );
            sb.append ( errorCode );
        } else {
            sb.append ( "exceptionType=" );
            sb.append ( exceptionType );
        }
        sb.append ( ", location=" );
        sb.append ( location );
        sb.append ( "]" );
        return ( sb.toString() );
    }
    public String getName() {
        if ( exceptionType == null ) {
            return Integer.toString ( errorCode );
        } else {
            return exceptionType;
        }
    }
}

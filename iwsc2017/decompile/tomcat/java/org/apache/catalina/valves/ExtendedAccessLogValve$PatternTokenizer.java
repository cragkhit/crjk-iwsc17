package org.apache.catalina.valves;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.io.StringReader;
protected static class PatternTokenizer {
    private final StringReader sr;
    private StringBuilder buf;
    private boolean ended;
    private boolean subToken;
    private boolean parameter;
    public PatternTokenizer ( final String str ) {
        this.buf = new StringBuilder();
        this.ended = false;
        this.sr = new StringReader ( str );
    }
    public boolean hasSubToken() {
        return this.subToken;
    }
    public boolean hasParameter() {
        return this.parameter;
    }
    public String getToken() throws IOException {
        if ( this.ended ) {
            return null;
        }
        String result = null;
        this.subToken = false;
        this.parameter = false;
        for ( int c = this.sr.read(); c != -1; c = this.sr.read() ) {
            switch ( c ) {
            case 32: {
                result = this.buf.toString();
                ( this.buf = new StringBuilder() ).append ( ( char ) c );
                return result;
            }
            case 45: {
                result = this.buf.toString();
                this.buf = new StringBuilder();
                this.subToken = true;
                return result;
            }
            case 40: {
                result = this.buf.toString();
                this.buf = new StringBuilder();
                this.parameter = true;
                return result;
            }
            case 41: {
                result = this.buf.toString();
                this.buf = new StringBuilder();
                break;
            }
            default: {
                this.buf.append ( ( char ) c );
                break;
            }
            }
        }
        this.ended = true;
        if ( this.buf.length() != 0 ) {
            return this.buf.toString();
        }
        return null;
    }
    public String getParameter() throws IOException {
        if ( !this.parameter ) {
            return null;
        }
        this.parameter = false;
        for ( int c = this.sr.read(); c != -1; c = this.sr.read() ) {
            if ( c == 41 ) {
                final String result = this.buf.toString();
                this.buf = new StringBuilder();
                return result;
            }
            this.buf.append ( ( char ) c );
        }
        return null;
    }
    public String getWhiteSpaces() throws IOException {
        if ( this.isEnded() ) {
            return "";
        }
        final StringBuilder whiteSpaces = new StringBuilder();
        if ( this.buf.length() > 0 ) {
            whiteSpaces.append ( ( CharSequence ) this.buf );
            this.buf = new StringBuilder();
        }
        int c;
        for ( c = this.sr.read(); Character.isWhitespace ( ( char ) c ); c = this.sr.read() ) {
            whiteSpaces.append ( ( char ) c );
        }
        if ( c == -1 ) {
            this.ended = true;
        } else {
            this.buf.append ( ( char ) c );
        }
        return whiteSpaces.toString();
    }
    public boolean isEnded() {
        return this.ended;
    }
    public String getRemains() throws IOException {
        final StringBuilder remains = new StringBuilder();
        for ( int c = this.sr.read(); c != -1; c = this.sr.read() ) {
            remains.append ( ( char ) c );
        }
        return remains.toString();
    }
}

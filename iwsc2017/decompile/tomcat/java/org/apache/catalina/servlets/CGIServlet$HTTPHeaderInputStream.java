package org.apache.catalina.servlets;
import java.io.IOException;
import java.io.InputStream;
protected static class HTTPHeaderInputStream extends InputStream {
    private static final int STATE_CHARACTER = 0;
    private static final int STATE_FIRST_CR = 1;
    private static final int STATE_FIRST_LF = 2;
    private static final int STATE_SECOND_CR = 3;
    private static final int STATE_HEADER_END = 4;
    private final InputStream input;
    private int state;
    HTTPHeaderInputStream ( final InputStream theInput ) {
        this.input = theInput;
        this.state = 0;
    }
    @Override
    public int read() throws IOException {
        if ( this.state == 4 ) {
            return -1;
        }
        final int i = this.input.read();
        if ( i == 10 ) {
            switch ( this.state ) {
            case 0: {
                this.state = 2;
                break;
            }
            case 1: {
                this.state = 2;
                break;
            }
            case 2:
            case 3: {
                this.state = 4;
                break;
            }
            }
        } else if ( i == 13 ) {
            switch ( this.state ) {
            case 0: {
                this.state = 1;
                break;
            }
            case 1: {
                this.state = 4;
                break;
            }
            case 2: {
                this.state = 3;
                break;
            }
            }
        } else {
            this.state = 0;
        }
        return i;
    }
}

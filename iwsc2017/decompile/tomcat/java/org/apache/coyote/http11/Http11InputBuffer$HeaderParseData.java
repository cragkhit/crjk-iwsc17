package org.apache.coyote.http11;
import org.apache.tomcat.util.buf.MessageBytes;
private static class HeaderParseData {
    int start;
    int realPos;
    int lastSignificantChar;
    MessageBytes headerValue;
    private HeaderParseData() {
        this.start = 0;
        this.realPos = 0;
        this.lastSignificantChar = 0;
        this.headerValue = null;
    }
    public void recycle() {
        this.start = 0;
        this.realPos = 0;
        this.lastSignificantChar = 0;
        this.headerValue = null;
    }
}

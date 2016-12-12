package org.apache.tomcat.util.http;
import org.apache.tomcat.util.buf.MessageBytes;
class MimeHeaderField {
    private final MessageBytes nameB;
    private final MessageBytes valueB;
    public MimeHeaderField() {
        this.nameB = MessageBytes.newInstance();
        this.valueB = MessageBytes.newInstance();
    }
    public void recycle() {
        this.nameB.recycle();
        this.valueB.recycle();
    }
    public MessageBytes getName() {
        return this.nameB;
    }
    public MessageBytes getValue() {
        return this.valueB;
    }
}

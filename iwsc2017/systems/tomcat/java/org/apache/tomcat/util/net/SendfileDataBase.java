package org.apache.tomcat.util.net;
public abstract class SendfileDataBase {
    public boolean keepAlive;
    public final String fileName;
    public long pos;
    public long length;
    public SendfileDataBase ( String filename, long pos, long length ) {
        this.fileName = filename;
        this.pos = pos;
        this.length = length;
    }
}

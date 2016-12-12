package org.apache.tomcat.util.net;
import java.nio.channels.FileChannel;
public static class SendfileData extends SendfileDataBase {
    private FileChannel fchannel;
    private boolean doneInline;
    private boolean error;
    public SendfileData ( final String filename, final long pos, final long length ) {
        super ( filename, pos, length );
        this.doneInline = false;
        this.error = false;
    }
}

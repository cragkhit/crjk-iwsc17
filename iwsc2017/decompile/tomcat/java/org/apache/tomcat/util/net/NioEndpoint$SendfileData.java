package org.apache.tomcat.util.net;
import java.nio.channels.FileChannel;
public static class SendfileData extends SendfileDataBase {
    protected volatile FileChannel fchannel;
    public SendfileData ( final String filename, final long pos, final long length ) {
        super ( filename, pos, length );
    }
}

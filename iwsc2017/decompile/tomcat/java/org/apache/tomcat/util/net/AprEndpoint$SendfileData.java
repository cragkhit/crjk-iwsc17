package org.apache.tomcat.util.net;
public static class SendfileData extends SendfileDataBase {
    protected long fd;
    protected long fdpool;
    protected long socket;
    public SendfileData ( final String filename, final long pos, final long length ) {
        super ( filename, pos, length );
    }
}

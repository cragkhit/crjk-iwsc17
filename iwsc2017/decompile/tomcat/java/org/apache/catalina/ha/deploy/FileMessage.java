package org.apache.catalina.ha.deploy;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.ha.ClusterMessageBase;
public class FileMessage extends ClusterMessageBase {
    private static final long serialVersionUID = 2L;
    private int messageNumber;
    private byte[] data;
    private int dataLength;
    private long totalNrOfMsgs;
    private final String fileName;
    private final String contextName;
    public FileMessage ( final Member source, final String fileName, final String contextName ) {
        this.address = source;
        this.fileName = fileName;
        this.contextName = contextName;
    }
    public int getMessageNumber() {
        return this.messageNumber;
    }
    public void setMessageNumber ( final int messageNumber ) {
        this.messageNumber = messageNumber;
    }
    public long getTotalNrOfMsgs() {
        return this.totalNrOfMsgs;
    }
    public void setTotalNrOfMsgs ( final long totalNrOfMsgs ) {
        this.totalNrOfMsgs = totalNrOfMsgs;
    }
    public byte[] getData() {
        return this.data;
    }
    public void setData ( final byte[] data, final int length ) {
        this.data = data;
        this.dataLength = length;
    }
    public int getDataLength() {
        return this.dataLength;
    }
    @Override
    public String getUniqueId() {
        final StringBuilder result = new StringBuilder ( this.getFileName() );
        result.append ( "#-#" );
        result.append ( this.getMessageNumber() );
        result.append ( "#-#" );
        result.append ( System.currentTimeMillis() );
        return result.toString();
    }
    public String getFileName() {
        return this.fileName;
    }
    public String getContextName() {
        return this.contextName;
    }
}

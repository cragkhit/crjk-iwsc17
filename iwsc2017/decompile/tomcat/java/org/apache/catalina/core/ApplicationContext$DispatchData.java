package org.apache.catalina.core;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.catalina.mapper.MappingData;
import org.apache.tomcat.util.buf.MessageBytes;
private static final class DispatchData {
    public MessageBytes uriMB;
    public MappingData mappingData;
    public DispatchData() {
        this.uriMB = MessageBytes.newInstance();
        final CharChunk uriCC = this.uriMB.getCharChunk();
        uriCC.setLimit ( -1 );
        this.mappingData = new MappingData();
    }
}

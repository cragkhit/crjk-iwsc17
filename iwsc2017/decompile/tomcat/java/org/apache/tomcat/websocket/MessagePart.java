package org.apache.tomcat.websocket;
import javax.websocket.SendHandler;
import java.nio.ByteBuffer;
class MessagePart {
    private final boolean fin;
    private final int rsv;
    private final byte opCode;
    private final ByteBuffer payload;
    private final SendHandler intermediateHandler;
    private volatile SendHandler endHandler;
    private final long blockingWriteTimeoutExpiry;
    public MessagePart ( final boolean fin, final int rsv, final byte opCode, final ByteBuffer payload, final SendHandler intermediateHandler, final SendHandler endHandler, final long blockingWriteTimeoutExpiry ) {
        this.fin = fin;
        this.rsv = rsv;
        this.opCode = opCode;
        this.payload = payload;
        this.intermediateHandler = intermediateHandler;
        this.endHandler = endHandler;
        this.blockingWriteTimeoutExpiry = blockingWriteTimeoutExpiry;
    }
    public boolean isFin() {
        return this.fin;
    }
    public int getRsv() {
        return this.rsv;
    }
    public byte getOpCode() {
        return this.opCode;
    }
    public ByteBuffer getPayload() {
        return this.payload;
    }
    public SendHandler getIntermediateHandler() {
        return this.intermediateHandler;
    }
    public SendHandler getEndHandler() {
        return this.endHandler;
    }
    public void setEndHandler ( final SendHandler endHandler ) {
        this.endHandler = endHandler;
    }
    public long getBlockingWriteTimeoutExpiry() {
        return this.blockingWriteTimeoutExpiry;
    }
}

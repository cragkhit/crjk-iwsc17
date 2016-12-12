package org.apache.tomcat.websocket;
import javax.websocket.Extension;
private abstract class TerminalTransformation implements Transformation {
    @Override
    public boolean validateRsvBits ( final int i ) {
        return true;
    }
    @Override
    public Extension getExtensionResponse() {
        return null;
    }
    @Override
    public void setNext ( final Transformation t ) {
    }
    @Override
    public boolean validateRsv ( final int rsv, final byte opCode ) {
        return rsv == 0;
    }
    @Override
    public void close() {
    }
}

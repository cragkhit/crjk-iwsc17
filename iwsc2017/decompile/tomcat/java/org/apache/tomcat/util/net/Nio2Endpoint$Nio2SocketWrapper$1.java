package org.apache.tomcat.util.net;
import java.util.concurrent.atomic.AtomicInteger;
static final class Nio2Endpoint$Nio2SocketWrapper$1 extends ThreadLocal<AtomicInteger> {
    @Override
    protected AtomicInteger initialValue() {
        return new AtomicInteger ( 0 );
    }
}

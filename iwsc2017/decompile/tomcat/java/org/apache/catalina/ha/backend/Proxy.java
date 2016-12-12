package org.apache.catalina.ha.backend;
import java.net.InetAddress;
public class Proxy {
    public InetAddress address;
    public int port;
    public Proxy() {
        this.address = null;
        this.port = 80;
    }
}

package org.apache.catalina.startup;
import org.apache.tomcat.util.descriptor.web.WebXml;
private static class DefaultWebXmlCacheEntry {
    private final WebXml webXml;
    private final long globalTimeStamp;
    private final long hostTimeStamp;
    public DefaultWebXmlCacheEntry ( final WebXml webXml, final long globalTimeStamp, final long hostTimeStamp ) {
        this.webXml = webXml;
        this.globalTimeStamp = globalTimeStamp;
        this.hostTimeStamp = hostTimeStamp;
    }
    public WebXml getWebXml() {
        return this.webXml;
    }
    public long getGlobalTimeStamp() {
        return this.globalTimeStamp;
    }
    public long getHostTimeStamp() {
        return this.hostTimeStamp;
    }
}

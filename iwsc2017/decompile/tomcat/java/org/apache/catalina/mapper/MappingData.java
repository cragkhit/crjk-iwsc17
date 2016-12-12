package org.apache.catalina.mapper;
import javax.servlet.http.MappingMatch;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.catalina.Wrapper;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
public class MappingData {
    public Host host;
    public Context context;
    public int contextSlashCount;
    public Context[] contexts;
    public Wrapper wrapper;
    public boolean jspWildCard;
    public final MessageBytes contextPath;
    public final MessageBytes requestPath;
    public final MessageBytes wrapperPath;
    public final MessageBytes pathInfo;
    public final MessageBytes redirectPath;
    public MappingMatch matchType;
    public MappingData() {
        this.host = null;
        this.context = null;
        this.contextSlashCount = 0;
        this.contexts = null;
        this.wrapper = null;
        this.jspWildCard = false;
        this.contextPath = MessageBytes.newInstance();
        this.requestPath = MessageBytes.newInstance();
        this.wrapperPath = MessageBytes.newInstance();
        this.pathInfo = MessageBytes.newInstance();
        this.redirectPath = MessageBytes.newInstance();
        this.matchType = MappingMatch.UNKNOWN;
    }
    public void recycle() {
        this.host = null;
        this.context = null;
        this.contextSlashCount = 0;
        this.contexts = null;
        this.wrapper = null;
        this.jspWildCard = false;
        this.contextPath.recycle();
        this.requestPath.recycle();
        this.wrapperPath.recycle();
        this.pathInfo.recycle();
        this.redirectPath.recycle();
        this.matchType = MappingMatch.UNKNOWN;
    }
}

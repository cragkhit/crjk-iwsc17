package javax.servlet.http;
public interface HttpUpgradeHandler {
    void init ( WebConnection p0 );
    void destroy();
}

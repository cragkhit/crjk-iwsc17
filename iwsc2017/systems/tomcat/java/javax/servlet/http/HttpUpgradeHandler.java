package javax.servlet.http;
public interface HttpUpgradeHandler {
    void init ( WebConnection connection );
    void destroy();
}

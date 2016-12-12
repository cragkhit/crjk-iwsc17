package org.apache.coyote;
import org.apache.tomcat.util.net.SSLHostConfig;
import java.util.concurrent.Executor;
public interface ProtocolHandler {
    void setAdapter ( Adapter p0 );
    Adapter getAdapter();
    Executor getExecutor();
    void init() throws Exception;
    void start() throws Exception;
    void pause() throws Exception;
    void resume() throws Exception;
    void stop() throws Exception;
    void destroy() throws Exception;
    boolean isAprRequired();
    boolean isSendfileSupported();
    void addSslHostConfig ( SSLHostConfig p0 );
    SSLHostConfig[] findSslHostConfigs();
    void addUpgradeProtocol ( UpgradeProtocol p0 );
    UpgradeProtocol[] findUpgradeProtocols();
}

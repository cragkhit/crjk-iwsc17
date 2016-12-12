package org.apache.coyote;
import java.util.concurrent.Executor;
import org.apache.tomcat.util.net.SSLHostConfig;
public interface ProtocolHandler {
    public void setAdapter ( Adapter adapter );
    public Adapter getAdapter();
    public Executor getExecutor();
    public void init() throws Exception;
    public void start() throws Exception;
    public void pause() throws Exception;
    public void resume() throws Exception;
    public void stop() throws Exception;
    public void destroy() throws Exception;
    public boolean isAprRequired();
    public boolean isSendfileSupported();
    public void addSslHostConfig ( SSLHostConfig sslHostConfig );
    public SSLHostConfig[] findSslHostConfigs();
    public void addUpgradeProtocol ( UpgradeProtocol upgradeProtocol );
    public UpgradeProtocol[] findUpgradeProtocols();
}

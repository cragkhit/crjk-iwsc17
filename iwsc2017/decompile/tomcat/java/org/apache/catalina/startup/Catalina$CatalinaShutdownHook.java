package org.apache.catalina.startup;
import org.apache.juli.ClassLoaderLogManager;
import java.util.logging.LogManager;
import org.apache.tomcat.util.ExceptionUtils;
protected class CatalinaShutdownHook extends Thread {
    @Override
    public void run() {
        try {
            if ( Catalina.this.getServer() != null ) {
                Catalina.this.stop();
            }
        } catch ( Throwable ex ) {
            ExceptionUtils.handleThrowable ( ex );
            Catalina.access$000().error ( Catalina.sm.getString ( "catalina.shutdownHookFail" ), ex );
        } finally {
            final LogManager logManager = LogManager.getLogManager();
            if ( logManager instanceof ClassLoaderLogManager ) {
                ( ( ClassLoaderLogManager ) logManager ).shutdown();
            }
        }
    }
}

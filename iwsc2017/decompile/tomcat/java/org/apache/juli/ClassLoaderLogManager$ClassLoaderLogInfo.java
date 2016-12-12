package org.apache.juli;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.Map;
protected static final class ClassLoaderLogInfo {
    final LogNode rootNode;
    final Map<String, Logger> loggers;
    final Map<String, Handler> handlers;
    final Properties props;
    ClassLoaderLogInfo ( final LogNode rootNode ) {
        this.loggers = new ConcurrentHashMap<String, Logger>();
        this.handlers = new HashMap<String, Handler>();
        this.props = new Properties();
        this.rootNode = rootNode;
    }
}

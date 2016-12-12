package org.apache.catalina.tribes.util;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
public class Logs {
    public static final Log MESSAGES;
    static {
        MESSAGES = LogFactory.getLog ( "org.apache.catalina.tribes.MESSAGES" );
    }
}

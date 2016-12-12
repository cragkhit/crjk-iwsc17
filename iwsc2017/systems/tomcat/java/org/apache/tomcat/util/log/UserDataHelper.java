package org.apache.tomcat.util.log;
import org.apache.juli.logging.Log;
public class UserDataHelper {
    private final Log log;
    private final Config config;
    private final long suppressionTime;
    private volatile long lastInfoTime = 0;
    public UserDataHelper ( Log log ) {
        this.log = log;
        Config tempConfig;
        String configString = System.getProperty (
                                  "org.apache.juli.logging.UserDataHelper.CONFIG" );
        if ( configString == null ) {
            tempConfig = Config.INFO_THEN_DEBUG;
        } else {
            try {
                tempConfig = Config.valueOf ( configString );
            } catch ( IllegalArgumentException iae ) {
                tempConfig = Config.INFO_THEN_DEBUG;
            }
        }
        suppressionTime = Integer.getInteger (
                              "org.apache.juli.logging.UserDataHelper.SUPPRESSION_TIME",
                              60 * 60 * 24 ).intValue() * 1000L;
        if ( suppressionTime == 0 ) {
            tempConfig = Config.INFO_ALL;
        }
        config = tempConfig;
    }
    public Mode getNextMode() {
        if ( Config.NONE == config ) {
            return null;
        } else if ( Config.DEBUG_ALL == config ) {
            return log.isDebugEnabled() ? Mode.DEBUG : null;
        } else if ( Config.INFO_THEN_DEBUG == config ) {
            if ( logAtInfo() ) {
                return log.isInfoEnabled() ? Mode.INFO_THEN_DEBUG : null;
            } else {
                return log.isDebugEnabled() ? Mode.DEBUG : null;
            }
        } else if ( Config.INFO_ALL == config ) {
            return log.isInfoEnabled() ? Mode.INFO : null;
        }
        return null;
    }
    private boolean logAtInfo() {
        if ( suppressionTime < 0 && lastInfoTime > 0 ) {
            return false;
        }
        long now = System.currentTimeMillis();
        if ( lastInfoTime + suppressionTime > now ) {
            return false;
        }
        lastInfoTime = now;
        return true;
    }
    private static enum Config {
        NONE,
        DEBUG_ALL,
        INFO_THEN_DEBUG,
        INFO_ALL
    }
    public static enum Mode {
        DEBUG,
        INFO_THEN_DEBUG,
        INFO
    }
}

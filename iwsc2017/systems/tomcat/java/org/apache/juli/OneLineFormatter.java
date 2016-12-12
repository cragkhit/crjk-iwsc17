package org.apache.juli;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
public class OneLineFormatter extends Formatter {
    private static final String ST_SEP = System.lineSeparator() + " ";
    private static final String UNKONWN_THREAD_NAME = "Unknown thread with ID ";
    private static final Object threadMxBeanLock = new Object();
    private static volatile ThreadMXBean threadMxBean = null;
    private static final int THREAD_NAME_CACHE_SIZE = 10000;
    private static ThreadLocal<LinkedHashMap<Integer, String>> threadNameCache =
    new ThreadLocal<LinkedHashMap<Integer, String>>() {
        @Override
        protected LinkedHashMap<Integer, String> initialValue() {
            return new LinkedHashMap<Integer, String>() {
                private static final long serialVersionUID = 1L;
                @Override
                protected boolean removeEldestEntry (
                    Entry<Integer, String> eldest ) {
                    return ( size() > THREAD_NAME_CACHE_SIZE );
                }
            };
        }
    };
    private static final String DEFAULT_TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    private static final int globalCacheSize = 30;
    private static final int localCacheSize = 5;
    private ThreadLocal<DateFormatCache> localDateCache;
    public OneLineFormatter() {
        String timeFormat = LogManager.getLogManager().getProperty (
                                OneLineFormatter.class.getName() + ".timeFormat" );
        if ( timeFormat == null ) {
            timeFormat = DEFAULT_TIME_FORMAT;
        }
        setTimeFormat ( timeFormat );
    }
    public void setTimeFormat ( String timeFormat ) {
        DateFormatCache globalDateCache = new DateFormatCache ( globalCacheSize, timeFormat, null );
        localDateCache = new ThreadLocal<DateFormatCache>() {
            @Override
            protected DateFormatCache initialValue() {
                return new DateFormatCache ( localCacheSize, timeFormat, globalDateCache );
            }
        };
    }
    public String getTimeFormat() {
        return localDateCache.get().getTimeFormat();
    }
    @Override
    public String format ( LogRecord record ) {
        StringBuilder sb = new StringBuilder();
        addTimestamp ( sb, record.getMillis() );
        sb.append ( ' ' );
        sb.append ( record.getLevel().getLocalizedName() );
        sb.append ( ' ' );
        sb.append ( '[' );
        if ( Thread.currentThread() instanceof AsyncFileHandler.LoggerThread ) {
            sb.append ( getThreadName ( record.getThreadID() ) );
        } else {
            sb.append ( Thread.currentThread().getName() );
        }
        sb.append ( ']' );
        sb.append ( ' ' );
        sb.append ( record.getSourceClassName() );
        sb.append ( '.' );
        sb.append ( record.getSourceMethodName() );
        sb.append ( ' ' );
        sb.append ( formatMessage ( record ) );
        if ( record.getThrown() != null ) {
            sb.append ( ST_SEP );
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter ( sw );
            record.getThrown().printStackTrace ( pw );
            pw.close();
            sb.append ( sw.getBuffer() );
        }
        sb.append ( System.lineSeparator() );
        return sb.toString();
    }
    protected void addTimestamp ( StringBuilder buf, long timestamp ) {
        buf.append ( localDateCache.get().getFormat ( timestamp ) );
        long frac = timestamp % 1000;
        buf.append ( '.' );
        if ( frac < 100 ) {
            if ( frac < 10 ) {
                buf.append ( '0' );
                buf.append ( '0' );
            } else {
                buf.append ( '0' );
            }
        }
        buf.append ( frac );
    }
    private static String getThreadName ( int logRecordThreadId ) {
        Map<Integer, String> cache = threadNameCache.get();
        String result = null;
        if ( logRecordThreadId > ( Integer.MAX_VALUE / 2 ) ) {
            result = cache.get ( Integer.valueOf ( logRecordThreadId ) );
        }
        if ( result != null ) {
            return result;
        }
        if ( logRecordThreadId > Integer.MAX_VALUE / 2 ) {
            result = UNKONWN_THREAD_NAME + logRecordThreadId;
        } else {
            if ( threadMxBean == null ) {
                synchronized ( threadMxBeanLock ) {
                    if ( threadMxBean == null ) {
                        threadMxBean = ManagementFactory.getThreadMXBean();
                    }
                }
            }
            ThreadInfo threadInfo =
                threadMxBean.getThreadInfo ( logRecordThreadId );
            if ( threadInfo == null ) {
                return Long.toString ( logRecordThreadId );
            }
            result = threadInfo.getThreadName();
        }
        cache.put ( Integer.valueOf ( logRecordThreadId ), result );
        return result;
    }
}

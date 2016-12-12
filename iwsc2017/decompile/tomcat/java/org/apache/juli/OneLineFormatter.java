package org.apache.juli;
import java.lang.management.ThreadInfo;
import java.util.Map;
import java.lang.management.ManagementFactory;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;
import java.util.logging.LogManager;
import java.util.LinkedHashMap;
import java.lang.management.ThreadMXBean;
import java.util.logging.Formatter;
public class OneLineFormatter extends Formatter {
    private static final String ST_SEP;
    private static final String UNKONWN_THREAD_NAME = "Unknown thread with ID ";
    private static final Object threadMxBeanLock;
    private static volatile ThreadMXBean threadMxBean;
    private static final int THREAD_NAME_CACHE_SIZE = 10000;
    private static ThreadLocal<LinkedHashMap<Integer, String>> threadNameCache;
    private static final String DEFAULT_TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    private static final int globalCacheSize = 30;
    private static final int localCacheSize = 5;
    private ThreadLocal<DateFormatCache> localDateCache;
    public OneLineFormatter() {
        String timeFormat = LogManager.getLogManager().getProperty ( OneLineFormatter.class.getName() + ".timeFormat" );
        if ( timeFormat == null ) {
            timeFormat = "dd-MMM-yyyy HH:mm:ss";
        }
        this.setTimeFormat ( timeFormat );
    }
    public void setTimeFormat ( final String timeFormat ) {
        final DateFormatCache globalDateCache = new DateFormatCache ( 30, timeFormat, null );
        this.localDateCache = new ThreadLocal<DateFormatCache>() {
            @Override
            protected DateFormatCache initialValue() {
                return new DateFormatCache ( 5, timeFormat, globalDateCache );
            }
        };
    }
    public String getTimeFormat() {
        return this.localDateCache.get().getTimeFormat();
    }
    @Override
    public String format ( final LogRecord record ) {
        final StringBuilder sb = new StringBuilder();
        this.addTimestamp ( sb, record.getMillis() );
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
        sb.append ( this.formatMessage ( record ) );
        if ( record.getThrown() != null ) {
            sb.append ( OneLineFormatter.ST_SEP );
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter ( sw );
            record.getThrown().printStackTrace ( pw );
            pw.close();
            sb.append ( sw.getBuffer() );
        }
        sb.append ( System.lineSeparator() );
        return sb.toString();
    }
    protected void addTimestamp ( final StringBuilder buf, final long timestamp ) {
        buf.append ( this.localDateCache.get().getFormat ( timestamp ) );
        final long frac = timestamp % 1000L;
        buf.append ( '.' );
        if ( frac < 100L ) {
            if ( frac < 10L ) {
                buf.append ( '0' );
                buf.append ( '0' );
            } else {
                buf.append ( '0' );
            }
        }
        buf.append ( frac );
    }
    private static String getThreadName ( final int logRecordThreadId ) {
        final Map<Integer, String> cache = OneLineFormatter.threadNameCache.get();
        String result = null;
        if ( logRecordThreadId > 1073741823 ) {
            result = cache.get ( logRecordThreadId );
        }
        if ( result != null ) {
            return result;
        }
        if ( logRecordThreadId > 1073741823 ) {
            result = "Unknown thread with ID " + logRecordThreadId;
        } else {
            if ( OneLineFormatter.threadMxBean == null ) {
                synchronized ( OneLineFormatter.threadMxBeanLock ) {
                    if ( OneLineFormatter.threadMxBean == null ) {
                        OneLineFormatter.threadMxBean = ManagementFactory.getThreadMXBean();
                    }
                }
            }
            final ThreadInfo threadInfo = OneLineFormatter.threadMxBean.getThreadInfo ( logRecordThreadId );
            if ( threadInfo == null ) {
                return Long.toString ( logRecordThreadId );
            }
            result = threadInfo.getThreadName();
        }
        cache.put ( logRecordThreadId, result );
        return result;
    }
    static {
        ST_SEP = System.lineSeparator() + " ";
        threadMxBeanLock = new Object();
        OneLineFormatter.threadMxBean = null;
        OneLineFormatter.threadNameCache = new ThreadLocal<LinkedHashMap<Integer, String>>() {
            @Override
            protected LinkedHashMap<Integer, String> initialValue() {
                return new LinkedHashMap<Integer, String>() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected boolean removeEldestEntry ( final Map.Entry<Integer, String> eldest ) {
                        return this.size() > 10000;
                    }
                };
            }
        };
    }
}

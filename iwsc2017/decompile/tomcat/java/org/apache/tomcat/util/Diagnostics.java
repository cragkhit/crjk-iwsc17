package org.apache.tomcat.util;
import java.util.logging.LogManager;
import java.lang.management.ManagementFactory;
import org.apache.juli.logging.LogFactory;
import java.util.Map;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.management.MemoryUsage;
import java.util.Date;
import java.util.Locale;
import java.util.Enumeration;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.Iterator;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.util.List;
import java.lang.management.MemoryMXBean;
import java.util.logging.LoggingMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ClassLoadingMXBean;
import java.text.SimpleDateFormat;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
public class Diagnostics {
    private static final String PACKAGE = "org.apache.tomcat.util";
    private static final StringManager sm;
    private static final String INDENT1 = "  ";
    private static final String INDENT2 = "\t";
    private static final String INDENT3 = "   ";
    private static final String CRLF = "\r\n";
    private static final String vminfoSystemProperty = "java.vm.info";
    private static final Log log;
    private static final SimpleDateFormat timeformat;
    private static final ClassLoadingMXBean classLoadingMXBean;
    private static final CompilationMXBean compilationMXBean;
    private static final OperatingSystemMXBean operatingSystemMXBean;
    private static final RuntimeMXBean runtimeMXBean;
    private static final ThreadMXBean threadMXBean;
    private static final LoggingMXBean loggingMXBean;
    private static final MemoryMXBean memoryMXBean;
    private static final List<GarbageCollectorMXBean> garbageCollectorMXBeans;
    private static final List<MemoryManagerMXBean> memoryManagerMXBeans;
    private static final List<MemoryPoolMXBean> memoryPoolMXBeans;
    public static boolean isThreadContentionMonitoringEnabled() {
        return Diagnostics.threadMXBean.isThreadContentionMonitoringEnabled();
    }
    public static void setThreadContentionMonitoringEnabled ( final boolean enable ) {
        Diagnostics.threadMXBean.setThreadContentionMonitoringEnabled ( enable );
        final boolean checkValue = Diagnostics.threadMXBean.isThreadContentionMonitoringEnabled();
        if ( enable != checkValue ) {
            Diagnostics.log.error ( "Could not set threadContentionMonitoringEnabled to " + enable + ", got " + checkValue + " instead" );
        }
    }
    public static boolean isThreadCpuTimeEnabled() {
        return Diagnostics.threadMXBean.isThreadCpuTimeEnabled();
    }
    public static void setThreadCpuTimeEnabled ( final boolean enable ) {
        Diagnostics.threadMXBean.setThreadCpuTimeEnabled ( enable );
        final boolean checkValue = Diagnostics.threadMXBean.isThreadCpuTimeEnabled();
        if ( enable != checkValue ) {
            Diagnostics.log.error ( "Could not set threadCpuTimeEnabled to " + enable + ", got " + checkValue + " instead" );
        }
    }
    public static void resetPeakThreadCount() {
        Diagnostics.threadMXBean.resetPeakThreadCount();
    }
    public static void setVerboseClassLoading ( final boolean verbose ) {
        Diagnostics.classLoadingMXBean.setVerbose ( verbose );
        final boolean checkValue = Diagnostics.classLoadingMXBean.isVerbose();
        if ( verbose != checkValue ) {
            Diagnostics.log.error ( "Could not set verbose class loading to " + verbose + ", got " + checkValue + " instead" );
        }
    }
    public static void setLoggerLevel ( final String loggerName, final String levelName ) {
        Diagnostics.loggingMXBean.setLoggerLevel ( loggerName, levelName );
        final String checkValue = Diagnostics.loggingMXBean.getLoggerLevel ( loggerName );
        if ( !checkValue.equals ( levelName ) ) {
            Diagnostics.log.error ( "Could not set logger level for logger '" + loggerName + "' to '" + levelName + "', got '" + checkValue + "' instead" );
        }
    }
    public static void setVerboseGarbageCollection ( final boolean verbose ) {
        Diagnostics.memoryMXBean.setVerbose ( verbose );
        final boolean checkValue = Diagnostics.memoryMXBean.isVerbose();
        if ( verbose != checkValue ) {
            Diagnostics.log.error ( "Could not set verbose garbage collection logging to " + verbose + ", got " + checkValue + " instead" );
        }
    }
    public static void gc() {
        Diagnostics.memoryMXBean.gc();
    }
    public static void resetPeakUsage ( final String name ) {
        for ( final MemoryPoolMXBean mbean : Diagnostics.memoryPoolMXBeans ) {
            if ( name.equals ( "all" ) || name.equals ( mbean.getName() ) ) {
                mbean.resetPeakUsage();
            }
        }
    }
    public static boolean setUsageThreshold ( final String name, final long threshold ) {
        for ( final MemoryPoolMXBean mbean : Diagnostics.memoryPoolMXBeans ) {
            if ( name.equals ( mbean.getName() ) ) {
                try {
                    mbean.setUsageThreshold ( threshold );
                    return true;
                } catch ( IllegalArgumentException ex ) {}
                catch ( UnsupportedOperationException ex2 ) {}
                return false;
            }
        }
        return false;
    }
    public static boolean setCollectionUsageThreshold ( final String name, final long threshold ) {
        for ( final MemoryPoolMXBean mbean : Diagnostics.memoryPoolMXBeans ) {
            if ( name.equals ( mbean.getName() ) ) {
                try {
                    mbean.setCollectionUsageThreshold ( threshold );
                    return true;
                } catch ( IllegalArgumentException ex ) {}
                catch ( UnsupportedOperationException ex2 ) {}
                return false;
            }
        }
        return false;
    }
    private static String getThreadDumpHeader ( final ThreadInfo ti ) {
        final StringBuilder sb = new StringBuilder ( "\"" + ti.getThreadName() + "\"" );
        sb.append ( " Id=" + ti.getThreadId() );
        sb.append ( " cpu=" + Diagnostics.threadMXBean.getThreadCpuTime ( ti.getThreadId() ) + " ns" );
        sb.append ( " usr=" + Diagnostics.threadMXBean.getThreadUserTime ( ti.getThreadId() ) + " ns" );
        sb.append ( " blocked " + ti.getBlockedCount() + " for " + ti.getBlockedTime() + " ms" );
        sb.append ( " waited " + ti.getWaitedCount() + " for " + ti.getWaitedTime() + " ms" );
        if ( ti.isSuspended() ) {
            sb.append ( " (suspended)" );
        }
        if ( ti.isInNative() ) {
            sb.append ( " (running in native)" );
        }
        sb.append ( "\r\n" );
        sb.append ( "   java.lang.Thread.State: " + ti.getThreadState() );
        sb.append ( "\r\n" );
        return sb.toString();
    }
    private static String getThreadDump ( final ThreadInfo ti ) {
        final StringBuilder sb = new StringBuilder ( getThreadDumpHeader ( ti ) );
        for ( final LockInfo li : ti.getLockedSynchronizers() ) {
            sb.append ( "\tlocks " + li.toString() + "\r\n" );
        }
        boolean start = true;
        final StackTraceElement[] stes = ti.getStackTrace();
        final Object[] monitorDepths = new Object[stes.length];
        final MonitorInfo[] mis = ti.getLockedMonitors();
        for ( int i = 0; i < mis.length; ++i ) {
            monitorDepths[mis[i].getLockedStackDepth()] = mis[i];
        }
        for ( int i = 0; i < stes.length; ++i ) {
            final StackTraceElement ste = stes[i];
            sb.append ( "\tat " + ste.toString() + "\r\n" );
            if ( start ) {
                if ( ti.getLockName() != null ) {
                    sb.append ( "\t- waiting on (a " + ti.getLockName() + ")" );
                    if ( ti.getLockOwnerName() != null ) {
                        sb.append ( " owned by " + ti.getLockOwnerName() + " Id=" + ti.getLockOwnerId() );
                    }
                    sb.append ( "\r\n" );
                }
                start = false;
            }
            if ( monitorDepths[i] != null ) {
                final MonitorInfo mi = ( MonitorInfo ) monitorDepths[i];
                sb.append ( "\t- locked (a " + mi.toString() + ") index " + mi.getLockedStackDepth() + " frame " + mi.getLockedStackFrame().toString() );
                sb.append ( "\r\n" );
            }
        }
        return sb.toString();
    }
    private static String getThreadDump ( final ThreadInfo[] tinfos ) {
        final StringBuilder sb = new StringBuilder();
        for ( final ThreadInfo tinfo : tinfos ) {
            sb.append ( getThreadDump ( tinfo ) );
            sb.append ( "\r\n" );
        }
        return sb.toString();
    }
    public static String findDeadlock() {
        ThreadInfo[] tinfos = null;
        final long[] ids = Diagnostics.threadMXBean.findDeadlockedThreads();
        if ( ids != null ) {
            tinfos = Diagnostics.threadMXBean.getThreadInfo ( Diagnostics.threadMXBean.findDeadlockedThreads(), true, true );
            if ( tinfos != null ) {
                final StringBuilder sb = new StringBuilder ( "Deadlock found between the following threads:" );
                sb.append ( "\r\n" );
                sb.append ( getThreadDump ( tinfos ) );
                return sb.toString();
            }
        }
        return "";
    }
    public static String getThreadDump() {
        return getThreadDump ( Diagnostics.sm );
    }
    public static String getThreadDump ( final Enumeration<Locale> requestedLocales ) {
        return getThreadDump ( StringManager.getManager ( "org.apache.tomcat.util", requestedLocales ) );
    }
    public static String getThreadDump ( final StringManager requestedSm ) {
        final StringBuilder sb = new StringBuilder();
        synchronized ( Diagnostics.timeformat ) {
            sb.append ( Diagnostics.timeformat.format ( new Date() ) );
        }
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.threadDumpTitle" ) );
        sb.append ( " " );
        sb.append ( Diagnostics.runtimeMXBean.getVmName() );
        sb.append ( " (" );
        sb.append ( Diagnostics.runtimeMXBean.getVmVersion() );
        final String vminfo = System.getProperty ( "java.vm.info" );
        if ( vminfo != null ) {
            sb.append ( " " + vminfo );
        }
        sb.append ( "):\r\n" );
        sb.append ( "\r\n" );
        final ThreadInfo[] tis = Diagnostics.threadMXBean.dumpAllThreads ( true, true );
        sb.append ( getThreadDump ( tis ) );
        sb.append ( findDeadlock() );
        return sb.toString();
    }
    private static String formatMemoryUsage ( final String name, final MemoryUsage usage ) {
        if ( usage != null ) {
            final StringBuilder sb = new StringBuilder();
            sb.append ( "  " + name + " init: " + usage.getInit() + "\r\n" );
            sb.append ( "  " + name + " used: " + usage.getUsed() + "\r\n" );
            sb.append ( "  " + name + " committed: " + usage.getCommitted() + "\r\n" );
            sb.append ( "  " + name + " max: " + usage.getMax() + "\r\n" );
            return sb.toString();
        }
        return "";
    }
    public static String getVMInfo() {
        return getVMInfo ( Diagnostics.sm );
    }
    public static String getVMInfo ( final Enumeration<Locale> requestedLocales ) {
        return getVMInfo ( StringManager.getManager ( "org.apache.tomcat.util", requestedLocales ) );
    }
    public static String getVMInfo ( final StringManager requestedSm ) {
        final StringBuilder sb = new StringBuilder();
        synchronized ( Diagnostics.timeformat ) {
            sb.append ( Diagnostics.timeformat.format ( new Date() ) );
        }
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoRuntime" ) );
        sb.append ( ":\r\n" );
        sb.append ( "  vmName: " + Diagnostics.runtimeMXBean.getVmName() + "\r\n" );
        sb.append ( "  vmVersion: " + Diagnostics.runtimeMXBean.getVmVersion() + "\r\n" );
        sb.append ( "  vmVendor: " + Diagnostics.runtimeMXBean.getVmVendor() + "\r\n" );
        sb.append ( "  specName: " + Diagnostics.runtimeMXBean.getSpecName() + "\r\n" );
        sb.append ( "  specVersion: " + Diagnostics.runtimeMXBean.getSpecVersion() + "\r\n" );
        sb.append ( "  specVendor: " + Diagnostics.runtimeMXBean.getSpecVendor() + "\r\n" );
        sb.append ( "  managementSpecVersion: " + Diagnostics.runtimeMXBean.getManagementSpecVersion() + "\r\n" );
        sb.append ( "  name: " + Diagnostics.runtimeMXBean.getName() + "\r\n" );
        sb.append ( "  startTime: " + Diagnostics.runtimeMXBean.getStartTime() + "\r\n" );
        sb.append ( "  uptime: " + Diagnostics.runtimeMXBean.getUptime() + "\r\n" );
        sb.append ( "  isBootClassPathSupported: " + Diagnostics.runtimeMXBean.isBootClassPathSupported() + "\r\n" );
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoOs" ) );
        sb.append ( ":\r\n" );
        sb.append ( "  name: " + Diagnostics.operatingSystemMXBean.getName() + "\r\n" );
        sb.append ( "  version: " + Diagnostics.operatingSystemMXBean.getVersion() + "\r\n" );
        sb.append ( "  architecture: " + Diagnostics.operatingSystemMXBean.getArch() + "\r\n" );
        sb.append ( "  availableProcessors: " + Diagnostics.operatingSystemMXBean.getAvailableProcessors() + "\r\n" );
        sb.append ( "  systemLoadAverage: " + Diagnostics.operatingSystemMXBean.getSystemLoadAverage() + "\r\n" );
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoThreadMxBean" ) );
        sb.append ( ":\r\n" );
        sb.append ( "  isCurrentThreadCpuTimeSupported: " + Diagnostics.threadMXBean.isCurrentThreadCpuTimeSupported() + "\r\n" );
        sb.append ( "  isThreadCpuTimeSupported: " + Diagnostics.threadMXBean.isThreadCpuTimeSupported() + "\r\n" );
        sb.append ( "  isThreadCpuTimeEnabled: " + Diagnostics.threadMXBean.isThreadCpuTimeEnabled() + "\r\n" );
        sb.append ( "  isObjectMonitorUsageSupported: " + Diagnostics.threadMXBean.isObjectMonitorUsageSupported() + "\r\n" );
        sb.append ( "  isSynchronizerUsageSupported: " + Diagnostics.threadMXBean.isSynchronizerUsageSupported() + "\r\n" );
        sb.append ( "  isThreadContentionMonitoringSupported: " + Diagnostics.threadMXBean.isThreadContentionMonitoringSupported() + "\r\n" );
        sb.append ( "  isThreadContentionMonitoringEnabled: " + Diagnostics.threadMXBean.isThreadContentionMonitoringEnabled() + "\r\n" );
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoThreadCounts" ) );
        sb.append ( ":\r\n" );
        sb.append ( "  daemon: " + Diagnostics.threadMXBean.getDaemonThreadCount() + "\r\n" );
        sb.append ( "  total: " + Diagnostics.threadMXBean.getThreadCount() + "\r\n" );
        sb.append ( "  peak: " + Diagnostics.threadMXBean.getPeakThreadCount() + "\r\n" );
        sb.append ( "  totalStarted: " + Diagnostics.threadMXBean.getTotalStartedThreadCount() + "\r\n" );
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoStartup" ) );
        sb.append ( ":\r\n" );
        for ( final String arg : Diagnostics.runtimeMXBean.getInputArguments() ) {
            sb.append ( "  " + arg + "\r\n" );
        }
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoPath" ) );
        sb.append ( ":\r\n" );
        sb.append ( "  bootClassPath: " + Diagnostics.runtimeMXBean.getBootClassPath() + "\r\n" );
        sb.append ( "  classPath: " + Diagnostics.runtimeMXBean.getClassPath() + "\r\n" );
        sb.append ( "  libraryPath: " + Diagnostics.runtimeMXBean.getLibraryPath() + "\r\n" );
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoClassLoading" ) );
        sb.append ( ":\r\n" );
        sb.append ( "  loaded: " + Diagnostics.classLoadingMXBean.getLoadedClassCount() + "\r\n" );
        sb.append ( "  unloaded: " + Diagnostics.classLoadingMXBean.getUnloadedClassCount() + "\r\n" );
        sb.append ( "  totalLoaded: " + Diagnostics.classLoadingMXBean.getTotalLoadedClassCount() + "\r\n" );
        sb.append ( "  isVerbose: " + Diagnostics.classLoadingMXBean.isVerbose() + "\r\n" );
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoClassCompilation" ) );
        sb.append ( ":\r\n" );
        sb.append ( "  name: " + Diagnostics.compilationMXBean.getName() + "\r\n" );
        sb.append ( "  totalCompilationTime: " + Diagnostics.compilationMXBean.getTotalCompilationTime() + "\r\n" );
        sb.append ( "  isCompilationTimeMonitoringSupported: " + Diagnostics.compilationMXBean.isCompilationTimeMonitoringSupported() + "\r\n" );
        sb.append ( "\r\n" );
        for ( final MemoryManagerMXBean mbean : Diagnostics.memoryManagerMXBeans ) {
            sb.append ( requestedSm.getString ( "diagnostics.vmInfoMemoryManagers", mbean.getName() ) );
            sb.append ( ":\r\n" );
            sb.append ( "  isValid: " + mbean.isValid() + "\r\n" );
            sb.append ( "  mbean.getMemoryPoolNames: \r\n" );
            final String[] names = mbean.getMemoryPoolNames();
            Arrays.sort ( names );
            for ( final String name : names ) {
                sb.append ( "\t" + name + "\r\n" );
            }
            sb.append ( "\r\n" );
        }
        for ( final GarbageCollectorMXBean mbean2 : Diagnostics.garbageCollectorMXBeans ) {
            sb.append ( requestedSm.getString ( "diagnostics.vmInfoGarbageCollectors", mbean2.getName() ) );
            sb.append ( ":\r\n" );
            sb.append ( "  isValid: " + mbean2.isValid() + "\r\n" );
            sb.append ( "  mbean.getMemoryPoolNames: \r\n" );
            final String[] names = mbean2.getMemoryPoolNames();
            Arrays.sort ( names );
            for ( final String name : names ) {
                sb.append ( "\t" + name + "\r\n" );
            }
            sb.append ( "  getCollectionCount: " + mbean2.getCollectionCount() + "\r\n" );
            sb.append ( "  getCollectionTime: " + mbean2.getCollectionTime() + "\r\n" );
            sb.append ( "\r\n" );
        }
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoMemory" ) );
        sb.append ( ":\r\n" );
        sb.append ( "  isVerbose: " + Diagnostics.memoryMXBean.isVerbose() + "\r\n" );
        sb.append ( "  getObjectPendingFinalizationCount: " + Diagnostics.memoryMXBean.getObjectPendingFinalizationCount() + "\r\n" );
        sb.append ( formatMemoryUsage ( "heap", Diagnostics.memoryMXBean.getHeapMemoryUsage() ) );
        sb.append ( formatMemoryUsage ( "non-heap", Diagnostics.memoryMXBean.getNonHeapMemoryUsage() ) );
        sb.append ( "\r\n" );
        for ( final MemoryPoolMXBean mbean3 : Diagnostics.memoryPoolMXBeans ) {
            sb.append ( requestedSm.getString ( "diagnostics.vmInfoMemoryPools", mbean3.getName() ) );
            sb.append ( ":\r\n" );
            sb.append ( "  isValid: " + mbean3.isValid() + "\r\n" );
            sb.append ( "  getType: " + mbean3.getType() + "\r\n" );
            sb.append ( "  mbean.getMemoryManagerNames: \r\n" );
            final String[] names = mbean3.getMemoryManagerNames();
            Arrays.sort ( names );
            for ( final String name : names ) {
                sb.append ( "\t" + name + "\r\n" );
            }
            sb.append ( "  isUsageThresholdSupported: " + mbean3.isUsageThresholdSupported() + "\r\n" );
            try {
                sb.append ( "  isUsageThresholdExceeded: " + mbean3.isUsageThresholdExceeded() + "\r\n" );
            } catch ( UnsupportedOperationException ex ) {}
            sb.append ( "  isCollectionUsageThresholdSupported: " + mbean3.isCollectionUsageThresholdSupported() + "\r\n" );
            try {
                sb.append ( "  isCollectionUsageThresholdExceeded: " + mbean3.isCollectionUsageThresholdExceeded() + "\r\n" );
            } catch ( UnsupportedOperationException ex2 ) {}
            try {
                sb.append ( "  getUsageThreshold: " + mbean3.getUsageThreshold() + "\r\n" );
            } catch ( UnsupportedOperationException ex3 ) {}
            try {
                sb.append ( "  getUsageThresholdCount: " + mbean3.getUsageThresholdCount() + "\r\n" );
            } catch ( UnsupportedOperationException ex4 ) {}
            try {
                sb.append ( "  getCollectionUsageThreshold: " + mbean3.getCollectionUsageThreshold() + "\r\n" );
            } catch ( UnsupportedOperationException ex5 ) {}
            try {
                sb.append ( "  getCollectionUsageThresholdCount: " + mbean3.getCollectionUsageThresholdCount() + "\r\n" );
            } catch ( UnsupportedOperationException ex6 ) {}
            sb.append ( formatMemoryUsage ( "current", mbean3.getUsage() ) );
            sb.append ( formatMemoryUsage ( "collection", mbean3.getCollectionUsage() ) );
            sb.append ( formatMemoryUsage ( "peak", mbean3.getPeakUsage() ) );
            sb.append ( "\r\n" );
        }
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoSystem" ) );
        sb.append ( ":\r\n" );
        final Map<String, String> props = Diagnostics.runtimeMXBean.getSystemProperties();
        final ArrayList<String> keys = new ArrayList<String> ( props.keySet() );
        Collections.sort ( keys );
        for ( final String prop : keys ) {
            sb.append ( "  " + prop + ": " + props.get ( prop ) + "\r\n" );
        }
        sb.append ( "\r\n" );
        sb.append ( requestedSm.getString ( "diagnostics.vmInfoLogger" ) );
        sb.append ( ":\r\n" );
        final List<String> loggers = Diagnostics.loggingMXBean.getLoggerNames();
        Collections.sort ( loggers );
        for ( final String logger : loggers ) {
            sb.append ( "  " + logger + ": level=" + Diagnostics.loggingMXBean.getLoggerLevel ( logger ) + ", parent=" + Diagnostics.loggingMXBean.getParentLoggerName ( logger ) + "\r\n" );
        }
        sb.append ( "\r\n" );
        return sb.toString();
    }
    static {
        sm = StringManager.getManager ( "org.apache.tomcat.util" );
        log = LogFactory.getLog ( Diagnostics.class );
        timeformat = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss.SSS" );
        classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        compilationMXBean = ManagementFactory.getCompilationMXBean();
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        threadMXBean = ManagementFactory.getThreadMXBean();
        loggingMXBean = LogManager.getLoggingMXBean();
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        memoryManagerMXBeans = ManagementFactory.getMemoryManagerMXBeans();
        memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    }
}

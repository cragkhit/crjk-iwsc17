package org.jfree.chart.servlet;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javax.servlet.http.HttpServletResponse;
import org.jfree.chart.ChartUtilities;
import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartRenderingInfo;
import javax.servlet.http.HttpSession;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.ParamChecks;
public class ServletUtilities {
    private static String tempFilePrefix;
    private static String tempOneTimeFilePrefix;
    public static String getTempFilePrefix() {
        return ServletUtilities.tempFilePrefix;
    }
    public static void setTempFilePrefix ( final String prefix ) {
        ParamChecks.nullNotPermitted ( prefix, "prefix" );
        ServletUtilities.tempFilePrefix = prefix;
    }
    public static String getTempOneTimeFilePrefix() {
        return ServletUtilities.tempOneTimeFilePrefix;
    }
    public static void setTempOneTimeFilePrefix ( final String prefix ) {
        ParamChecks.nullNotPermitted ( prefix, "prefix" );
        ServletUtilities.tempOneTimeFilePrefix = prefix;
    }
    public static String saveChartAsPNG ( final JFreeChart chart, final int width, final int height, final HttpSession session ) throws IOException {
        return saveChartAsPNG ( chart, width, height, null, session );
    }
    public static String saveChartAsPNG ( final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info, final HttpSession session ) throws IOException {
        ParamChecks.nullNotPermitted ( chart, "chart" );
        createTempDir();
        String prefix = ServletUtilities.tempFilePrefix;
        if ( session == null ) {
            prefix = ServletUtilities.tempOneTimeFilePrefix;
        }
        final File tempFile = File.createTempFile ( prefix, ".png", new File ( System.getProperty ( "java.io.tmpdir" ) ) );
        ChartUtilities.saveChartAsPNG ( tempFile, chart, width, height, info );
        if ( session != null ) {
            registerChartForDeletion ( tempFile, session );
        }
        return tempFile.getName();
    }
    public static String saveChartAsJPEG ( final JFreeChart chart, final int width, final int height, final HttpSession session ) throws IOException {
        return saveChartAsJPEG ( chart, width, height, null, session );
    }
    public static String saveChartAsJPEG ( final JFreeChart chart, final int width, final int height, final ChartRenderingInfo info, final HttpSession session ) throws IOException {
        ParamChecks.nullNotPermitted ( chart, "chart" );
        createTempDir();
        String prefix = ServletUtilities.tempFilePrefix;
        if ( session == null ) {
            prefix = ServletUtilities.tempOneTimeFilePrefix;
        }
        final File tempFile = File.createTempFile ( prefix, ".jpeg", new File ( System.getProperty ( "java.io.tmpdir" ) ) );
        ChartUtilities.saveChartAsJPEG ( tempFile, chart, width, height, info );
        if ( session != null ) {
            registerChartForDeletion ( tempFile, session );
        }
        return tempFile.getName();
    }
    protected static void createTempDir() {
        final String tempDirName = System.getProperty ( "java.io.tmpdir" );
        if ( tempDirName == null ) {
            throw new RuntimeException ( "Temporary directory system property (java.io.tmpdir) is null." );
        }
        final File tempDir = new File ( tempDirName );
        if ( !tempDir.exists() ) {
            tempDir.mkdirs();
        }
    }
    protected static void registerChartForDeletion ( final File tempFile, final HttpSession session ) {
        if ( session != null ) {
            ChartDeleter chartDeleter = ( ChartDeleter ) session.getAttribute ( "JFreeChart_Deleter" );
            if ( chartDeleter == null ) {
                chartDeleter = new ChartDeleter();
                session.setAttribute ( "JFreeChart_Deleter", ( Object ) chartDeleter );
            }
            chartDeleter.addChart ( tempFile.getName() );
        } else {
            System.out.println ( "Session is null - chart will not be deleted" );
        }
    }
    public static void sendTempFile ( final String filename, final HttpServletResponse response ) throws IOException {
        final File file = new File ( System.getProperty ( "java.io.tmpdir" ), filename );
        sendTempFile ( file, response );
    }
    public static void sendTempFile ( final File file, final HttpServletResponse response ) throws IOException {
        String mimeType = null;
        final String filename = file.getName();
        if ( filename.length() > 5 ) {
            if ( filename.substring ( filename.length() - 5, filename.length() ).equals ( ".jpeg" ) ) {
                mimeType = "image/jpeg";
            } else if ( filename.substring ( filename.length() - 4, filename.length() ).equals ( ".png" ) ) {
                mimeType = "image/png";
            }
        }
        sendTempFile ( file, response, mimeType );
    }
    public static void sendTempFile ( final File file, final HttpServletResponse response, final String mimeType ) throws IOException {
        if ( file.exists() ) {
            final BufferedInputStream bis = new BufferedInputStream ( new FileInputStream ( file ) );
            if ( mimeType != null ) {
                response.setHeader ( "Content-Type", mimeType );
            }
            response.setHeader ( "Content-Length", String.valueOf ( file.length() ) );
            final SimpleDateFormat sdf = new SimpleDateFormat ( "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH );
            sdf.setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
            response.setHeader ( "Last-Modified", sdf.format ( new Date ( file.lastModified() ) ) );
            final BufferedOutputStream bos = new BufferedOutputStream ( ( OutputStream ) response.getOutputStream() );
            final byte[] input = new byte[1024];
            boolean eof = false;
            while ( !eof ) {
                final int length = bis.read ( input );
                if ( length == -1 ) {
                    eof = true;
                } else {
                    bos.write ( input, 0, length );
                }
            }
            bos.flush();
            bis.close();
            bos.close();
            return;
        }
        throw new FileNotFoundException ( file.getAbsolutePath() );
    }
    public static String searchReplace ( final String inputString, final String searchString, final String replaceString ) {
        final int i = inputString.indexOf ( searchString );
        if ( i == -1 ) {
            return inputString;
        }
        String r = "";
        r = r + inputString.substring ( 0, i ) + replaceString;
        if ( i + searchString.length() < inputString.length() ) {
            r += searchReplace ( inputString.substring ( i + searchString.length() ), searchString, replaceString );
        }
        return r;
    }
    static {
        ServletUtilities.tempFilePrefix = "jfreechart-";
        ServletUtilities.tempOneTimeFilePrefix = "jfreechart-onetime-";
    }
}

package org.apache.catalina.valves;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public class ErrorReportValve extends ValveBase {
    private boolean showReport = true;
    private boolean showServerInfo = true;
    public ErrorReportValve() {
        super ( true );
    }
    @Override
    public void invoke ( Request request, Response response ) throws IOException, ServletException {
        getNext().invoke ( request, response );
        if ( response.isCommitted() ) {
            if ( response.setErrorReported() ) {
                try {
                    response.flushBuffer();
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                }
                response.getCoyoteResponse().action ( ActionCode.CLOSE_NOW, null );
            }
            return;
        }
        Throwable throwable = ( Throwable ) request.getAttribute ( RequestDispatcher.ERROR_EXCEPTION );
        if ( request.isAsync() && !request.isAsyncCompleting() ) {
            return;
        }
        if ( throwable != null && !response.isError() ) {
            response.reset();
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
        response.setSuspended ( false );
        try {
            report ( request, response, throwable );
        } catch ( Throwable tt ) {
            ExceptionUtils.handleThrowable ( tt );
        }
    }
    protected void report ( Request request, Response response, Throwable throwable ) {
        int statusCode = response.getStatus();
        if ( statusCode < 400 || response.getContentWritten() > 0 || !response.setErrorReported() ) {
            return;
        }
        String message = RequestUtil.filter ( response.getMessage() );
        if ( message == null ) {
            if ( throwable != null ) {
                String exceptionMessage = throwable.getMessage();
                if ( exceptionMessage != null && exceptionMessage.length() > 0 ) {
                    message = RequestUtil.filter ( ( new Scanner ( exceptionMessage ) ).nextLine() );
                }
            }
            if ( message == null ) {
                message = "";
            }
        }
        String report = null;
        StringManager smClient = StringManager.getManager (
                                     Constants.Package, request.getLocales() );
        response.setLocale ( smClient.getLocale() );
        try {
            report = smClient.getString ( "http." + statusCode );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
        if ( report == null ) {
            if ( message.length() == 0 ) {
                return;
            } else {
                report = smClient.getString ( "errorReportValve.noDescription" );
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append ( "<!DOCTYPE html><html><head>" );
        if ( showServerInfo || showReport ) {
            sb.append ( "<title>" );
            if ( showServerInfo ) {
                sb.append ( ServerInfo.getServerInfo() ).append ( " - " );
            }
            sb.append ( smClient.getString ( "errorReportValve.errorReport" ) );
            sb.append ( "</title>" );
            sb.append ( "<style type=\"text/css\">" );
            sb.append ( org.apache.catalina.util.TomcatCSS.TOMCAT_CSS );
            sb.append ( "</style> " );
        } else {
            sb.append ( "<title>" );
            sb.append ( smClient.getString ( "errorReportValve.errorReport" ) );
            sb.append ( "</title>" );
        }
        sb.append ( "</head><body>" );
        sb.append ( "<h1>" );
        sb.append ( smClient.getString ( "errorReportValve.statusHeader",
                                         String.valueOf ( statusCode ), message ) ).append ( "</h1>" );
        if ( showReport ) {
            sb.append ( "<div class=\"line\"></div>" );
            sb.append ( "<p><b>type</b> " );
            if ( throwable != null ) {
                sb.append ( smClient.getString ( "errorReportValve.exceptionReport" ) );
            } else {
                sb.append ( smClient.getString ( "errorReportValve.statusReport" ) );
            }
            sb.append ( "</p>" );
            sb.append ( "<p><b>" );
            sb.append ( smClient.getString ( "errorReportValve.message" ) );
            sb.append ( "</b> <u>" );
            sb.append ( message ).append ( "</u></p>" );
            sb.append ( "<p><b>" );
            sb.append ( smClient.getString ( "errorReportValve.description" ) );
            sb.append ( "</b> <u>" );
            sb.append ( report );
            sb.append ( "</u></p>" );
            if ( throwable != null ) {
                String stackTrace = getPartialServletStackTrace ( throwable );
                sb.append ( "<p><b>" );
                sb.append ( smClient.getString ( "errorReportValve.exception" ) );
                sb.append ( "</b></p><pre>" );
                sb.append ( RequestUtil.filter ( stackTrace ) );
                sb.append ( "</pre>" );
                int loops = 0;
                Throwable rootCause = throwable.getCause();
                while ( rootCause != null && ( loops < 10 ) ) {
                    stackTrace = getPartialServletStackTrace ( rootCause );
                    sb.append ( "<p><b>" );
                    sb.append ( smClient.getString ( "errorReportValve.rootCause" ) );
                    sb.append ( "</b></p><pre>" );
                    sb.append ( RequestUtil.filter ( stackTrace ) );
                    sb.append ( "</pre>" );
                    rootCause = rootCause.getCause();
                    loops++;
                }
                sb.append ( "<p><b>" );
                sb.append ( smClient.getString ( "errorReportValve.note" ) );
                sb.append ( "</b> <u>" );
                sb.append ( smClient.getString ( "errorReportValve.rootCauseInLogs",
                                                 showServerInfo ? ServerInfo.getServerInfo() : "" ) );
                sb.append ( "</u></p>" );
            }
            sb.append ( "<hr class=\"line\">" );
        }
        if ( showServerInfo ) {
            sb.append ( "<h3>" ).append ( ServerInfo.getServerInfo() ).append ( "</h3>" );
        }
        sb.append ( "</body></html>" );
        try {
            try {
                response.setContentType ( "text/html" );
                response.setCharacterEncoding ( "utf-8" );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                if ( container.getLogger().isDebugEnabled() ) {
                    container.getLogger().debug ( "status.setContentType", t );
                }
            }
            Writer writer = response.getReporter();
            if ( writer != null ) {
                writer.write ( sb.toString() );
                response.finishResponse();
            }
        } catch ( IOException e ) {
        } catch ( IllegalStateException e ) {
        }
    }
    protected String getPartialServletStackTrace ( Throwable t ) {
        StringBuilder trace = new StringBuilder();
        trace.append ( t.toString() ).append ( '\n' );
        StackTraceElement[] elements = t.getStackTrace();
        int pos = elements.length;
        for ( int i = elements.length - 1; i >= 0; i-- ) {
            if ( ( elements[i].getClassName().startsWith
                    ( "org.apache.catalina.core.ApplicationFilterChain" ) )
                    && ( elements[i].getMethodName().equals ( "internalDoFilter" ) ) ) {
                pos = i;
                break;
            }
        }
        for ( int i = 0; i < pos; i++ ) {
            if ( ! ( elements[i].getClassName().startsWith
                     ( "org.apache.catalina.core." ) ) ) {
                trace.append ( '\t' ).append ( elements[i].toString() ).append ( '\n' );
            }
        }
        return trace.toString();
    }
    public void setShowReport ( boolean showReport ) {
        this.showReport = showReport;
    }
    public boolean isShowReport() {
        return showReport;
    }
    public void setShowServerInfo ( boolean showServerInfo ) {
        this.showServerInfo = showServerInfo;
    }
    public boolean isShowServerInfo() {
        return showServerInfo;
    }
}

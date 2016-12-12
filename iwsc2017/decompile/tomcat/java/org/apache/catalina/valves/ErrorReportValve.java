package org.apache.catalina.valves;
import java.io.Writer;
import org.apache.catalina.util.ServerInfo;
import org.apache.tomcat.util.res.StringManager;
import java.util.Scanner;
import org.apache.catalina.util.RequestUtil;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
public class ErrorReportValve extends ValveBase {
    private boolean showReport;
    private boolean showServerInfo;
    public ErrorReportValve() {
        super ( true );
        this.showReport = true;
        this.showServerInfo = true;
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        this.getNext().invoke ( request, response );
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
        final Throwable throwable = ( Throwable ) request.getAttribute ( "javax.servlet.error.exception" );
        if ( request.isAsync() && !request.isAsyncCompleting() ) {
            return;
        }
        if ( throwable != null && !response.isError() ) {
            response.reset();
            response.sendError ( 500 );
        }
        response.setSuspended ( false );
        try {
            this.report ( request, response, throwable );
        } catch ( Throwable tt ) {
            ExceptionUtils.handleThrowable ( tt );
        }
    }
    protected void report ( final Request request, final Response response, final Throwable throwable ) {
        final int statusCode = response.getStatus();
        if ( statusCode < 400 || response.getContentWritten() > 0L || !response.setErrorReported() ) {
            return;
        }
        String message = RequestUtil.filter ( response.getMessage() );
        if ( message == null ) {
            if ( throwable != null ) {
                final String exceptionMessage = throwable.getMessage();
                if ( exceptionMessage != null && exceptionMessage.length() > 0 ) {
                    message = RequestUtil.filter ( new Scanner ( exceptionMessage ).nextLine() );
                }
            }
            if ( message == null ) {
                message = "";
            }
        }
        String report = null;
        final StringManager smClient = StringManager.getManager ( "org.apache.catalina.valves", request.getLocales() );
        response.setLocale ( smClient.getLocale() );
        try {
            report = smClient.getString ( "http." + statusCode );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
        if ( report == null ) {
            if ( message.length() == 0 ) {
                return;
            }
            report = smClient.getString ( "errorReportValve.noDescription" );
        }
        final StringBuilder sb = new StringBuilder();
        sb.append ( "<!DOCTYPE html><html><head>" );
        if ( this.showServerInfo || this.showReport ) {
            sb.append ( "<title>" );
            if ( this.showServerInfo ) {
                sb.append ( ServerInfo.getServerInfo() ).append ( " - " );
            }
            sb.append ( smClient.getString ( "errorReportValve.errorReport" ) );
            sb.append ( "</title>" );
            sb.append ( "<style type=\"text/css\">" );
            sb.append ( "h1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} h2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} h3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} body {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} b {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} p {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;} a {color:black;} a.name {color:black;} .line {height:1px;background-color:#525D76;border:none;}" );
            sb.append ( "</style> " );
        } else {
            sb.append ( "<title>" );
            sb.append ( smClient.getString ( "errorReportValve.errorReport" ) );
            sb.append ( "</title>" );
        }
        sb.append ( "</head><body>" );
        sb.append ( "<h1>" );
        sb.append ( smClient.getString ( "errorReportValve.statusHeader", String.valueOf ( statusCode ), message ) ).append ( "</h1>" );
        if ( this.showReport ) {
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
                String stackTrace = this.getPartialServletStackTrace ( throwable );
                sb.append ( "<p><b>" );
                sb.append ( smClient.getString ( "errorReportValve.exception" ) );
                sb.append ( "</b></p><pre>" );
                sb.append ( RequestUtil.filter ( stackTrace ) );
                sb.append ( "</pre>" );
                int loops = 0;
                for ( Throwable rootCause = throwable.getCause(); rootCause != null && loops < 10; rootCause = rootCause.getCause(), ++loops ) {
                    stackTrace = this.getPartialServletStackTrace ( rootCause );
                    sb.append ( "<p><b>" );
                    sb.append ( smClient.getString ( "errorReportValve.rootCause" ) );
                    sb.append ( "</b></p><pre>" );
                    sb.append ( RequestUtil.filter ( stackTrace ) );
                    sb.append ( "</pre>" );
                }
                sb.append ( "<p><b>" );
                sb.append ( smClient.getString ( "errorReportValve.note" ) );
                sb.append ( "</b> <u>" );
                sb.append ( smClient.getString ( "errorReportValve.rootCauseInLogs", this.showServerInfo ? ServerInfo.getServerInfo() : "" ) );
                sb.append ( "</u></p>" );
            }
            sb.append ( "<hr class=\"line\">" );
        }
        if ( this.showServerInfo ) {
            sb.append ( "<h3>" ).append ( ServerInfo.getServerInfo() ).append ( "</h3>" );
        }
        sb.append ( "</body></html>" );
        try {
            try {
                response.setContentType ( "text/html" );
                response.setCharacterEncoding ( "utf-8" );
            } catch ( Throwable t2 ) {
                ExceptionUtils.handleThrowable ( t2 );
                if ( this.container.getLogger().isDebugEnabled() ) {
                    this.container.getLogger().debug ( "status.setContentType", t2 );
                }
            }
            final Writer writer = response.getReporter();
            if ( writer != null ) {
                writer.write ( sb.toString() );
                response.finishResponse();
            }
        } catch ( IOException ex ) {}
        catch ( IllegalStateException ex2 ) {}
    }
    protected String getPartialServletStackTrace ( final Throwable t ) {
        final StringBuilder trace = new StringBuilder();
        trace.append ( t.toString() ).append ( '\n' );
        final StackTraceElement[] elements = t.getStackTrace();
        int pos = elements.length;
        for ( int i = elements.length - 1; i >= 0; --i ) {
            if ( elements[i].getClassName().startsWith ( "org.apache.catalina.core.ApplicationFilterChain" ) && elements[i].getMethodName().equals ( "internalDoFilter" ) ) {
                pos = i;
                break;
            }
        }
        for ( int i = 0; i < pos; ++i ) {
            if ( !elements[i].getClassName().startsWith ( "org.apache.catalina.core." ) ) {
                trace.append ( '\t' ).append ( elements[i].toString() ).append ( '\n' );
            }
        }
        return trace.toString();
    }
    public void setShowReport ( final boolean showReport ) {
        this.showReport = showReport;
    }
    public boolean isShowReport() {
        return this.showReport;
    }
    public void setShowServerInfo ( final boolean showServerInfo ) {
        this.showServerInfo = showServerInfo;
    }
    public boolean isShowServerInfo() {
        return this.showServerInfo;
    }
}

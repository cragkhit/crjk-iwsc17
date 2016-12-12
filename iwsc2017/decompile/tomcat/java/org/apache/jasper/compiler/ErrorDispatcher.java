package org.apache.jasper.compiler;
import org.apache.jasper.JspCompilationContext;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;
import java.io.IOException;
import org.apache.jasper.JasperException;
public class ErrorDispatcher {
    private final ErrorHandler errHandler;
    private final boolean jspcMode;
    public ErrorDispatcher ( final boolean jspcMode ) {
        this.errHandler = new DefaultErrorHandler();
        this.jspcMode = jspcMode;
    }
    public void jspError ( final String errCode, final String... args ) throws JasperException {
        this.dispatch ( null, errCode, args, null );
    }
    public void jspError ( final Mark where, final String errCode, final String... args ) throws JasperException {
        this.dispatch ( where, errCode, args, null );
    }
    public void jspError ( final Node n, final String errCode, final String... args ) throws JasperException {
        this.dispatch ( n.getStart(), errCode, args, null );
    }
    public void jspError ( final Exception e ) throws JasperException {
        this.dispatch ( null, null, null, e );
    }
    public void jspError ( final Exception e, final String errCode, final String... args ) throws JasperException {
        this.dispatch ( null, errCode, args, e );
    }
    public void jspError ( final Mark where, final Exception e, final String errCode, final String... args ) throws JasperException {
        this.dispatch ( where, errCode, args, e );
    }
    public void jspError ( final Node n, final Exception e, final String errCode, final String... args ) throws JasperException {
        this.dispatch ( n.getStart(), errCode, args, e );
    }
    public static JavacErrorDetail[] parseJavacErrors ( final String errMsg, final String fname, final Node.Nodes page ) throws JasperException, IOException {
        return parseJavacMessage ( errMsg, fname, page );
    }
    public void javacError ( final JavacErrorDetail[] javacErrors ) throws JasperException {
        this.errHandler.javacError ( javacErrors );
    }
    public void javacError ( final String errorReport, final Exception e ) throws JasperException {
        this.errHandler.javacError ( errorReport, e );
    }
    private void dispatch ( final Mark where, final String errCode, final Object[] args, final Exception e ) throws JasperException {
        String file = null;
        String errMsg = null;
        int line = -1;
        int column = -1;
        boolean hasLocation = false;
        if ( errCode != null ) {
            errMsg = Localizer.getMessage ( errCode, args );
        } else if ( e != null ) {
            errMsg = e.getMessage();
        }
        if ( where != null ) {
            if ( this.jspcMode ) {
                try {
                    file = where.getURL().toString();
                } catch ( MalformedURLException me ) {
                    file = where.getFile();
                }
            } else {
                file = where.getFile();
            }
            line = where.getLineNumber();
            column = where.getColumnNumber();
            hasLocation = true;
        }
        Exception nestedEx = e;
        if ( e instanceof SAXException && ( ( SAXException ) e ).getException() != null ) {
            nestedEx = ( ( SAXException ) e ).getException();
        }
        if ( hasLocation ) {
            this.errHandler.jspError ( file, line, column, errMsg, nestedEx );
        } else {
            this.errHandler.jspError ( errMsg, nestedEx );
        }
    }
    private static JavacErrorDetail[] parseJavacMessage ( final String errMsg, final String fname, final Node.Nodes page ) throws IOException, JasperException {
        final ArrayList<JavacErrorDetail> errors = new ArrayList<JavacErrorDetail>();
        StringBuilder errMsgBuf = null;
        int lineNum = -1;
        JavacErrorDetail javacError = null;
        final BufferedReader reader = new BufferedReader ( new StringReader ( errMsg ) );
        String line = null;
        while ( ( line = reader.readLine() ) != null ) {
            final int beginColon = line.indexOf ( 58, 2 );
            final int endColon = line.indexOf ( 58, beginColon + 1 );
            if ( beginColon >= 0 && endColon >= 0 ) {
                if ( javacError != null ) {
                    errors.add ( javacError );
                }
                final String lineNumStr = line.substring ( beginColon + 1, endColon );
                try {
                    lineNum = Integer.parseInt ( lineNumStr );
                } catch ( NumberFormatException e ) {
                    lineNum = -1;
                }
                errMsgBuf = new StringBuilder();
                javacError = createJavacError ( fname, page, errMsgBuf, lineNum );
            }
            if ( errMsgBuf != null ) {
                errMsgBuf.append ( line );
                errMsgBuf.append ( System.lineSeparator() );
            }
        }
        if ( javacError != null ) {
            errors.add ( javacError );
        }
        reader.close();
        JavacErrorDetail[] errDetails = null;
        if ( errors.size() > 0 ) {
            errDetails = new JavacErrorDetail[errors.size()];
            errors.toArray ( errDetails );
        }
        return errDetails;
    }
    public static JavacErrorDetail createJavacError ( final String fname, final Node.Nodes page, final StringBuilder errMsgBuf, final int lineNum ) throws JasperException {
        return createJavacError ( fname, page, errMsgBuf, lineNum, null );
    }
    public static JavacErrorDetail createJavacError ( final String fname, final Node.Nodes page, final StringBuilder errMsgBuf, final int lineNum, final JspCompilationContext ctxt ) throws JasperException {
        final ErrorVisitor errVisitor = new ErrorVisitor ( lineNum );
        page.visit ( errVisitor );
        final Node errNode = errVisitor.getJspSourceNode();
        JavacErrorDetail javacError;
        if ( errNode != null && errNode.getStart() != null ) {
            if ( errVisitor.getJspSourceNode() instanceof Node.Scriptlet || errVisitor.getJspSourceNode() instanceof Node.Declaration ) {
                javacError = new JavacErrorDetail ( fname, lineNum, errNode.getStart().getFile(), errNode.getStart().getLineNumber() + lineNum - errVisitor.getJspSourceNode().getBeginJavaLine(), errMsgBuf, ctxt );
            } else {
                javacError = new JavacErrorDetail ( fname, lineNum, errNode.getStart().getFile(), errNode.getStart().getLineNumber(), errMsgBuf, ctxt );
            }
        } else {
            javacError = new JavacErrorDetail ( fname, lineNum, errMsgBuf );
        }
        return javacError;
    }
    private static class ErrorVisitor extends Node.Visitor {
        private final int lineNum;
        private Node found;
        public ErrorVisitor ( final int lineNum ) {
            this.lineNum = lineNum;
        }
        public void doVisit ( final Node n ) throws JasperException {
            if ( this.lineNum >= n.getBeginJavaLine() && this.lineNum < n.getEndJavaLine() ) {
                this.found = n;
            }
        }
        public Node getJspSourceNode() {
            return this.found;
        }
    }
}

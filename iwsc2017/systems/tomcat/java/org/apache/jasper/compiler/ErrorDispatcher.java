package org.apache.jasper.compiler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.xml.sax.SAXException;
public class ErrorDispatcher {
    private final ErrorHandler errHandler;
    private final boolean jspcMode;
    public ErrorDispatcher ( boolean jspcMode ) {
        errHandler = new DefaultErrorHandler();
        this.jspcMode = jspcMode;
    }
    public void jspError ( String errCode, String... args ) throws JasperException {
        dispatch ( null, errCode, args, null );
    }
    public void jspError ( Mark where, String errCode, String... args )
    throws JasperException {
        dispatch ( where, errCode, args, null );
    }
    public void jspError ( Node n, String errCode, String... args )
    throws JasperException {
        dispatch ( n.getStart(), errCode, args, null );
    }
    public void jspError ( Exception e ) throws JasperException {
        dispatch ( null, null, null, e );
    }
    public void jspError ( Exception e, String errCode, String... args )
    throws JasperException {
        dispatch ( null, errCode, args, e );
    }
    public void jspError ( Mark where, Exception e, String errCode, String... args )
    throws JasperException {
        dispatch ( where, errCode, args, e );
    }
    public void jspError ( Node n, Exception e, String errCode, String... args )
    throws JasperException {
        dispatch ( n.getStart(), errCode, args, e );
    }
    public static JavacErrorDetail[] parseJavacErrors ( String errMsg,
            String fname,
            Node.Nodes page )
    throws JasperException, IOException {
        return parseJavacMessage ( errMsg, fname, page );
    }
    public void javacError ( JavacErrorDetail[] javacErrors )
    throws JasperException {
        errHandler.javacError ( javacErrors );
    }
    public void javacError ( String errorReport, Exception e )
    throws JasperException {
        errHandler.javacError ( errorReport, e );
    }
    private void dispatch ( Mark where, String errCode, Object[] args,
                            Exception e ) throws JasperException {
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
            if ( jspcMode ) {
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
        if ( ( e instanceof SAXException )
                && ( ( ( SAXException ) e ).getException() != null ) ) {
            nestedEx = ( ( SAXException ) e ).getException();
        }
        if ( hasLocation ) {
            errHandler.jspError ( file, line, column, errMsg, nestedEx );
        } else {
            errHandler.jspError ( errMsg, nestedEx );
        }
    }
    private static JavacErrorDetail[] parseJavacMessage (
        String errMsg, String fname, Node.Nodes page )
    throws IOException, JasperException {
        ArrayList<JavacErrorDetail> errors = new ArrayList<>();
        StringBuilder errMsgBuf = null;
        int lineNum = -1;
        JavacErrorDetail javacError = null;
        BufferedReader reader = new BufferedReader ( new StringReader ( errMsg ) );
        String line = null;
        while ( ( line = reader.readLine() ) != null ) {
            int beginColon = line.indexOf ( ':', 2 );
            int endColon = line.indexOf ( ':', beginColon + 1 );
            if ( ( beginColon >= 0 ) && ( endColon >= 0 ) ) {
                if ( javacError != null ) {
                    errors.add ( javacError );
                }
                String lineNumStr = line.substring ( beginColon + 1, endColon );
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
    public static JavacErrorDetail createJavacError ( String fname,
            Node.Nodes page, StringBuilder errMsgBuf, int lineNum )
    throws JasperException {
        return createJavacError ( fname, page, errMsgBuf, lineNum, null );
    }
    public static JavacErrorDetail createJavacError ( String fname,
            Node.Nodes page, StringBuilder errMsgBuf, int lineNum,
            JspCompilationContext ctxt ) throws JasperException {
        JavacErrorDetail javacError;
        ErrorVisitor errVisitor = new ErrorVisitor ( lineNum );
        page.visit ( errVisitor );
        Node errNode = errVisitor.getJspSourceNode();
        if ( ( errNode != null ) && ( errNode.getStart() != null ) ) {
            if ( errVisitor.getJspSourceNode() instanceof Node.Scriptlet ||
                    errVisitor.getJspSourceNode() instanceof Node.Declaration ) {
                javacError = new JavacErrorDetail (
                    fname,
                    lineNum,
                    errNode.getStart().getFile(),
                    errNode.getStart().getLineNumber() + lineNum -
                    errVisitor.getJspSourceNode().getBeginJavaLine(),
                    errMsgBuf,
                    ctxt );
            } else {
                javacError = new JavacErrorDetail (
                    fname,
                    lineNum,
                    errNode.getStart().getFile(),
                    errNode.getStart().getLineNumber(),
                    errMsgBuf,
                    ctxt );
            }
        } else {
            javacError = new JavacErrorDetail (
                fname,
                lineNum,
                errMsgBuf );
        }
        return javacError;
    }
    private static class ErrorVisitor extends Node.Visitor {
        private final int lineNum;
        private Node found;
        public ErrorVisitor ( int lineNum ) {
            this.lineNum = lineNum;
        }
        @Override
        public void doVisit ( Node n ) throws JasperException {
            if ( ( lineNum >= n.getBeginJavaLine() )
                    && ( lineNum < n.getEndJavaLine() ) ) {
                found = n;
            }
        }
        public Node getJspSourceNode() {
            return found;
        }
    }
}

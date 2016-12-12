package org.apache.jasper.runtime;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspWriter;
public class ServletResponseWrapperInclude extends HttpServletResponseWrapper {
    private final PrintWriter printWriter;
    private final JspWriter jspWriter;
    public ServletResponseWrapperInclude ( ServletResponse response,
                                           JspWriter jspWriter ) {
        super ( ( HttpServletResponse ) response );
        this.printWriter = new PrintWriter ( jspWriter );
        this.jspWriter = jspWriter;
    }
    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new IllegalStateException();
    }
    @Override
    public void resetBuffer() {
        try {
            jspWriter.clearBuffer();
        } catch ( IOException ioe ) {
        }
    }
}

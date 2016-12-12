package org.apache.jasper.runtime;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspWriter;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponseWrapper;
public class ServletResponseWrapperInclude extends HttpServletResponseWrapper {
    private final PrintWriter printWriter;
    private final JspWriter jspWriter;
    public ServletResponseWrapperInclude ( final ServletResponse response, final JspWriter jspWriter ) {
        super ( ( HttpServletResponse ) response );
        this.printWriter = new PrintWriter ( ( Writer ) jspWriter );
        this.jspWriter = jspWriter;
    }
    public PrintWriter getWriter() throws IOException {
        return this.printWriter;
    }
    public ServletOutputStream getOutputStream() throws IOException {
        throw new IllegalStateException();
    }
    public void resetBuffer() {
        try {
            this.jspWriter.clearBuffer();
        } catch ( IOException ex ) {}
    }
}

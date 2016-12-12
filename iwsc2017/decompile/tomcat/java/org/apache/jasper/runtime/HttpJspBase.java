package org.apache.jasper.runtime;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.jasper.compiler.Localizer;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.jsp.HttpJspPage;
import javax.servlet.http.HttpServlet;
public abstract class HttpJspBase extends HttpServlet implements HttpJspPage {
    private static final long serialVersionUID = 1L;
    public final void init ( final ServletConfig config ) throws ServletException {
        super.init ( config );
        this.jspInit();
        this._jspInit();
    }
    public String getServletInfo() {
        return Localizer.getMessage ( "jsp.engine.info" );
    }
    public final void destroy() {
        this.jspDestroy();
        this._jspDestroy();
    }
    public final void service ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
        this._jspService ( request, response );
    }
    public void jspInit() {
    }
    public void _jspInit() {
    }
    public void jspDestroy() {
    }
    protected void _jspDestroy() {
    }
    public abstract void _jspService ( final HttpServletRequest p0, final HttpServletResponse p1 ) throws ServletException, IOException;
}

package org.apache.jasper.runtime;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;
import org.apache.jasper.compiler.Localizer;
public abstract class HttpJspBase extends HttpServlet implements HttpJspPage {
    private static final long serialVersionUID = 1L;
    protected HttpJspBase() {
    }
    @Override
    public final void init ( ServletConfig config )
    throws ServletException {
        super.init ( config );
        jspInit();
        _jspInit();
    }
    @Override
    public String getServletInfo() {
        return Localizer.getMessage ( "jsp.engine.info" );
    }
    @Override
    public final void destroy() {
        jspDestroy();
        _jspDestroy();
    }
    @Override
    public final void service ( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
        _jspService ( request, response );
    }
    @Override
    public void jspInit() {
    }
    public void _jspInit() {
    }
    @Override
    public void jspDestroy() {
    }
    protected void _jspDestroy() {
    }
    @Override
    public abstract void _jspService ( HttpServletRequest request,
                                       HttpServletResponse response )
    throws ServletException, IOException;
}

package javax.servlet.jsp;
import javax.servlet.Servlet;
public interface JspPage extends Servlet {
    void jspInit();
    void jspDestroy();
}

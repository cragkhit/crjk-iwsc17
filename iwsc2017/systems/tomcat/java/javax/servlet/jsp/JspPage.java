package javax.servlet.jsp;
import javax.servlet.Servlet;
public interface JspPage extends Servlet {
    public void jspInit();
    public void jspDestroy();
}

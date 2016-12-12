package javax.servlet.jsp.tagext;
import java.io.InputStream;
public abstract class PageData {
    public PageData() {
    }
    public abstract InputStream getInputStream();
}

package javax.servlet.http;
import javax.servlet.annotation.WebServlet;
public interface Mapping {
    String getMatchValue();
    String getPattern();
    MappingMatch getMappingMatch();
    String getServletName();
}

package javax.servlet.http;
public interface Mapping {
    String getMatchValue();
    String getPattern();
    MappingMatch getMappingMatch();
    String getServletName();
}

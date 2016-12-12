package javax.websocket;
import java.util.List;
public interface Extension {
    String getName();
    List<Parameter> getParameters();
    public interface Parameter {
        String getName();
        String getValue();
    }
}

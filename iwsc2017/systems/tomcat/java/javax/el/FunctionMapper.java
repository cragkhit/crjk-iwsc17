package javax.el;
import java.lang.reflect.Method;
public abstract class FunctionMapper {
    public abstract Method resolveFunction ( String prefix, String localName );
    public void mapFunction ( String prefix, String localName, Method method ) {
    }
}

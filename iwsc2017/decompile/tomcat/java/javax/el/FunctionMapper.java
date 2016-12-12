package javax.el;
import java.lang.reflect.Method;
public abstract class FunctionMapper {
    public abstract Method resolveFunction ( final String p0, final String p1 );
    public void mapFunction ( final String prefix, final String localName, final Method method ) {
    }
}

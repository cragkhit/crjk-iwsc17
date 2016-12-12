package org.apache.jasper.el;
import java.lang.reflect.Method;
import javax.el.FunctionMapper;
@Deprecated
public final class FunctionMapperImpl extends FunctionMapper {
    private final javax.servlet.jsp.el.FunctionMapper fnMapper;
    public FunctionMapperImpl ( final javax.servlet.jsp.el.FunctionMapper fnMapper ) {
        this.fnMapper = fnMapper;
    }
    public Method resolveFunction ( final String prefix, final String localName ) {
        return this.fnMapper.resolveFunction ( prefix, localName );
    }
}

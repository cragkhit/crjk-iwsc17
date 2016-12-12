package javax.el;
import java.lang.reflect.Method;
private static class MethodWrapper extends Wrapper {
    private final Method m;
    public MethodWrapper ( final Method m ) {
        this.m = m;
    }
    @Override
    public Object unWrap() {
        return this.m;
    }
    @Override
    public Class<?>[] getParameterTypes() {
        return this.m.getParameterTypes();
    }
    @Override
    public boolean isVarArgs() {
        return this.m.isVarArgs();
    }
    @Override
    public boolean isBridge() {
        return this.m.isBridge();
    }
}

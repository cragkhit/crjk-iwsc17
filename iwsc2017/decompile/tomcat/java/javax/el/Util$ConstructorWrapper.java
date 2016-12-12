package javax.el;
import java.lang.reflect.Constructor;
private static class ConstructorWrapper extends Wrapper {
    private final Constructor<?> c;
    public ConstructorWrapper ( final Constructor<?> c ) {
        this.c = c;
    }
    @Override
    public Object unWrap() {
        return this.c;
    }
    @Override
    public Class<?>[] getParameterTypes() {
        return this.c.getParameterTypes();
    }
    @Override
    public boolean isVarArgs() {
        return this.c.isVarArgs();
    }
    @Override
    public boolean isBridge() {
        return false;
    }
}

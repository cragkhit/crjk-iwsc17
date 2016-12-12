package org.junit.runners.model;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.junit.runners.BlockJUnit4ClassRunner;
public class FrameworkField extends FrameworkMember<FrameworkField> {
    private final Field field;
    FrameworkField ( Field field ) {
        if ( field == null ) {
            throw new NullPointerException (
                "FrameworkField cannot be created without an underlying field." );
        }
        this.field = field;
    }
    @Override
    public String getName() {
        return getField().getName();
    }
    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }
    public <T extends Annotation> T getAnnotation ( Class<T> annotationType ) {
        return field.getAnnotation ( annotationType );
    }
    @Override
    public boolean isShadowedBy ( FrameworkField otherMember ) {
        return otherMember.getName().equals ( getName() );
    }
    @Override
    protected int getModifiers() {
        return field.getModifiers();
    }
    public Field getField() {
        return field;
    }
    @Override
    public Class<?> getType() {
        return field.getType();
    }
    @Override
    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }
    public Object get ( Object target ) throws IllegalArgumentException, IllegalAccessException {
        return field.get ( target );
    }
    @Override
    public String toString() {
        return field.toString();
    }
}

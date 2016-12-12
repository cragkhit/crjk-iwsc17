package org.junit.experimental.theories.internal;
import org.junit.Assume;
import org.junit.experimental.theories.DataPoint;
import org.junit.runners.model.FrameworkMethod;
import org.junit.experimental.theories.PotentialAssignment;
static class MethodParameterValue extends PotentialAssignment {
    private final FrameworkMethod method;
    private MethodParameterValue ( final FrameworkMethod dataPointMethod ) {
        this.method = dataPointMethod;
    }
    public Object getValue() throws CouldNotGenerateValueException {
        try {
            return this.method.invokeExplosively ( null, new Object[0] );
        } catch ( IllegalArgumentException e ) {
            throw new RuntimeException ( "unexpected: argument length is checked" );
        } catch ( IllegalAccessException e2 ) {
            throw new RuntimeException ( "unexpected: getMethods returned an inaccessible method" );
        } catch ( Throwable throwable ) {
            final DataPoint annotation = this.method.getAnnotation ( DataPoint.class );
            Assume.assumeTrue ( annotation == null || !AllMembersSupplier.access$000 ( annotation.ignoredExceptions(), throwable ) );
            throw new CouldNotGenerateValueException ( throwable );
        }
    }
    public String getDescription() throws CouldNotGenerateValueException {
        return this.method.getName();
    }
}

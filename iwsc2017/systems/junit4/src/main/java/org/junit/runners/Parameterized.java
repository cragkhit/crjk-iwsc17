package org.junit.runners;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.runner.Runner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParametersFactory;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import org.junit.runners.parameterized.TestWithParameters;
public class Parameterized extends Suite {
    @Retention ( RetentionPolicy.RUNTIME )
    @Target ( ElementType.METHOD )
    public @interface Parameters {
    String name() default "{index}";
    }
    @Retention ( RetentionPolicy.RUNTIME )
    @Target ( ElementType.FIELD )
    public @interface Parameter {
    int value() default 0;
    }
    @Retention ( RetentionPolicy.RUNTIME )
    @Inherited
    @Target ( ElementType.TYPE )
    public @interface UseParametersRunnerFactory {
    Class<? extends ParametersRunnerFactory> value() default BlockJUnit4ClassRunnerWithParametersFactory.class;
    }
    public Parameterized ( Class<?> klass ) throws Throwable {
        super ( klass, RunnersFactory.createRunnersForClass ( klass ) );
    }
    private static class RunnersFactory {
        private static final ParametersRunnerFactory DEFAULT_FACTORY = new BlockJUnit4ClassRunnerWithParametersFactory();
        private final TestClass testClass;
        static List<Runner> createRunnersForClass ( Class<?> klass )
        throws Throwable {
            return new RunnersFactory ( klass ).createRunners();
        }
        private RunnersFactory ( Class<?> klass ) {
            testClass = new TestClass ( klass );
        }
        private List<Runner> createRunners() throws Throwable {
            Parameters parameters = getParametersMethod().getAnnotation (
                                        Parameters.class );
            return Collections.unmodifiableList ( createRunnersForParameters (
                    allParameters(), parameters.name(),
                    getParametersRunnerFactory() ) );
        }
        private ParametersRunnerFactory getParametersRunnerFactory()
        throws InstantiationException, IllegalAccessException {
            UseParametersRunnerFactory annotation = testClass
                                                    .getAnnotation ( UseParametersRunnerFactory.class );
            if ( annotation == null ) {
                return DEFAULT_FACTORY;
            } else {
                Class<? extends ParametersRunnerFactory> factoryClass = annotation
                        .value();
                return factoryClass.newInstance();
            }
        }
        private TestWithParameters createTestWithNotNormalizedParameters (
            String pattern, int index, Object parametersOrSingleParameter ) {
            Object[] parameters = ( parametersOrSingleParameter instanceof Object[] ) ? ( Object[] ) parametersOrSingleParameter
                                  : new Object[] { parametersOrSingleParameter };
            return createTestWithParameters ( testClass, pattern, index,
                                              parameters );
        }
        @SuppressWarnings ( "unchecked" )
        private Iterable<Object> allParameters() throws Throwable {
            Object parameters = getParametersMethod().invokeExplosively ( null );
            if ( parameters instanceof Iterable ) {
                return ( Iterable<Object> ) parameters;
            } else if ( parameters instanceof Object[] ) {
                return Arrays.asList ( ( Object[] ) parameters );
            } else {
                throw parametersMethodReturnedWrongType();
            }
        }
        private FrameworkMethod getParametersMethod() throws Exception {
            List<FrameworkMethod> methods = testClass
                                            .getAnnotatedMethods ( Parameters.class );
            for ( FrameworkMethod each : methods ) {
                if ( each.isStatic() && each.isPublic() ) {
                    return each;
                }
            }
            throw new Exception ( "No public static parameters method on class "
                                  + testClass.getName() );
        }
        private List<Runner> createRunnersForParameters (
            Iterable<Object> allParameters, String namePattern,
            ParametersRunnerFactory runnerFactory ) throws Exception {
            try {
                List<TestWithParameters> tests = createTestsForParameters (
                                                     allParameters, namePattern );
                List<Runner> runners = new ArrayList<Runner>();
                for ( TestWithParameters test : tests ) {
                    runners.add ( runnerFactory
                                  .createRunnerForTestWithParameters ( test ) );
                }
                return runners;
            } catch ( ClassCastException e ) {
                throw parametersMethodReturnedWrongType();
            }
        }
        private List<TestWithParameters> createTestsForParameters (
            Iterable<Object> allParameters, String namePattern )
        throws Exception {
            int i = 0;
            List<TestWithParameters> children = new ArrayList<TestWithParameters>();
            for ( Object parametersOfSingleTest : allParameters ) {
                children.add ( createTestWithNotNormalizedParameters ( namePattern,
                               i++, parametersOfSingleTest ) );
            }
            return children;
        }
        private Exception parametersMethodReturnedWrongType() throws Exception {
            String className = testClass.getName();
            String methodName = getParametersMethod().getName();
            String message = MessageFormat.format (
                                 "{0}.{1}() must return an Iterable of arrays.", className,
                                 methodName );
            return new Exception ( message );
        }
        private TestWithParameters createTestWithParameters (
            TestClass testClass, String pattern, int index,
            Object[] parameters ) {
            String finalPattern = pattern.replaceAll ( "\\{index\\}",
                                  Integer.toString ( index ) );
            String name = MessageFormat.format ( finalPattern, parameters );
            return new TestWithParameters ( "[" + name + "]", testClass,
                                            Arrays.asList ( parameters ) );
        }
    }
}

package org.junit.experimental.theories;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.Assume;
import org.junit.runners.BlockJUnit4ClassRunner;
import java.util.Iterator;
import org.junit.Assert;
import org.junit.experimental.theories.internal.Assignments;
import java.util.ArrayList;
import org.junit.internal.AssumptionViolatedException;
import java.util.List;
import org.junit.runners.model.TestClass;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
public static class TheoryAnchor extends Statement {
    private int successes;
    private final FrameworkMethod testMethod;
    private final TestClass testClass;
    private List<AssumptionViolatedException> fInvalidParameters;
    public TheoryAnchor ( final FrameworkMethod testMethod, final TestClass testClass ) {
        this.successes = 0;
        this.fInvalidParameters = new ArrayList<AssumptionViolatedException>();
        this.testMethod = testMethod;
        this.testClass = testClass;
    }
    private TestClass getTestClass() {
        return this.testClass;
    }
    public void evaluate() throws Throwable {
        this.runWithAssignment ( Assignments.allUnassigned ( this.testMethod.getMethod(), this.getTestClass() ) );
        final boolean hasTheoryAnnotation = this.testMethod.getAnnotation ( Theory.class ) != null;
        if ( this.successes == 0 && hasTheoryAnnotation ) {
            Assert.fail ( "Never found parameters that satisfied method assumptions.  Violated assumptions: " + this.fInvalidParameters );
        }
    }
    protected void runWithAssignment ( final Assignments parameterAssignment ) throws Throwable {
        if ( !parameterAssignment.isComplete() ) {
            this.runWithIncompleteAssignment ( parameterAssignment );
        } else {
            this.runWithCompleteAssignment ( parameterAssignment );
        }
    }
    protected void runWithIncompleteAssignment ( final Assignments incomplete ) throws Throwable {
        for ( final PotentialAssignment source : incomplete.potentialsForNextUnassigned() ) {
            this.runWithAssignment ( incomplete.assignNext ( source ) );
        }
    }
    protected void runWithCompleteAssignment ( final Assignments complete ) throws Throwable {
        new BlockJUnit4ClassRunner ( this.getTestClass().getJavaClass() ) {
            protected void collectInitializationErrors ( final List<Throwable> errors ) {
            }
            public Statement methodBlock ( final FrameworkMethod method ) {
                final Statement statement = super.methodBlock ( method );
                return new Statement() {
                    public void evaluate() throws Throwable {
                        try {
                            statement.evaluate();
                            TheoryAnchor.this.handleDataPointSuccess();
                        } catch ( AssumptionViolatedException e ) {
                            TheoryAnchor.this.handleAssumptionViolation ( e );
                        } catch ( Throwable e2 ) {
                            TheoryAnchor.this.reportParameterizedError ( e2, complete.getArgumentStrings ( TheoryAnchor.this.nullsOk() ) );
                        }
                    }
                };
            }
            protected Statement methodInvoker ( final FrameworkMethod method, final Object test ) {
                return TheoryAnchor.this.methodCompletesWithParameters ( method, complete, test );
            }
            public Object createTest() throws Exception {
                final Object[] params = complete.getConstructorArguments();
                if ( !TheoryAnchor.this.nullsOk() ) {
                    Assume.assumeNotNull ( params );
                }
                return this.getTestClass().getOnlyConstructor().newInstance ( params );
            }
        } .methodBlock ( this.testMethod ).evaluate();
    }
    private Statement methodCompletesWithParameters ( final FrameworkMethod method, final Assignments complete, final Object freshInstance ) {
        return new Statement() {
            public void evaluate() throws Throwable {
                final Object[] values = complete.getMethodArguments();
                if ( !TheoryAnchor.this.nullsOk() ) {
                    Assume.assumeNotNull ( values );
                }
                method.invokeExplosively ( freshInstance, values );
            }
        };
    }
    protected void handleAssumptionViolation ( final AssumptionViolatedException e ) {
        this.fInvalidParameters.add ( e );
    }
    protected void reportParameterizedError ( final Throwable e, final Object... params ) throws Throwable {
        if ( params.length == 0 ) {
            throw e;
        }
        throw new ParameterizedAssertionError ( e, this.testMethod.getName(), params );
    }
    private boolean nullsOk() {
        final Theory annotation = this.testMethod.getMethod().getAnnotation ( Theory.class );
        return annotation != null && annotation.nullsAccepted();
    }
    protected void handleDataPointSuccess() {
        ++this.successes;
    }
}

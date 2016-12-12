package javax.el;
public abstract class EvaluationListener {
    public void beforeEvaluation ( final ELContext context, final String expression ) {
    }
    public void afterEvaluation ( final ELContext context, final String expression ) {
    }
    public void propertyResolved ( final ELContext context, final Object base, final Object property ) {
    }
}

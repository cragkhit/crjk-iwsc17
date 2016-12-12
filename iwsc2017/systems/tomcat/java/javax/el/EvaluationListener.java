package javax.el;
public abstract class EvaluationListener {
    public void beforeEvaluation ( ELContext context, String expression ) {
    }
    public void afterEvaluation ( ELContext context, String expression ) {
    }
    public void propertyResolved ( ELContext context, Object base, Object property ) {
    }
}

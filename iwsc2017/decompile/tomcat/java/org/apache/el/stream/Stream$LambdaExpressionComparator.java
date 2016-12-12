package org.apache.el.stream;
import javax.el.ELContext;
import org.apache.el.lang.ELSupport;
import javax.el.LambdaExpression;
import java.util.Comparator;
private static class LambdaExpressionComparator implements Comparator<Object> {
    private final LambdaExpression le;
    public LambdaExpressionComparator ( final LambdaExpression le ) {
        this.le = le;
    }
    @Override
    public int compare ( final Object o1, final Object o2 ) {
        return ELSupport.coerceToNumber ( null, this.le.invoke ( new Object[] { o1, o2 } ), Integer.class ).intValue();
    }
}

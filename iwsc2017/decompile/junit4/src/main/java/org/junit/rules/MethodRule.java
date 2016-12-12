package org.junit.rules;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
public interface MethodRule {
    Statement apply ( Statement p0, FrameworkMethod p1, Object p2 );
}

package org.junit.rules;
import org.junit.Rule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
public interface MethodRule {
    Statement apply ( Statement base, FrameworkMethod method, Object target );
}

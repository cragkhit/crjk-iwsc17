package org.junit.rules;
import org.junit.runner.Description;
public class TestName extends TestWatcher {
    private volatile String name;
    @Override
    protected void starting ( Description d ) {
        name = d.getMethodName();
    }
    public String getMethodName() {
        return name;
    }
}

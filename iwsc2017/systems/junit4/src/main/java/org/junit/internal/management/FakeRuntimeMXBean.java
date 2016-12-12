package org.junit.internal.management;
import java.util.Collections;
import java.util.List;
class FakeRuntimeMXBean implements RuntimeMXBean {
    @Override
    public List<String> getInputArguments() {
        return Collections.emptyList();
    }
}

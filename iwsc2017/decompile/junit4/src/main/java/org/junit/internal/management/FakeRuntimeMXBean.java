package org.junit.internal.management;
import java.util.Collections;
import java.util.List;
class FakeRuntimeMXBean implements RuntimeMXBean {
    public List<String> getInputArguments() {
        return Collections.emptyList();
    }
}

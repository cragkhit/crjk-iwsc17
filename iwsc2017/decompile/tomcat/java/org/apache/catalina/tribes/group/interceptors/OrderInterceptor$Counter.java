package org.apache.catalina.tribes.group.interceptors;
import java.util.concurrent.atomic.AtomicInteger;
protected static class Counter {
    private final AtomicInteger value;
    protected Counter() {
        this.value = new AtomicInteger ( 0 );
    }
    public int getCounter() {
        return this.value.get();
    }
    public void setCounter ( final int counter ) {
        this.value.set ( counter );
    }
    public int inc() {
        return this.value.addAndGet ( 1 );
    }
}

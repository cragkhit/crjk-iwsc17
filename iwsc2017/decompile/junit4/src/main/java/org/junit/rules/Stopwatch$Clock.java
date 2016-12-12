package org.junit.rules;
static class Clock {
    public long nanoTime() {
        return System.nanoTime();
    }
}

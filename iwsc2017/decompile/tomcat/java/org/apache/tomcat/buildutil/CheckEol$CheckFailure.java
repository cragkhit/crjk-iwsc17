package org.apache.tomcat.buildutil;
import java.io.File;
private static class CheckFailure {
    private final File file;
    private final int line;
    private final String value;
    public CheckFailure ( final File file, final int line, final String value ) {
        this.file = file;
        this.line = line;
        this.value = value;
    }
    @Override
    public String toString() {
        return System.lineSeparator() + this.file + ": uses " + this.value + " on line " + this.line;
    }
}

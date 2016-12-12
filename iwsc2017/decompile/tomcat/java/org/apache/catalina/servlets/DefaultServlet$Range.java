package org.apache.catalina.servlets;
protected static class Range {
    public long start;
    public long end;
    public long length;
    public boolean validate() {
        if ( this.end >= this.length ) {
            this.end = this.length - 1L;
        }
        return this.start >= 0L && this.end >= 0L && this.start <= this.end && this.length > 0L;
    }
}

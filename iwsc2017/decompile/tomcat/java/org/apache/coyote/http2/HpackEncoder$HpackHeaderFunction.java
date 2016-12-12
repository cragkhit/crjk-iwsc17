package org.apache.coyote.http2;
private interface HpackHeaderFunction {
    boolean shouldUseIndexing ( String p0, String p1 );
    boolean shouldUseHuffman ( String p0, String p1 );
    boolean shouldUseHuffman ( String p0 );
}

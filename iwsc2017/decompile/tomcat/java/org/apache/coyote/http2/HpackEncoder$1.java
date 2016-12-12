package org.apache.coyote.http2;
static final class HpackEncoder$1 implements HpackHeaderFunction {
    @Override
    public boolean shouldUseIndexing ( final String headerName, final String value ) {
        return !headerName.equals ( "content-length" ) && !headerName.equals ( "date" );
    }
    @Override
    public boolean shouldUseHuffman ( final String header, final String value ) {
        return value.length() > 5;
    }
    @Override
    public boolean shouldUseHuffman ( final String header ) {
        return header.length() > 5;
    }
}

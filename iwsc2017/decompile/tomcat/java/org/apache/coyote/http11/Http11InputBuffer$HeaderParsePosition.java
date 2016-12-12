package org.apache.coyote.http11;
private enum HeaderParsePosition {
    HEADER_START,
    HEADER_NAME,
    HEADER_VALUE_START,
    HEADER_VALUE,
    HEADER_MULTI_LINE,
    HEADER_SKIPLINE;
}

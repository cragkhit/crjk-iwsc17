package org.apache.coyote.http11;
private enum HeaderParseStatus {
    DONE,
    HAVE_MORE_HEADERS,
    NEED_MORE_DATA;
}

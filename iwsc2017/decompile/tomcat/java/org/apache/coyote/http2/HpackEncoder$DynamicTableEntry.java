package org.apache.coyote.http2;
private class DynamicTableEntry extends TableEntry {
    private DynamicTableEntry ( final String name, final String value, final int position ) {
        super ( name, value, position );
    }
    @Override
    int getPosition() {
        return super.getPosition() + HpackEncoder.access$600 ( HpackEncoder.this ) + Hpack.STATIC_TABLE_LENGTH;
    }
}

package org.apache.juli;
class OneLineFormatter$2 extends ThreadLocal<DateFormatCache> {
    final   String val$timeFormat;
    final   DateFormatCache val$globalDateCache;
    @Override
    protected DateFormatCache initialValue() {
        return new DateFormatCache ( 5, this.val$timeFormat, this.val$globalDateCache );
    }
}

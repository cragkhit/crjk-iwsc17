package org.jfree.data.time;
protected class ValueSequence {
    float[] dataPoints;
    public ValueSequence ( final DynamicTimeSeriesCollection this$0 ) {
        this ( this$0, DynamicTimeSeriesCollection.access$000 ( this$0 ) );
    }
    public ValueSequence ( final int length ) {
        this.dataPoints = new float[length];
        for ( int i = 0; i < length; ++i ) {
            this.dataPoints[i] = 0.0f;
        }
    }
    public void enterData ( final int index, final float value ) {
        this.dataPoints[index] = value;
    }
    public float getData ( final int index ) {
        return this.dataPoints[index];
    }
}

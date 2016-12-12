package org.apache.jasper.compiler;
private static class LineInfo {
    private int inputStartLine;
    private int outputStartLine;
    private int lineFileID;
    private int inputLineCount;
    private int outputLineIncrement;
    private boolean lineFileIDSet;
    private LineInfo() {
        this.inputStartLine = -1;
        this.outputStartLine = -1;
        this.lineFileID = 0;
        this.inputLineCount = 1;
        this.outputLineIncrement = 1;
        this.lineFileIDSet = false;
    }
    public void setInputStartLine ( final int inputStartLine ) {
        if ( inputStartLine < 0 ) {
            throw new IllegalArgumentException ( "" + inputStartLine );
        }
        this.inputStartLine = inputStartLine;
    }
    public void setOutputStartLine ( final int outputStartLine ) {
        if ( outputStartLine < 0 ) {
            throw new IllegalArgumentException ( "" + outputStartLine );
        }
        this.outputStartLine = outputStartLine;
    }
    public void setLineFileID ( final int lineFileID ) {
        if ( lineFileID < 0 ) {
            throw new IllegalArgumentException ( "" + lineFileID );
        }
        this.lineFileID = lineFileID;
        this.lineFileIDSet = true;
    }
    public void setInputLineCount ( final int inputLineCount ) {
        if ( inputLineCount < 0 ) {
            throw new IllegalArgumentException ( "" + inputLineCount );
        }
        this.inputLineCount = inputLineCount;
    }
    public void setOutputLineIncrement ( final int outputLineIncrement ) {
        if ( outputLineIncrement < 0 ) {
            throw new IllegalArgumentException ( "" + outputLineIncrement );
        }
        this.outputLineIncrement = outputLineIncrement;
    }
    public String getString() {
        if ( this.inputStartLine == -1 || this.outputStartLine == -1 ) {
            throw new IllegalStateException();
        }
        final StringBuilder out = new StringBuilder();
        out.append ( this.inputStartLine );
        if ( this.lineFileIDSet ) {
            out.append ( "#" + this.lineFileID );
        }
        if ( this.inputLineCount != 1 ) {
            out.append ( "," + this.inputLineCount );
        }
        out.append ( ":" + this.outputStartLine );
        if ( this.outputLineIncrement != 1 ) {
            out.append ( "," + this.outputLineIncrement );
        }
        out.append ( '\n' );
        return out.toString();
    }
    @Override
    public String toString() {
        return this.getString();
    }
}
